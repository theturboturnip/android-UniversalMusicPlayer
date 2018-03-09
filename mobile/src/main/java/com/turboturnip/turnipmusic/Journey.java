package com.turboturnip.turnipmusic;

import com.turboturnip.turboshuffle.TurboShuffle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class Journey {
	public static final String JSON_TYPE_VALUE = "Journey";
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
		public PlayType playType;
		public int playCount;
		public TurboShuffle.Config shuffleConfig;
		public MusicFilter[] filters;

		public Stage(PlayType playType, int playCount, TurboShuffle.Config shuffleConfig, MusicFilter... filters){
			this.playType = playType;
			this.playCount = playCount;
			this.shuffleConfig = shuffleConfig;
			this.filters = filters;
		}
		public Stage(JSONObject sourceObject) throws JSONException{
			if (!sourceObject.has("type"))
				throw new JSONException("Given JSON doesn't have a type field, which is required.");
			if (sourceObject.getString("type").compareTo(JSON_TYPE_VALUE) != 0)
				throw new JSONException("Given JSON has incorrect type, '" + JSON_TYPE_VALUE + "' is required.");
			playType = PlayType.valueFor(sourceObject.getString(JSON_PLAYTYPE_KEY));
			playCount = sourceObject.getInt(JSON_PLAYCOUNT_KEY);
			//shuffleConfig = ;
			JSONArray jsonFilters = sourceObject.getJSONArray(JSON_FILTERS_KEY);
			filters = new MusicFilter[jsonFilters.length()];
			for (int i = 0; i < jsonFilters.length(); i++)
				filters[i] = new MusicFilter(jsonFilters.getString(i));
		}
		public JSONStringer encodeAsJson(JSONStringer stringer) throws JSONException{
			stringer.object();
			stringer.key("type").value(JSON_TYPE_VALUE);
			stringer.key(JSON_PLAYTYPE_KEY).value(playType.toString());
			stringer.key(JSON_PLAYCOUNT_KEY).value(playCount);
			//stringer.key(JSON_SHUFFLECONFIG_KEY).value(shuffleConfig);
			stringer.key(JSON_FILTERS_KEY).array();
			for (MusicFilter f : filters){
				stringer.value(f.toString());
			}
			stringer.endArray();
			stringer.endObject();
			return stringer;
		}
	}
	public Stage[] stages;

	public Journey(MusicFilter singleFilter){
		stages = new Stage[]{
			new Stage(Stage.PlayType.Repeat, 0, null, singleFilter)
		};
	}
	public Journey(String json) throws JSONException{
		Object o = new JSONTokener(json).nextValue();
		if (!o.getClass().isAssignableFrom(JSONObject.class))
			throw new JSONException("Given data was not valid JSON.");
		JSONObject decodedObject = (JSONObject) o;
		if (!decodedObject.has("type"))
			throw new JSONException("Given JSON does not have a type field, which is required!");
		if (decodedObject.getString("type").compareTo(JSON_TYPE_VALUE) != 0)
			throw new JSONException("Given JSON has an incorrect type, '" + JSON_TYPE_VALUE + "' is required.");
		JSONArray jsonStages = decodedObject.getJSONArray(JSON_STAGES_KEY);
		stages = new Stage[jsonStages.length()];
		for (int i = 0; i < jsonStages.length(); i++){
			stages[i] = new Stage(jsonStages.getJSONObject(i));
		}
	}
	public Journey(Stage... stages){
		this.stages = stages;
	}

	@Override
	public String toString(){
		try {
			JSONStringer stringer = new JSONStringer();
			stringer.object().key("type").value(JSON_TYPE_VALUE);
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
