package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = { SongEntity.class, TagEntity.class, SongTagJoinEntity.class, AlbumEntity.class },
		version = 1)
public abstract class SongDatabase extends RoomDatabase {

	private static volatile SongDatabase INSTANCE;

	public abstract SongEntityDao songDao();
	public abstract TagEntityDao tagDao();
	public abstract SongTagJoinEntityDao songTagJoinDao();
	public abstract AlbumEntityDao albumDao();

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

}