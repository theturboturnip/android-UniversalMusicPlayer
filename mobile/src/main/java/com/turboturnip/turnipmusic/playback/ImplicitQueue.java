package com.turboturnip.turnipmusic.playback;

import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turboshuffle.SongPoolKey;
import com.turboturnip.turboshuffle.TurboShuffleSong;
import com.turboturnip.turnipmusic.model.Song;

import java.util.HashMap;
import java.util.Map;

public abstract class ImplicitQueue {

	protected SongPool[] pools;
	protected Map<Song, SongPoolKey> keys;

	SongPoolKey getKeyForSong(Song song){
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

		return keyForSong;
	}

	void initialize(SongPool[] pools){
		this.pools = pools;
		if (this.pools.length == 0)
			throw new IllegalArgumentException("Tried to create an OrderedImplicitQueue with no pools!");
		this.keys = new HashMap<>();
	}

	abstract void onSongPlayed(Song song);
	abstract Song nextSong();
}
