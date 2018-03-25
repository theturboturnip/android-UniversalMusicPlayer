package com.turboturnip.turnipplaylists.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.turboturnip.turnipplaylists.db.DBConstants;
import com.turboturnip.turnipplaylists.db.entities.VideoEntity;

@Dao
public interface VideoEntityDao {
	@Query("SELECT * FROM " + DBConstants.VIDEO_ENTITY_TABLE + " WHERE videoId like :id")
	VideoEntity getById(String id);

	@Insert(onConflict = OnConflictStrategy.FAIL)
	long newVideo(VideoEntity entity);

	@Update
	void updateVideo(VideoEntity entity);
}
