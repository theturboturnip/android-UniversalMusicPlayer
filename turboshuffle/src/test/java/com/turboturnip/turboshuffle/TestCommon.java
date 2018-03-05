package com.turboturnip.turboshuffle;

import java.util.Random;

class TestCommon {
	public static final int CONFIG_TEST_REPS = 100;
	public static final int CONFIG_TEST_SONGS = 1000;

	static TurboShuffle.Config EvenConfig(int poolCount){
		return new TurboShuffle.Config(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, TurboShuffle.Config.ProbabilityMode.BySong, 1,
				TestCommon.EvenWeights(poolCount));
	}
	static float[] EvenWeights(int poolCount){
		float[] weights = new float[poolCount];
		while(poolCount > 0){
			weights[poolCount - 1] = 1.0f;
			poolCount--;
		}
		return weights;
	}
	static float[] UnevenWeights(int poolCount){
		float[] weights = new float[poolCount];
		while(poolCount > 0){
			weights[poolCount - 1] = poolCount;
			poolCount--;
		}
		return weights;
	}
	static TurboShuffle.SongPool[] CreateTestCasePools(int pools, int songsPerPool, int songLength){
		TurboShuffle.SongPool[] toReturn = new TurboShuffle.SongPool[pools];
		int songId = 0;
		for (int i = 0; i < pools; i++){
			TurboShuffleSong[] songs = new TurboShuffleSong[songsPerPool];
			for (int j = 0; j < songsPerPool; j++) {
				songs[j] = new TestShuffleSong(songId, songLength);
				songId++;
			}
			toReturn[i] = new TurboShuffle.SongPool(songs);
		}

		return toReturn;
	}
	static TurboShuffle CreateTestCaseShuffler(TurboShuffle.Config config, int pools, int songsPerPool, int songLength){
		return new TurboShuffle(config, CreateTestCasePools(pools, songsPerPool, songLength));
	}
	static TurboShuffle.State RunShufflerNTimes(TurboShuffle shuffler, int N){
		TurboShuffle.State state = shuffler.new State();
		Random rng = new Random();
		for (int i = 0; i < N; i++){
			TurboShuffle.SongPoolKey nextKey = shuffler.NextKey(state, rng);
			state.IncrementHistory(nextKey);
		}
		return state;
	}
}
