package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;
import com.turboturnip.turnipmusic.model.db.Converters;
import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.FILTER_TABLE)
public class FilterEntity {
	@PrimaryKey
	public final int id;
	@TypeConverters(Converters.class)
	public final MusicFilterType filterType;
	public final String filterValue;

	public FilterEntity(MusicFilter filter){
		this.id = filter.hashCode();
		this.filterType = filter.filterType;
		this.filterValue = filter.filterValue;
	}
	public FilterEntity(int id, MusicFilterType filterType, String filterValue){
		this.id = id;
		this.filterType = filterType;
		this.filterValue = filterValue;
	}

	public MusicFilter getMusicFilter(){
		return new MusicFilter(filterType, filterValue);
	}
}
