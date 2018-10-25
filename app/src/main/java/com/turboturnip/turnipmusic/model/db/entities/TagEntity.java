package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.TAG_TABLE)
public class TagEntity {
	@PrimaryKey(autoGenerate = true)
	public final int id;
	public final String name;

	@Ignore
	public TagEntity(String name){
		this.id = name.hashCode();
		this.name = name;
	}

	public TagEntity(int id, String name){
		this.id = id;
		this.name = name;
	}
}
