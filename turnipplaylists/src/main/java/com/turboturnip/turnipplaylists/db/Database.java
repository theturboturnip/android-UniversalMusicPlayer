package com.turboturnip.turnipplaylists.db;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.turboturnip.turnipplaylists.db.daos.VideoEntityDao;
import com.turboturnip.turnipplaylists.db.entities.VideoEntity;

@android.arch.persistence.room.Database(entities = {VideoEntity.class},
		version = 1)
public abstract class Database extends RoomDatabase {

	private static volatile Database INSTANCE;

	public abstract VideoEntityDao videoEntityDao();

	public static Database getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (Database.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
							Database.class, "Videos.db")
							.build();
				}
			}
		}
		return INSTANCE;
	}
}

