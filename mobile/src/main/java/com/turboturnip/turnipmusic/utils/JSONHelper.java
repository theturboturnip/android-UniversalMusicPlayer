package com.turboturnip.turnipmusic.utils;

import com.turboturnip.turboshuffle.TurboShuffleConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

public class JSONHelper {
	private static final String TAG = LogHelper.makeLogTag(JSONHelper.class);

	public static final String JSON_TYPE_KEY = "type";

	public static JSONObject typeCheckJSONObject(String json, String expectedType) throws JSONException{
		Object o = new JSONTokener(json).nextValue();
		if (!(o instanceof JSONObject))
			throw new JSONException("Given data was not valid JSON.");
		JSONObject decodedObject = (JSONObject) o;
		typeCheckJSONObject(decodedObject, expectedType);
		return decodedObject;
	}
	public static void typeCheckJSONObject(JSONObject object, String expectedType) throws JSONException{
		if (!object.has(JSON_TYPE_KEY))
			throw new JSONException("Given JSON doesn't have a type field, which is required.");
		if (object.getString(JSON_TYPE_KEY).compareTo(expectedType) != 0)
			throw new JSONException("Given JSON has incorrect type, '" + expectedType + "' is required.");
	}

	public static class TurboShuffleConfigHandler {
		private static final String JSON_TYPE_VALUE = "TSConfig";
		private static final String JSON_CLUMP_TYPE_KEY = "clumpType";
		private static final String JSON_CLUMP_SEVERITY_KEY = "clumpSeverity";
		private static final String JSON_CLUMP_WEIGHT_KEY = "clumpWeight";
		private static final String JSON_CLUMP_LENGTH_SEVERITY_KEY = "clumpLengthSeverity";
		private static final String JSON_HISTORY_SEVERITY_KEY = "historySeverity";
		private static final String JSON_PROBABILITY_MODE_KEY = "probabilityMode";
		private static final String JSON_MAX_MEMORY_KEY = "maxMemory";
		private static final String JSON_POOL_WEIGHTS_KEY = "poolWeights";

		public static void encode(TurboShuffleConfig config, JSONStringer stringer) throws JSONException{
			if (config == null){
				stringer.value(null);
				return;
			}
			stringer.object().key(JSON_TYPE_KEY).value(JSON_TYPE_VALUE);
			stringer.key(JSON_CLUMP_TYPE_KEY).value(config.clumpType);
			stringer.key(JSON_CLUMP_SEVERITY_KEY).value(config.clumpSeverity);
			stringer.key(JSON_CLUMP_WEIGHT_KEY).value(config.clumpWeight);
			stringer.key(JSON_CLUMP_LENGTH_SEVERITY_KEY).value(config.clumpLengthSeverity);
			stringer.key(JSON_HISTORY_SEVERITY_KEY).value(config.historySeverity);
			stringer.key(JSON_PROBABILITY_MODE_KEY).value(config.probabilityMode.toString());
			stringer.key(JSON_MAX_MEMORY_KEY).value(config.maximumMemory);
			stringer.key(JSON_POOL_WEIGHTS_KEY).array();
			for (float w : config.poolWeights)
				stringer.value(w);
			stringer.endArray();
			stringer.endObject();
		}
		public static TurboShuffleConfig decode(JSONObject object) throws JSONException{
			typeCheckJSONObject(object, JSON_TYPE_VALUE);
			JSONArray jsonPoolWeights = object.getJSONArray(JSON_POOL_WEIGHTS_KEY);
			float[] poolWeights = new float[jsonPoolWeights.length()];
			for (int i = 0; i < poolWeights.length; i++){
				poolWeights[i] = (float)jsonPoolWeights.getDouble(i);
			}
			return new TurboShuffleConfig(
					(float)object.getDouble(JSON_CLUMP_TYPE_KEY),
					(float)object.getDouble(JSON_CLUMP_SEVERITY_KEY),
					(float)object.getDouble(JSON_CLUMP_WEIGHT_KEY),
					(float)object.getDouble(JSON_CLUMP_LENGTH_SEVERITY_KEY),
					(float)object.getDouble(JSON_HISTORY_SEVERITY_KEY),
					TurboShuffleConfig.ProbabilityMode.valueFor(object.getString(JSON_PROBABILITY_MODE_KEY)),
					object.getInt(JSON_MAX_MEMORY_KEY),
					poolWeights
			);
		}
	}
}
