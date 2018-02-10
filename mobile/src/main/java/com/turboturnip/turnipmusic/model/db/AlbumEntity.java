package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = DBConstants.ALBUM_TABLE)
public class AlbumEntity {
	@PrimaryKey
	public final int id;
	public final String name;
	public final int trackCount;
	public final String artPath;

	@Ignore
	public AlbumEntity(String name, int trackCount, String artPath){
		this.id = name.hashCode();
		this.name = name;
		this.trackCount = trackCount;
		this.artPath = artPath;
	}
	public AlbumEntity(int id, String name, int trackCount, String artPath){
		this.id = id;
		this.name = name;
		this.trackCount = trackCount;
		this.artPath = artPath;
	}
}
