package com.turboturnip.turnipmusic.playback;

import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turboshuffle.SongPoolKey;
import com.turboturnip.turboshuffle.TurboShuffle;
import com.turboturnip.turnipmusic.model.Song;

import java.util.Random;

public class ShuffledImplicitQueue extends ImplicitQueue {

	private TurboShuffle shuffler;
	private TurboShuffle.State currentState;
	private Random rng;

	void initialize(SongPool[] pools){
		super.initialize(pools);
		float[] weights = new float[pools.length];
		for (int i = 0; i < weights.length; i++)
			weights[i] = 5.0f;
		TurboShuffle.Config config = new TurboShuffle.Config(
				0.5f, 1.0f, 0.8f, 0.0f, 10.0f, TurboShuffle.Config.ProbabilityMode.BySong, 100, weights
		);
		shuffler = new TurboShuffle(config, pools);
		currentState = shuffler.new State();
		rng = new Random();
	}
	void onSongPlayed(Song song){
		SongPoolKey key = getKeyForSong(song);
		if (key != null)
			currentState.IncrementHistory(key);
	}
	Song nextSong(){
		return (Song)shuffler.NextSong(currentState, rng);
	}
}
