package com.turboturnip.turnipmusic.backend.queue;

import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turboshuffle.SongPoolKey;
import com.turboturnip.turnipmusic.model.Song;

public class OrderedImplicitQueue extends ImplicitQueue {

	private int currentPool = -1, currentSong = -1;

	public void initialize(SongPool[] pools){
		super.initialize(pools);
		this.currentPool = 0;
		this.currentSong = -1;
	}
	public Song nextSong(){
		int newPool = currentPool;
		int newSong = currentSong + 1;
		if (newSong >= pools[currentPool].songs.size()) {
			newPool++;
			newSong = 0;
			if (newPool >= pools.length) newPool = 0;
		}
		Song toPlay = (Song)pools[newPool].songs.get(newSong);
		keys.put(toPlay, new SongPoolKey(newPool, newSong));
		return toPlay;
	}
	public void onSongPlayed(Song song){
		SongPoolKey keyForSong = getKeyForSong(song);

		if (keyForSong == null) return;
		currentPool = keyForSong.poolIndex;
		currentSong = keyForSong.songIndexInPool;
	}
}
