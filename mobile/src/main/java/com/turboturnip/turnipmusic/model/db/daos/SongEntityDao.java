package com.turboturnip.turnipmusic.model.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.turboturnip.turnipmusic.model.db.DBConstants;
import com.turboturnip.turnipmusic.model.db.entities.SongEntity;

import java.util.List;

@Dao
public interface SongEntityDao {
	@Query("SELECT * FROM " + DBConstants.SONG_TABLE + " WHERE id=:id")
	SongEntity getSong(int id);
	@Query("SELECT * FROM " + DBConstants.SONG_TABLE + " WHERE mediaId=:mediaId")
	SongEntity getSongByMediaId(String mediaId);
	@Query("SELECT id FROM " + DBConstants.SONG_TABLE + " WHERE name LIKE :name")
	Integer getSongIdByName(String name);
	@Query("SELECT * FROM " + DBConstants.SONG_TABLE + " WHERE albumId=:albumId ORDER BY albumIndex")
	List<SongEntity> getSongsInAlbum(int albumId);
	@Query("SELECT id FROM " + DBConstants.SONG_TABLE + " WHERE albumId=:albumId ORDER BY albumIndex")
	List<Integer> getSongIdsInAlbum(int albumId);
	//@Query("SELECT mediaId FROM " + DBConstants.SONG_TABLE + " WHERE artist LIKE :artist")
	@Query("SELECT id FROM " + DBConstants.SONG_TABLE + " WHERE name LIKE '%' || :query || '%' ORDER BY INSTR(name, :query)")
	List<Integer> orderedSearchForIds(String query);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertSong(SongEntity song);

	@Query("DELETE FROM " + DBConstants.SONG_TABLE)
	void clearDatabase();
}