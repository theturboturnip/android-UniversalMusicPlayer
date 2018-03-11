package com.turboturnip.turnipmusic.model.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;

import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.model.db.entities.JourneyStageEntity;
import com.turboturnip.turnipmusic.model.db.entities.JourneyStageJoinEntity;

import java.util.List;

@Dao
public interface JourneyStageJoinEntityDao {
	@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
	@Query("SELECT * FROM " + DBConstants.JOURNEY_STAGE_TABLE + " INNER JOIN "+
			DBConstants.JOURNEY_STAGE_JOIN_TABLE+" ON "+
			DBConstants.JOURNEY_STAGE_TABLE+".id="+DBConstants.JOURNEY_STAGE_JOIN_TABLE+".stageId WHERE "+
			DBConstants.JOURNEY_STAGE_JOIN_TABLE+".journeyId=:journeyId ORDER BY " + DBConstants.JOURNEY_STAGE_JOIN_TABLE + ".stageIndex"
	)
	List<JourneyStageEntity> getStagesForJourney(int journeyId);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertJoin(JourneyStageJoinEntity join);

	@Query("DELETE FROM " + DBConstants.JOURNEY_STAGE_JOIN_TABLE + " WHERE journeyId = :id")
	void deleteJoinsForJourney(int id);
}
