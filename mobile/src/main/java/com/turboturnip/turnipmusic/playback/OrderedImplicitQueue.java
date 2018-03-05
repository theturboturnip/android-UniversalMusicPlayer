package com.turboturnip.turnipmusic.playback;

import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turboshuffle.SongPoolKey;
import com.turboturnip.turboshuffle.TurboShuffleSong;
import com.turboturnip.turnipmusic.model.Song;

import java.util.HashMap;
import java.util.Map;

public class OrderedImplicitQueue implements ImplicitQueue {

	private SongPool[] pools;
	private int currentPool = -1, currentSong = -1;
	private Map<TurboShuffleSong, SongPoolKey> keys;

	public void initialize(SongPool[] pools){
		this.pools = pools;
		if (this.pools.length == 0)
			throw new IllegalArgumentException("Tried to create an OrderedImplicitQueue with no pools!");
		this.currentPool = 0;
		this.currentSong = 0;
		this.keys = new HashMap<>();
	}
	public Song nextSong(){
		int newPool = currentPool, newSong = currentSong + 1;
		if (newSong >= pools[currentPool].songs.size()) {
			newPool++;
			newSong = 0;
			if (newPool >= pools.length) newPool = 0;
		}
		TurboShuffleSong toPlay = pools[newPool].songs.get(newSong);
		keys.put(toPlay, new SongPoolKey(newPool, newSong));
		return (Song)toPlay;
	}
	public void onSongPlayed(Song song){
		SongPoolKey keyForSong = null;
		if (keys.containsKey(song)) {
			keyForSong = keys.get(song);
		}else{
			int poolIndex = 0, songIndexInPool = 0;
			for(poolIndex = 0; poolIndex < pools.length; poolIndex++){
				for(songIndexInPool = 0; songIndexInPool < pools[poolIndex].songs.size(); songIndexInPool++){
					if (pools[poolIndex].songs.get(songIndexInPool).getId().equals(song.getId()))
						break;
				}
				if (songIndexInPool < pools[poolIndex].songs.size())
					break;
			}
			if (poolIndex < pools.length){
				keyForSong = new SongPoolKey(poolIndex, songIndexInPool);
				keys.put(song, keyForSong);
			}
		}

		if (keyForSong == null) return;
		currentPool = keyForSong.poolIndex;
		currentSong = keyForSong.songIndexInPool;
	}
}
