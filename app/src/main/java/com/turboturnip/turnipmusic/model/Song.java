package com.turboturnip.turnipmusic.model;

import android.support.v4.media.MediaMetadataCompat;

import com.turboturnip.turboshuffle.TurboShuffleSong;

import java.util.ArrayList;

/**
 * Convenience class that packages the MediaMetadata
 */

public class Song implements TurboShuffleSong<Integer>{
	private MediaMetadataCompat metadata;
	private final String filePath;
	private final ArrayList<Integer> tags;
    private final int metadataIdHash;
    private final Album album; // TODO: Replace with Album

	public Song(MediaMetadataCompat metadata, String filePath, Album album){//SongEntity dbEntity, AlbumEntity albumDBEntity,  String artist, Long duration, String genre){
		this.metadata = metadata;
		this.album = album;
		/*if (albumDBEntity != null &&  dbEntity.albumId != albumDBEntity.getId())
			throw new RuntimeException("Trying to create a Song with mismatching SongEntity and AlbumEntities");
		this.metadata = new MediaMetadataCompat.Builder()
				.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, dbEntity.getId()+"")
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumDBEntity == null ? "" : albumDBEntity.name)
				.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
				.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
				.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumDBEntity == null ? "" : albumDBEntity.artPath)
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, dbEntity.name)
				.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, dbEntity.albumIndex)
				.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, albumDBEntity == null ? 0 : albumDBEntity.trackCount)
				.build();*/
		this.filePath = filePath;
		// TODO: Put tags in the constructor
		this.tags = new ArrayList<>();
		this.metadataIdHash = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).hashCode();
	}

	public final MediaMetadataCompat getMetadata(){ return metadata; }
	public final void setMetadata(MediaMetadataCompat newVersion){ metadata = newVersion; }
	public final String getFilePath(){ return filePath; }
	public final int getLengthInSeconds(){ return (int)this.metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION); }
	public final ArrayList<Integer> getTags(){ return tags; }
	public final Integer getId() { return metadataIdHash; }
	public final String getLibraryId() { return this.metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID); }

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != Song.class) {
			return false;
		}

		Song that = (Song) o;

		return metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
				== that.metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
	}

	@Override
	public int hashCode() {
		return metadataIdHash;
	}
}
