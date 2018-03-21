package com.turboturnip.turnipmusic.model;

import org.json.JSONException;

public class ConstJourney{
	public final String name;
	public final Journey.Stage[] stages;

	public ConstJourney(Journey base){
		this.name = base.name;
		this.stages = base.stages.toArray(new Journey.Stage[base.stages.size()]);
	}
	public ConstJourney(String json) throws JSONException{
		this(new Journey(json));
	}
}
