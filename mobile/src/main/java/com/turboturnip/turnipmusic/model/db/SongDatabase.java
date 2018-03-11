package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.db.daos.*;
import com.turboturnip.turnipmusic.model.db.entities.*;

import java.util.List;

// TODO: Multiple filter support in stages!
@Database(entities = { SongEntity.class, TagEntity.class, SongTagJoinEntity.class, AlbumEntity.class, FilterEntity.class, JourneyEntity.class, JourneyStageEntity.class, JourneyStageJoinEntity.class },
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
	public abstract FilterEntityDao filterDao();

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
		// Remove filters without stages
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
			MusicFilter filter = filterDao().getFilter(stageEntity.filterId).getMusicFilter();
			stages[i] = stageEntity.getStage(filter);
		}
		return new Journey(entity.name, stages);
	}
	public void renameJourney(Journey journey, Journey oldVersion){
		int oldId = new JourneyEntity(oldVersion).id;
		journeyDao().removeJourney(oldId);
		journeyStageJoinDao().deleteJoinsForJourney(oldId);
		insertJourney(journey);
	}
	public void insertJourney(Journey journey){
		JourneyEntity journeyEntity = new JourneyEntity(journey);
		journeyDao().insertJourney(journeyEntity);
		journeyStageJoinDao().deleteJoinsForJourney(journeyEntity.id);
		int i = 0;
		for (Journey.Stage s : journey.stages){
			FilterEntity filterEntity = new FilterEntity(s.filters[0]);
			filterDao().insertFilter(filterEntity);
			JourneyStageEntity stageEntity = new JourneyStageEntity(s, filterEntity.id);
			journeyStageDao().insertStage(stageEntity);
			journeyStageJoinDao().insertJoin(new JourneyStageJoinEntity(journeyEntity.id, stageEntity.id, i));
			i++;
		}
	}
}