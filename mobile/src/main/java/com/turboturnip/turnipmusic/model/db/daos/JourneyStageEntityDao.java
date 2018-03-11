package com.turboturnip.turnipmusic.model.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.model.db.entities.JourneyStageEntity;

import java.util.List;

@Dao
public interface JourneyStageEntityDao {
	@Query("SELECT * FROM " + DBConstants.JOURNEY_STAGE_TABLE)
	List<JourneyStageEntity> getStages();
	@Query("SELECT * FROM " + DBConstants.JOURNEY_STAGE_TABLE + " WHERE id=:id")
	JourneyStageEntity getStage(int id);


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertStage(JourneyStageEntity stageEntity);

	@Query("DELETE FROM " + DBConstants.JOURNEY_STAGE_TABLE)
	void clearDatabase();
}