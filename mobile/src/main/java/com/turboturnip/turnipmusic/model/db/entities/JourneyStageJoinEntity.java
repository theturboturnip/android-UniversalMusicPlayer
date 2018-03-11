package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.JOURNEY_STAGE_JOIN_TABLE,
		primaryKeys = { "journeyId", "stageId" },
		foreignKeys = {
				@ForeignKey(entity = JourneyEntity.class,
						parentColumns = "id",
						childColumns = "journeyId"),
				@ForeignKey(entity = JourneyStageEntity.class,
						parentColumns = "id",
						childColumns = "stageId")
		})
public class JourneyStageJoinEntity {
	public final int journeyId;
	public final int stageId;
	public final int stageIndex;

	public JourneyStageJoinEntity(int journeyId, int stageId, int stageIndex){
		this.journeyId = journeyId;
		this.stageId = stageId;
		this.stageIndex = stageIndex;
	}
}
