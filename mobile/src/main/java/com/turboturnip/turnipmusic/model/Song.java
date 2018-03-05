package com.turboturnip.turnipmusic.model;

import android.support.v4.media.MediaMetadataCompat;

import com.turboturnip.turboshuffle.TurboShuffleSong;
import com.turboturnip.turnipmusic.model.db.AlbumEntity;
import com.turboturnip.turnipmusic.model.db.SongEntity;

import java.util.ArrayList;

/**
 * Convenience class that packages the MediaMetadata
 */

public class Song implements TurboShuffleSong<String>{
	private SongEntity dbEntity;
	private MediaMetadataCompat metadata;
	private String filePath;
	private ArrayList<Integer> tags;

	public Song(SongEntity dbEntity, AlbumEntity albumDBEntity, String filePath, String artist, Long duration, String genre){
		this.dbEntity = dbEntity;
		if (albumDBEntity != null &&  dbEntity.albumId != albumDBEntity.id)
			throw new RuntimeException("Trying to create a Song with mismatching SongEntity and AlbumEntities");
		this.metadata = new MediaMetadataCompat.Builder()
				.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, dbEntity.mediaId)
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumDBEntity == null ? "" : albumDBEntity.name)
				.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
				.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
				.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumDBEntity == null ? "" : albumDBEntity.artPath)
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, dbEntity.name)
				.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, dbEntity.albumIndex)
				.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, albumDBEntity == null ? 0 : albumDBEntity.trackCount)
				.build();
		this.filePath = filePath;
		// TODO: Put tags in the constructor
		this.tags = new ArrayList<>();
	}

	public final MediaMetadataCompat getMetadata(){ return metadata; }
	public void setMetadata(MediaMetadataCompat metadata) { this.metadata = metadata; }
	public final String getFilePath(){ return filePath; }
	public final String getId(){ return dbEntity.mediaId; }
	public final int getLengthInSeconds(){ return (int)this.metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION); }
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
