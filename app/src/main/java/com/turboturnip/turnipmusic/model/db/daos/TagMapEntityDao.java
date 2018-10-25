package com.turboturnip.turnipmusic.model.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.model.db.entities.TagEntity;
import com.turboturnip.turnipmusic.model.db.entities.TagMapEntity;

import java.util.List;

@Dao
public interface TagMapEntityDao {
	@Query("SELECT * FROM " + DBConstants.TAG_MAP_TABLE + " WHERE id=:songMediaId")
	List<TagEntity> getTagsFromSongMediaId(String songMediaId);
	@Query("SELECT songMediaId FROM " + DBConstants.TAG_MAP_TABLE + " INNER JOIN " + DBConstants.TAG_TABLE + " ON "+DBConstants.TAG_TABLE+".id="+DBConstants.TAG_MAP_TABLE+".tagId WHERE name LIKE :tag")
	List<String> getSongMediaIdsWithTag(String tag);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertTagMap(TagMapEntity tags);

	@Query("DELETE FROM " + DBConstants.TAG_MAP_TABLE)
	void clearDatabase();
}