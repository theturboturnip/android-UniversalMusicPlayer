package com.turboturnip.turnipplaylists.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.turboturnip.turnipplaylists.db.DBConstants;

@Entity(tableName = DBConstants.VIDEO_ENTITY_TABLE)
public class VideoEntity {
	@PrimaryKey
	@NonNull
	public String videoId;

	@NonNull
	public String name;
	@NonNull
	public String artist;
	@NonNull
	public String album;

	public VideoEntity(@NonNull String videoId, @NonNull String name, @NonNull String artist, @NonNull String album){
		this.videoId = videoId;
		this.name = name;
		this.artist = artist;
		this.album = album;
	}
}
