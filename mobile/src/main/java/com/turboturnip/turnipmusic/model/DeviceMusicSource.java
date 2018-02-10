package com.turboturnip.turnipmusic.model;

import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.turboturnip.turnipmusic.model.db.AlbumEntity;
import com.turboturnip.turnipmusic.model.db.SongEntity;
import com.turboturnip.turnipmusic.model.db.SongDatabase;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility Class that gets music from the device using MediaSource
 */

public class DeviceMusicSource implements MusicProviderSource {

	private static final String TAG = LogHelper.makeLogTag(DeviceMusicSource.class);

	// TODO: Use the projections.
	private static String[] musicProjection = null;
	private static String[] genresProjection = null;
	private static String[] albumProjection = null;

	@Override
	public Iterator<Song> iterator(Context context, SongDatabase db) {
		try {
			ArrayList<Song> tracks = new ArrayList<>();
			Cursor musicCursor = context.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicProjection, null, null,
					null);


			if (musicCursor == null) {
				LogHelper.e(TAG, "Failed to retrieve music: Query Failed");
				return tracks.iterator();
			} else if (!musicCursor.moveToFirst()) {
				LogHelper.e(TAG, "No music found on the device!.");
				return tracks.iterator();
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

			Cursor albumCursor = context.getContentResolver().query(
					MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumProjection, null, null,
					null);
			if (albumCursor == null){
				LogHelper.e(TAG, "Failed to get albums");
				return tracks.iterator();
			}
			int albumNameColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
			int albumArtColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
			int albumTotalSongsColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

			// add the albums to the database
			albumCursor.moveToPosition(0);
			do {
				String albumName = albumCursor.getString(albumNameColumn);
				String artPath = albumCursor.getString(albumArtColumn);
				int totalTrackCount = (int)albumCursor.getLong(albumTotalSongsColumn);
				AlbumEntity preexistingEntity = db.albumDao().getAlbumByName(albumName);
				if (preexistingEntity != null)
					db.albumDao().insertAlbum(new AlbumEntity(preexistingEntity.id, albumName, totalTrackCount, artPath));
				else
					db.albumDao().insertAlbum(new AlbumEntity(albumName, totalTrackCount, artPath));
			} while (albumCursor.moveToNext());

			Cursor genresCursor;

			// add each song to mItems
			do {
				if (musicCursor.getInt(isMusicColumn) == 0) continue;

				String title = musicCursor.getString(titleColumn);
				String id = musicCursor.getString(idColumn);
				String filePath = musicCursor.getString(filePathColumn);
				String artist = musicCursor.getString(artistColumn);
				long duration = musicCursor.getLong(durationColumn);

				String genre = "";
				{
					Uri genreUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", Integer.parseInt(id));
					genresCursor = context.getContentResolver().query(genreUri,
							genresProjection, null, null, null);
					int genreColumn = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
					if (genresCursor.moveToFirst()) {
						genre = genresCursor.getString(genreColumn);
						LogHelper.i("Found Genre: ", genre);
					}
					genresCursor.close();
				}

				AlbumEntity albumEntity = null;
				int albumID = -1;
				int albumIndex = -1;
				int albumDeviceID = musicCursor.getInt(albumIDColumn);
				if (albumCursor.moveToPosition(albumDeviceID)) {
					String albumName = albumCursor.getString(albumNameColumn);
					albumEntity = db.albumDao().getAlbumByName(albumName);

					// AlbumEntity cannot be null here, if the previous db update worked.
					albumID = albumEntity.id;
					albumIndex = (int)musicCursor.getLong(trackNumberColumn);
				} else {
					String albumName = musicCursor.getString(albumFromMusicColumn);
					albumEntity = db.albumDao().getAlbumByName(albumName);
					if (albumEntity == null) {
						albumEntity = new AlbumEntity(albumName, 1, "");
						db.albumDao().insertAlbum(albumEntity);
					}
					albumID = albumEntity.id;

					List<SongEntity> songsInAlbum = db.songDao().getSongsInAlbum(albumEntity.id);
					albumIndex = 0;
					for (SongEntity s : songsInAlbum){
						if (s.mediaId.equals(id))
							break;
						albumIndex++;
					}
				}

				SongEntity songEntity = new SongEntity(id, title, albumID, albumIndex);
				db.songDao().insertSong(songEntity);

				tracks.add(new Song(songEntity, albumEntity, filePath, artist, duration, genre));
			} while (musicCursor.moveToNext());

			albumCursor.close();
			musicCursor.close();

			LogHelper.i(TAG, "Collected ", tracks.size(), " total songs");
			return tracks.iterator();
		} catch (Exception e) {
			LogHelper.e(TAG, e, "Could not retrieve music list");
			throw new RuntimeException("Could not retrieve music list", e);
		}
	}
}
