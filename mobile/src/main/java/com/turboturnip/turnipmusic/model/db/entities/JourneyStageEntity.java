package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.turboturnip.turboshuffle.TurboShuffleConfig;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.db.Converters;
import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.utils.JSONHelper;

import org.json.JSONException;

@Entity(tableName = DBConstants.JOURNEY_STAGE_TABLE,
		foreignKeys = {
				@ForeignKey(entity = FilterEntity.class,
						parentColumns = "id",
						childColumns = "filterId"
				)
		}
)
public class JourneyStageEntity {
	@PrimaryKey
	public final int id;
	public final String name;
	@TypeConverters(Converters.class)
	public final Journey.Stage.PlayType playType;
	public final int playCount;
	public final String shuffleConfigJSON;
	public final int filterId;

	public JourneyStageEntity(Journey.Stage base, int filterId){
		this.id = base.name.hashCode();
		this.name = base.name;
		this.playType = base.playType;
		this.playCount = base.playCount;
		this.shuffleConfigJSON = JSONHelper.TurboShuffleConfigHandler.encode(base.shuffleConfig);
		this.filterId = filterId;
	}
	public JourneyStageEntity(int id, String name, Journey.Stage.PlayType playType, int playCount, String shuffleConfigJSON, int filterId){
		this.id = id;
		this.name = name;
		this.playType = playType;
		this.playCount = playCount;
		this.shuffleConfigJSON = shuffleConfigJSON;
		this.filterId = filterId;
	}
	public final TurboShuffleConfig getConfig(){
		try {
			return JSONHelper.TurboShuffleConfigHandler.decode(JSONHelper.typeCheckJSONObject(shuffleConfigJSON, JSONHelper.TurboShuffleConfigHandler.JSON_TYPE_VALUE));
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	public Journey.Stage getStage(MusicFilter filter){
		return new Journey.Stage(name, playType, playCount, getConfig(), filter);
	}
}
