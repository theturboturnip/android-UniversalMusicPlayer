package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface TagEntityDao {
	@Query("SELECT * FROM " + DBConstants.TAG_TABLE + " WHERE id=:id")
	TagEntity getTag(int id);
	@Query("SELECT * FROM " + DBConstants.TAG_TABLE + " WHERE name=:name")
	TagEntity getTag(String name);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertTag(TagEntity tags);

	@Query("DELETE FROM " + DBConstants.TAG_TABLE)
	void clearDatabase();
}