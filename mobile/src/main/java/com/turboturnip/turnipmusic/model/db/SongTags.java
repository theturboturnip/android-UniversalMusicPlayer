package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

@Entity(tableName="Tags")
public class SongTags {
	@NonNull
	@PrimaryKey
	@ColumnInfo(name="table_id")
	private String mTableId;

	@ColumnInfo(name="media_id")
	private String mMediaId;

	@ColumnInfo(name="concatenated_tags")
	private String mConcatenatedTags;

	@Ignore
	public SongTags(String mediaId){
		this.mTableId = UUID.randomUUID().toString();
		this.mMediaId = mediaId;
		this.mConcatenatedTags = "";
	}

	public SongTags(@NonNull String tableId, String mediaId, String concatenatedTags){
		this.mTableId = tableId;
		this.mMediaId = mediaId;
		this.mConcatenatedTags = concatenatedTags;
	}

	public String getTableId(){
		return mTableId;
	}
	public String getMediaId(){
		return mMediaId;
	}
	public String getConcatenatedTags(){
		return mConcatenatedTags;
	}
}
