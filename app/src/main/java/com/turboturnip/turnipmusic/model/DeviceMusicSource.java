package com.turboturnip.turnipmusic.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.model.db.SongDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility Class that gets music from the device using MediaSource
 */

public class DeviceMusicSource implements MusicProviderSource {

	private static final String TAG = LogHelper.makeLogTag(DeviceMusicSource.class);

	// TODO: Use the projections?
	private static String[] musicProjection = null;
	private static String[] genresProjection = null;
	private static String[] albumProjection = null;

	// TODO: Should this return Map<String, Album>?
	@Override
    public Collection<Album> albums(Context context) {
	    ArrayList<Album> albums = new ArrayList<>();

        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumProjection,
                "NOT "+MediaStore.Audio.Albums.NUMBER_OF_SONGS+"=0",
                null,
                null);
        if (albumCursor == null){
            LogHelper.e(TAG, "Failed to get albums");
            return albums;
        }
        int albumIdColumn = albumCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int albumNameColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
        int albumArtColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
        int albumTotalSongsColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
        int albumArtistColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);

        if (!albumCursor.moveToFirst()) return albums;
        do {
            String albumLibraryId = albumCursor.getString(albumIdColumn);
            String albumName = albumCursor.getString(albumNameColumn);
            String albumArtist = albumCursor.getString(albumArtistColumn);
            String artPath = albumCursor.getString(albumArtColumn);
            long totalTrackCount = albumCursor.getLong(albumTotalSongsColumn);
            LogHelper.d(TAG, "Album ", albumName, " has ", totalTrackCount);

            albums.add(new Album(albumLibraryId, albumName, albumArtist, artPath, totalTrackCount));
        } while (albumCursor.moveToNext());

        albumCursor.close();

        return albums;
    }

	@Override
	public Collection<Song> songs(Context context, Map<String, Album> albums, SongDatabase db) {
		try {
			ArrayList<Song> songs = new ArrayList<>();
			Cursor musicCursor = context.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicProjection, null, null,
					null);


			if (musicCursor == null) {
				LogHelper.e(TAG, "Failed to retrieve music: Query Failed");
				return songs;
			} else if (!musicCursor.moveToFirst()) {
				LogHelper.e(TAG, "No music found on the device!.");
				return songs;
			}
			LogHelper.i(TAG, "Listing...");
			// retrieve the indices of the columns where the ID, title, etc. of the song are
			int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
			int filePathColumn = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
			int albumFromMusicColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
			int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
			int albumIDColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
			int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
			int trackNumberColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);

			int isMusicColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);

			Cursor genresCursor;

			// add each song to mItems
			do {
				if (musicCursor.getInt(isMusicColumn) == 0) continue;

				MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

				String mediaId = musicCursor.getString(idColumn);

				metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
						.putString(MediaMetadataCompat.METADATA_KEY_TITLE,  musicCursor.getString(titleColumn))
						.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicCursor.getString(artistColumn))
						.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, musicCursor.getLong(durationColumn))
                        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, musicCursor.getLong(trackNumberColumn));

				String genre = "";
				{
					Uri genreUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", Integer.parseInt(mediaId));
					genresCursor = context.getContentResolver().query(genreUri,
							genresProjection, null, null, null);
					int genreColumn = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
					if (genresCursor.moveToFirst()) {
						genre = genresCursor.getString(genreColumn);
						LogHelper.i("Found Genre: ", genre);
					}
					genresCursor.close();
				}
				metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre);


                int albumLibraryId = musicCursor.getInt(albumIDColumn);
				Album album = albums.get(""+albumLibraryId);
				if (album != null) {
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album.name)
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, album.artPath)
                            .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, album.trackCount);
				} else {
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicCursor.getString(albumFromMusicColumn))
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "")
                            .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 0);
				}


				songs.add(new Song(metadataBuilder.build(), musicCursor.getString(filePathColumn), album));
			} while (musicCursor.moveToNext());

			musicCursor.close();

			LogHelper.i(TAG, "Collected ", songs.size(), " total songs");
			return songs;
		} catch (Exception e) {
			LogHelper.e(TAG, e, "Could not retrieve music list");
			throw new RuntimeException("Could not retrieve music list", e);
		}
	}

    @Override
	public List<String> songMediaIdsForAlbumLibraryId(Context context, String albumLibraryId){
        Cursor songCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.ALBUM_ID},
                MediaStore.Audio.Media.ALBUM_ID+" is ?",
                new String[]{albumLibraryId},
                MediaStore.Audio.Media.TRACK);

        int idColumn = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);

        List<String> songMediaIds = new ArrayList<>(songCursor.getCount());
        if (!songCursor.moveToFirst()){
            songCursor.close();
            return songMediaIds;
        }
        do {
            songMediaIds.add(songCursor.getString(idColumn));
        }while (songCursor.moveToNext());

        return songMediaIds;
    }
}
