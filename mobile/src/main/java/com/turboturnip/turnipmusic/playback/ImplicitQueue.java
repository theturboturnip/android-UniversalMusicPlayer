package com.turboturnip.turnipmusic.playback;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ImplicitQueue {
	public static final String TAG = LogHelper.makeLogTag(ImplicitQueue.class);

	private List<Integer> mOrderedPool;
	private MusicProvider mMusicProvider;
	private int lastPlayedIndexInPool = -1;

	public ImplicitQueue(MusicProvider provider){
		mMusicProvider = provider;
	}

	// This is called whenever a song is played.
	public void onIndexPlayed(int explicitIndex){
		lastPlayedIndexInPool = mOrderedPool.indexOf(explicitIndex);
	}
	public int nextIndex(MusicProvider provider){
		if (lastPlayedIndexInPool + 1 >= mOrderedPool.size())
			return mOrderedPool.get(0);
		return mOrderedPool.get(lastPlayedIndexInPool + 1);
	}
	public void changePool(MusicFilter newFilter){
		mOrderedPool = mMusicProvider.getFilteredSongIndices(newFilter);
		lastPlayedIndexInPool = -1;
		if (mOrderedPool.size() == 0)
			throw new RuntimeException("MusicFilter " + newFilter.toString() + " doesn't have any songs in the pool!");
	}
}
