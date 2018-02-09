package com.turboturnip.turnipmusic.model.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface SongTagDao {

	/**
	 * Get the tags from the table. Since for simplicity we only have one tags in the database,
	 * this query gets all tagss from the table, but limits the result to just the 1st tags.
	 *
	 * @return the tags from the table
	 */
	@Query("SELECT * FROM Tags WHERE media_id LIKE :mediaId LIMIT 1")
	SongTags getTags(String mediaId);

	/**
	 * Insert a tags in the database. If the tags already exists, replace it.
	 *
	 * @param tags the tags to be inserted.
	 */
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertTags(SongTags tags);

	/**
	 * Delete all tagss.
	 */
	@Query("DELETE FROM Tags")
	void deleteAllSongTags();
}
