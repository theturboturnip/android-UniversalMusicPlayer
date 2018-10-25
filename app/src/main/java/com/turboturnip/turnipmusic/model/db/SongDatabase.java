package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.turboturnip.turnipmusic.model.db.daos.TagEntityDao;
import com.turboturnip.turnipmusic.model.db.daos.TagMapEntityDao;
import com.turboturnip.turnipmusic.model.db.entities.TagMapEntity;
import com.turboturnip.turnipmusic.model.db.entities.TagEntity;

@Database(entities = { TagEntity.class, TagMapEntity.class },
		version = 1)
public abstract class SongDatabase extends RoomDatabase {

	private static volatile SongDatabase INSTANCE;

	public abstract TagEntityDao tagDao();
	public abstract TagMapEntityDao tagMapDao();

	public static SongDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (SongDatabase.class) {
				if (INSTANCE == null) {
					INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
							SongDatabase.class, "Songs.db")
							.build();
				}
			}
		}
		return INSTANCE;
	}

	// TODO
	public void clean(){
		// Remove tags without songs
	}
	// TODO
	public void clearAllDatabases(){

	}
}