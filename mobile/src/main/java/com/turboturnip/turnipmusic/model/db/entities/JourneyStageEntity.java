package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.turboturnip.turboshuffle.TurboShuffleConfig;
import com.turboturnip.turnipmusic.model.CompositeMusicFilter;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.db.Converters;
import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.utils.JSONHelper;

import org.json.JSONException;

@Entity(tableName = DBConstants.JOURNEY_STAGE_TABLE)
public class JourneyStageEntity {
	@PrimaryKey(autoGenerate = true)
	public final int id;
	public final String name;
	@TypeConverters(Converters.class)
	public final Journey.Stage.PlayType playType;
	public final int playCount;
	public final String shuffleConfigJSON;
	public final String filterArrayJSON;

	@Ignore
	public JourneyStageEntity(Journey.Stage base){
		this.id = base.id;
		this.name = base.name;
		this.playType = base.playType;
		this.playCount = base.playCount;
		this.shuffleConfigJSON = JSONHelper.TurboShuffleConfigHandler.encode(base.shuffleConfig);
		this.filterArrayJSON = JSONHelper.encodeStringArray(base.pools.toArray());
	}
	public JourneyStageEntity(int id, String name, Journey.Stage.PlayType playType, int playCount, String shuffleConfigJSON, String filterArrayJSON){
		this.id = id;
		this.name = name;
		this.playType = playType;
		this.playCount = playCount;
		this.shuffleConfigJSON = shuffleConfigJSON;
		this.filterArrayJSON = filterArrayJSON;
	}
	public final TurboShuffleConfig getConfig(){
		if (shuffleConfigJSON == null || shuffleConfigJSON.isEmpty()) return null;
		try {
			return JSONHelper.TurboShuffleConfigHandler.decode(JSONHelper.typeCheckJSONObject(shuffleConfigJSON, JSONHelper.TurboShuffleConfigHandler.JSON_TYPE_VALUE));
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	public final CompositeMusicFilter[] getFilters(){
		if (filterArrayJSON == null || filterArrayJSON.isEmpty()) return new CompositeMusicFilter[0];
		try {
			String[] compositeFilterStrings = JSONHelper.decodeStringArray(filterArrayJSON);
			CompositeMusicFilter[] filters = new CompositeMusicFilter[compositeFilterStrings.length];
			for (int i = 0; i < filters.length; i++){
				filters[i] = new CompositeMusicFilter(compositeFilterStrings[i]);
			}
			return filters;
		}catch(JSONException e){
			e.printStackTrace();
			return new CompositeMusicFilter[0];
		}
	}
	public Journey.Stage getStage(){
		return new Journey.Stage(id, name, playType, playCount, getConfig(), getFilters());
	}
}
