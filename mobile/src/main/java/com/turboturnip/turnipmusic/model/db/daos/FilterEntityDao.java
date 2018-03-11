package com.turboturnip.turnipmusic.model.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.model.db.entities.FilterEntity;

import java.util.List;

@Dao
public interface FilterEntityDao {
	@Query("SELECT * FROM " + DBConstants.FILTER_TABLE)
	List<FilterEntity> getFilters();
	@Query("SELECT * FROM " + DBConstants.FILTER_TABLE + " WHERE id=:id")
	FilterEntity getFilter(int id);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertFilter(FilterEntity filter);

	@Query("DELETE FROM " + DBConstants.FILTER_TABLE)
	void clearDatabase();
}
