package com.turboturnip.turnipmusic.model;

import com.turboturnip.turnipmusic.model.Journey;

import org.json.JSONException;

public class ConstJourney{
	public final Journey.Stage[] stages;

	public ConstJourney(Journey base){
		this.stages = base.stages;
	}
	public ConstJourney(String json) throws JSONException{
		this(new Journey(json));
	}
}
