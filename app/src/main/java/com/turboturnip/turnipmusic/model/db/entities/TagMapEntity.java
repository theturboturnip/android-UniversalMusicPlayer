package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.TAG_MAP_TABLE,
		foreignKeys = {
@ForeignKey(entity = TagEntity.class,
		parentColumns = "id",
		childColumns = "tagId")
                })
public class TagMapEntity {
	@PrimaryKey
	public final int id;
	public final String songMediaId;
	public final int tagId;

	public TagMapEntity(int id, String songMediaId, int tagId){
		this.id = id;
		this.songMediaId = songMediaId;
		this.tagId = tagId;
	}
}
