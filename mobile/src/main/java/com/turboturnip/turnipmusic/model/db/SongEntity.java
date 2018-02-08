package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = DBConstants.SONG_TABLE)
public class SongEntity {
	@PrimaryKey
	public final int id;
	public final String mediaId;
	public final String name;

	@Ignore
	public SongEntity(String mediaId, String name){
		this.id = mediaId.hashCode();
		this.mediaId = mediaId;
		this.name = name;
	}
	public SongEntity(int id, String mediaId, String name){
		this.id = id;
		this.mediaId = mediaId;
		this.name = name;
	}
}
