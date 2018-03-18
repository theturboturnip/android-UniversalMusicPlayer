package com.turboturnip.turnipmusic.model;

import com.turboturnip.turboshuffle.TurboShuffleConfig;
import com.turboturnip.turnipmusic.utils.JSONHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.HashMap;
import java.util.Map;

public class Journey {
	public static final String JSON_TYPE_VALUE = "Journey";
	public static final String JSON_NAME_KEY = "name";
	public static final String JSON_STAGES_KEY = "stages";

	public static class Stage {
		public static final String JSON_PLAYTYPE_KEY = "playType";
		public static final String JSON_PLAYCOUNT_KEY = "playCount";
		public static final String JSON_SHUFFLECONFIG_KEY = "shuffleConfig";
		public static final String JSON_FILTERS_KEY = "filters";
		public static final String JSON_TYPE_VALUE = "Stage";

		public enum PlayType {
			Repeat ("REPEAT"),
			Shuffle ("SHUFFLE");

			private final String name;

			private static final Map<String, PlayType> map = new HashMap<>();
			static {
				for (PlayType en : values()) {
					map.put(en.name, en);
				}
			}

			public static PlayType valueFor(String name) {
				return map.get(name);
			}

			PlayType(String s) {
				name = s;
			}

			public boolean equals(String otherName) {
				// (otherName == null) check is not needed because name.equals(null) returns false
				return name.equals(otherName);
			}

			public String toString() {
				return this.name;
			}
		}
		public String name;
		public PlayType playType;
		public int playCount;
		public TurboShuffleConfig shuffleConfig;
		public MusicFilter[] filters;

		public Stage(String name, PlayType playType, int playCount, TurboShuffleConfig shuffleConfig, MusicFilter... filters){
			this.name = name;
			this.playType = playType;
			this.playCount = playCount;
			this.shuffleConfig = shuffleConfig;
			this.filters = filters;
		}
		public Stage(String json) throws JSONException{
			this(JSONHelper.typeCheckJSONObject(json, JSON_TYPE_VALUE));
		}
		public Stage(JSONObject sourceObject) throws JSONException{
			JSONHelper.typeCheckJSONObject(sourceObject, JSON_TYPE_VALUE);
			name = sourceObject.getString(JSON_NAME_KEY);
			playType = PlayType.valueFor(sourceObject.getString(JSON_PLAYTYPE_KEY));
			playCount = sourceObject.getInt(JSON_PLAYCOUNT_KEY);
			if (!sourceObject.isNull(JSON_SHUFFLECONFIG_KEY))
				shuffleConfig = JSONHelper.TurboShuffleConfigHandler.decode(sourceObject.getJSONObject(JSON_SHUFFLECONFIG_KEY));
			else
				shuffleConfig = null;
			JSONArray jsonFilters = sourceObject.getJSONArray(JSON_FILTERS_KEY);
			filters = new MusicFilter[jsonFilters.length()];
			for (int i = 0; i < jsonFilters.length(); i++)
				filters[i] = new MusicFilter(jsonFilters.getString(i));
		}
		public void encodeAsJson(JSONStringer stringer) throws JSONException{
			stringer.object();
			stringer.key(JSONHelper.JSON_TYPE_KEY).value(JSON_TYPE_VALUE);
			stringer.key(JSON_NAME_KEY).value(name);
			stringer.key(JSON_PLAYTYPE_KEY).value(playType.toString());
			stringer.key(JSON_PLAYCOUNT_KEY).value(playCount);
			if (shuffleConfig != null) {
				stringer.key(JSON_SHUFFLECONFIG_KEY);
				JSONHelper.TurboShuffleConfigHandler.encode(shuffleConfig, stringer);
			}
			stringer.key(JSON_FILTERS_KEY).array();
			for (MusicFilter f : filters){
				stringer.value(f.toString());
			}
			stringer.endArray();
			stringer.endObject();
		}
		@Override
		public String toString(){
			try{
				JSONStringer stringer = new JSONStringer();
				encodeAsJson(stringer);
				return stringer.toString();
			}catch(JSONException e){
				e.printStackTrace();
				return "null";
			}
		}
	}
	public String name;
	public Stage[] stages;

	public Journey(String name, MusicFilter singleFilter){
		this.name = name;
		stages = new Stage[]{
			new Stage("", Stage.PlayType.Repeat, 0, null, singleFilter)
		};
	}
	public Journey(String json) throws JSONException{
		JSONObject decodedObject = JSONHelper.typeCheckJSONObject(json, JSON_TYPE_VALUE);
		name = decodedObject.getString(JSON_NAME_KEY);
		JSONArray jsonStages = decodedObject.getJSONArray(JSON_STAGES_KEY);
		stages = new Stage[jsonStages.length()];
		for (int i = 0; i < jsonStages.length(); i++){
			stages[i] = new Stage(jsonStages.getJSONObject(i));
		}
	}
	public Journey(String name, Stage... stages){
		this.name = name;
		this.stages = stages;
	}

	@Override
	public String toString(){
		try {
			JSONStringer stringer = new JSONStringer();
			stringer.object().key(JSONHelper.JSON_TYPE_KEY).value(JSON_TYPE_VALUE);
			stringer.key(JSON_NAME_KEY).value(name);
			stringer.key(JSON_STAGES_KEY).array();
			for (Stage s : stages) {
				s.encodeAsJson(stringer);
			}
			stringer.endArray();
			stringer.endObject();
			return stringer.toString();
		}catch(JSONException e){
			e.printStackTrace();
			return "";
		}
	}
}
