package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SongTags.class}, version = 1)
public abstract class SongTagDatabase extends RoomDatabase {

	private static volatile SongTagDatabase INSTANCE;

	public abstract SongTagDao tagDao();

	public static SongTagDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SongTagDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
							SongTagDatabase.class, "Tags.db")
							.build();
				}
			}
		}
		return INSTANCE;
	}

}