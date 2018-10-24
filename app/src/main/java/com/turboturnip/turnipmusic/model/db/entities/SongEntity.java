package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.SONG_TABLE)
public class SongEntity {
	@PrimaryKey(autoGenerate = true)
	private int id;
	public final String mediaId;
	public final String name;
	public final int albumId;
	public final int albumIndex;

	@Ignore
	public SongEntity(String mediaId, String name){
		this(mediaId, name, -1, -1);
	}
	@Ignore
	public SongEntity(String mediaId, String name, int albumId, int albumIndex){
		this.id = 0;
		this.mediaId = mediaId;
		this.name = name;
		this.albumId = albumId;
		this.albumIndex = albumIndex;
	}
	public SongEntity(int id, String mediaId, String name, int albumId, int albumIndex){
		this.id = id;
		this.mediaId = mediaId;
		this.name = name;
		this.albumId = albumId;
		this.albumIndex = albumIndex;
	}

	public int getId(){return id;}
	public void setId(int newId){id = newId;}
}
