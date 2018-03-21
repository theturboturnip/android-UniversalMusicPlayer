package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.db.daos.AlbumEntityDao;
import com.turboturnip.turnipmusic.model.db.daos.JourneyEntityDao;
import com.turboturnip.turnipmusic.model.db.daos.JourneyStageEntityDao;
import com.turboturnip.turnipmusic.model.db.daos.JourneyStageJoinEntityDao;
import com.turboturnip.turnipmusic.model.db.daos.SongEntityDao;
import com.turboturnip.turnipmusic.model.db.daos.SongTagJoinEntityDao;
import com.turboturnip.turnipmusic.model.db.daos.TagEntityDao;
import com.turboturnip.turnipmusic.model.db.entities.AlbumEntity;
import com.turboturnip.turnipmusic.model.db.entities.JourneyEntity;
import com.turboturnip.turnipmusic.model.db.entities.JourneyStageEntity;
import com.turboturnip.turnipmusic.model.db.entities.JourneyStageJoinEntity;
import com.turboturnip.turnipmusic.model.db.entities.SongEntity;
import com.turboturnip.turnipmusic.model.db.entities.SongTagJoinEntity;
import com.turboturnip.turnipmusic.model.db.entities.TagEntity;

import java.util.List;

// TODO: Multiple filter support in stages!
@Database(entities = { SongEntity.class, TagEntity.class, SongTagJoinEntity.class, AlbumEntity.class, JourneyEntity.class, JourneyStageEntity.class, JourneyStageJoinEntity.class },
		version = 1)
public abstract class SongDatabase extends RoomDatabase {

	private static volatile SongDatabase INSTANCE;

	public abstract SongEntityDao songDao();
	public abstract TagEntityDao tagDao();
	public abstract SongTagJoinEntityDao songTagJoinDao();
	public abstract AlbumEntityDao albumDao();
	public abstract JourneyEntityDao journeyDao();
	public abstract JourneyStageEntityDao journeyStageDao();
	public abstract JourneyStageJoinEntityDao journeyStageJoinDao();

	public static SongDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SongDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
							SongDatabase.class, "Songs.db")
							.build();
				}
			}
		}
		return INSTANCE;
	}

	// TODO
	public void clean(){
		// Remove tags without songs
	}
	// TODO
	public void clearAllDatabases(){

	}

	public Journey createJourney(int id){
		return createJourney(journeyDao().getJourney(id));
	}
	public Journey createJourney(JourneyEntity entity){
		List<JourneyStageEntity> stageEntities = journeyStageJoinDao().getStagesForJourney(entity.id);
		Journey.Stage[] stages = new Journey.Stage[stageEntities.size()];
		for (int i = 0; i < stages.length; i++){
			JourneyStageEntity stageEntity = stageEntities.get(i);
			stages[i] = stageEntity.getStage();
		}
		return new Journey(entity.id, entity.name, stages);
	}
	public void insertJourney(Journey journey){
		int id = journey.id;
		JourneyEntity journeyEntity = new JourneyEntity(journey);
		if (id > 0) {
			journeyDao().updateJourney(journeyEntity);
			journeyStageJoinDao().deleteJoinsForJourney(id);
		}else {
			id = (int) journeyDao().insertJourney(journeyEntity);
		}
		int i = 0;
		for (Journey.Stage s : journey.stages){
			JourneyStageEntity stageEntity = new JourneyStageEntity(s);
			int stageId = stageEntity.id;
			if (stageId > 0) {
				journeyStageDao().updateStage(stageEntity);
			}else{
				stageId = (int) journeyStageDao().insertStage(stageEntity);
			}
			journeyStageJoinDao().insertJoin(new JourneyStageJoinEntity(id, stageId, i));
			i++;
		}
	}
}