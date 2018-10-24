package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.ALBUM_TABLE)
public class AlbumEntity {
	@PrimaryKey(autoGenerate = true)
	private int id;
	public final String name;
	public final int trackCount;
	public final String artPath;

	@Ignore
	public AlbumEntity(String name, int trackCount, String artPath){
		this.id = 0;
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

	public int getId(){return id;}
	public void setId(int newId){id = newId;}
}
