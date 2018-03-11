package com.turboturnip.turnipmusic.model.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;

import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.model.db.entities.SongTagJoinEntity;
import com.turboturnip.turnipmusic.model.db.entities.TagEntity;

import java.util.List;

@Dao
public interface SongTagJoinEntityDao {
	@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
	@Query("SELECT * FROM " + DBConstants.TAG_TABLE + " INNER JOIN "+
			DBConstants.SONG_TAG_JOIN_TABLE+" ON "+
			DBConstants.TAG_TABLE+".id="+DBConstants.SONG_TAG_JOIN_TABLE+".tagId WHERE "+
			DBConstants.SONG_TAG_JOIN_TABLE+".songId=:songId"
	)
	List<TagEntity> getTagsForSong(int songId);
	@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
	@Query("SELECT id FROM " + DBConstants.SONG_TABLE + " INNER JOIN "+
			DBConstants.SONG_TAG_JOIN_TABLE+" ON "+
			DBConstants.SONG_TABLE+".id="+DBConstants.SONG_TAG_JOIN_TABLE+".songId WHERE "+
			DBConstants.SONG_TAG_JOIN_TABLE+".tagId=:tagId"
	)
	List<Integer> getSongIdsForTag(int tagId);

	@Insert()
	void insertJoin(SongTagJoinEntity join);

	@Query("DELETE FROM " + DBConstants.SONG_TAG_JOIN_TABLE)
	void clearDatabase();
}