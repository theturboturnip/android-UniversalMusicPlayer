package com.turboturnip.turnipmusic.model.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.db.DBConstants;

@Entity(tableName = DBConstants.JOURNEY_TABLE)
public class JourneyEntity {
	@PrimaryKey
	public final int id;
	public final String name;

	public JourneyEntity(Journey base){
		this.id = base.name.hashCode();
		this.name = base.name;
	}
	public JourneyEntity(int id, String name){
		this.id = id;
		this.name = name;
	}
}
