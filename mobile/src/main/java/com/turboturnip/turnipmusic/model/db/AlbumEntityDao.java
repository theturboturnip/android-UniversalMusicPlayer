package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface AlbumEntityDao {
	@Query("SELECT * FROM " + DBConstants.ALBUM_TABLE)
	List<AlbumEntity> getAlbums();
	@Query("SELECT * FROM " + DBConstants.ALBUM_TABLE + " WHERE name LIKE :name")
	AlbumEntity getAlbumByName(String name);
	@Query("SELECT * FROM " + DBConstants.ALBUM_TABLE + " WHERE id=:id")
	AlbumEntity getAlbumById(int id);
	@Query("SELECT * FROM " + DBConstants.ALBUM_TABLE + " WHERE name LIKE '%' || :query || '%' ORDER BY INSTR(name, :query)")
	List<AlbumEntity> orderedSearch(String query);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertAlbum(AlbumEntity album);

	@Query("DELETE FROM " + DBConstants.SONG_TABLE)
	void clearDatabase();
}
