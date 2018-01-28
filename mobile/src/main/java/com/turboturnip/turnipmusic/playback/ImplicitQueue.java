package com.turboturnip.turnipmusic.playback;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.utils.LogHelper;

public class ImplicitQueue {
	public static final String TAG = LogHelper.makeLogTag(ImplicitQueue.class);

	private MusicFilter pool;
	private int lastPlayedIndex = -1;

	public ImplicitQueue(MusicFilter initialPool){
		changePool(initialPool);
	}

	// This is called whenever a song is played.
	public void onIndexPlayed(int explicitIndex){
		lastPlayedIndex = explicitIndex;
	}
	public int nextIndex(MusicProvider provider){
		int newIndex = lastPlayedIndex + 1;
		while (newIndex < provider.songCount()){
			if (pool.accepts(provider.getMusic(newIndex)))
				return newIndex;
		}
		newIndex = 0;
		while (newIndex <= lastPlayedIndex){
			if (pool.accepts(provider.getMusic(newIndex)))
				return newIndex;
		}
		LogHelper.e(TAG, "No songs found that match the implicit queue!");
		return -1;
	}
	public void changePool(MusicFilter newPool){
		pool = newPool;
	}
}
