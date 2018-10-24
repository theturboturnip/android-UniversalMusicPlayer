package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.SONG_TAG_JOIN_TABLE,
		primaryKeys = { "songId", "tagId" },
		foreignKeys = {
@ForeignKey(entity = SongEntity.class,
		parentColumns = "id",
		childColumns = "songId"),
@ForeignKey(entity = TagEntity.class,
		parentColumns = "id",
		childColumns = "tagId")
                })
public class SongTagJoinEntity {
	public final int songId;
	public final int tagId;

	public SongTagJoinEntity(int songId, int tagId){
		this.songId = songId;
		this.tagId = tagId;
	}
}
