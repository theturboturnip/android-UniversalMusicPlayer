package com.turboturnip.turnipmusic.model.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.model.db.entities.JourneyEntity;

import java.util.List;

@Dao
public interface JourneyEntityDao {
	@Query("SELECT * FROM " + DBConstants.JOURNEY_TABLE + " ORDER BY name")
	List<JourneyEntity> getJourneys();
	@Query("SELECT * FROM " + DBConstants.JOURNEY_TABLE + " WHERE id=:id")
	JourneyEntity getJourney(int id);
	@Query("SELECT * FROM " + DBConstants.JOURNEY_TABLE + " WHERE name like :name")
	JourneyEntity getJourneyForName(String name);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertJourney(JourneyEntity journey);

	@Query("DELETE FROM " + DBConstants.JOURNEY_TABLE + " WHERE id=:id")
	void removeJourney(int id);
	@Query("DELETE FROM " + DBConstants.JOURNEY_TABLE)
	void clearDatabase();
}
