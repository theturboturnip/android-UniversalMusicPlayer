package com.turboturnip.turnipmusic.model;

import android.support.v4.media.MediaMetadataCompat;

import com.turboturnip.turnipmusic.model.db.SongEntity;

import com.turboturnip.turnipmusic.model.db.SongTags;

import java.util.ArrayList;

/**
 * Convenience class that packages the MediaMetadata
 */

public class Song {
	private SongEntity dbEntity;
	private MediaMetadataCompat metadata;
	private String filePath;
	private ArrayList<Integer> tags;

	public Song(SongEntity dbEntity, String filePath, String album, String artist, Long duration, String genre, String iconUrl, Long trackNumber, Long totalTrackCount){
		this.dbEntity = dbEntity;
		this.metadata = new MediaMetadataCompat.Builder()
				.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, dbEntity.mediaId)
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
				.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
				.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
				.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, dbEntity.name)
				.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
				.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
				.build();
		this.filePath = filePath;
		// TODO: Put tags in the constructor
		this.tags = new ArrayList<>();
	}

	public final MediaMetadataCompat getMetadata(){ return metadata; }
	public void setMetadata(MediaMetadataCompat metadata) { this.metadata = metadata; }
	public final String getFilePath(){ return filePath; }
	public final String getSongID(){ return dbEntity.mediaId; }
	public final ArrayList<Integer> getTags(){ return tags; }

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != Song.class) {
			return false;
		}

		Song that = (Song) o;

		return dbEntity.id == that.dbEntity.id;
	}

	@Override
	public int hashCode() {
		return dbEntity.id;
	}
}
