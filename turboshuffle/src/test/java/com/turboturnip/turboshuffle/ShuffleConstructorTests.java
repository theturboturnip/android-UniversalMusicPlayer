package com.turboturnip.turboshuffle;

import org.junit.Test;

public class ShuffleConstructorTests {

	private TurboShuffle.Config EvenConfig(int poolCount){
		float[] weights = new float[poolCount];
		while(poolCount > 0){
			weights[poolCount - 1] = 1.0f;
			poolCount--;
		}
		return new TurboShuffle.Config(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, TurboShuffle.Config.ProbabilityMode.ByLength, 1, weights);
	}
	@Test
	public void shuffle_CreateValidConfig_Works(){
		EvenConfig(5);
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_Config_NoWeightThrows(){
		new TurboShuffle.Config(0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				TurboShuffle.Config.ProbabilityMode.ByLength, 1, new float[0]);
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_Config_ZeroWeightThrows(){
		new TurboShuffle.Config(0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				TurboShuffle.Config.ProbabilityMode.ByLength, 1, new float[]{1.0f, 1.0f, 0.0f});
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_Config_SubZeroWeightThrows(){
		new TurboShuffle.Config(0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				TurboShuffle.Config.ProbabilityMode.ByLength, 1, new float[]{1.0f, 1.0f, -0.1f});
	}

	@Test
	public void shuffle_CreateShuffler_Works(){
		new TurboShuffle(
				EvenConfig(1),
				new TurboShuffle.SongPool(
						new TestShuffleSong(0, 0),
						new TestShuffleSong(1, 0),
						new TestShuffleSong(2, 0)
				)
		);
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_ZeroPools_Throws(){
		new TurboShuffle(
				EvenConfig(1)
		);
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_ConfigInconsitency_Throws(){
		new TurboShuffle(
				EvenConfig(2),
				new TurboShuffle.SongPool(
						new TestShuffleSong(0, 0),
						new TestShuffleSong(1, 0),
						new TestShuffleSong(2, 0)
				)
		);
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_PoolOverlap_Throws(){
		new TurboShuffle(
				EvenConfig(2),
				new TurboShuffle.SongPool(
						new TestShuffleSong(0, 0),
						new TestShuffleSong(1, 0),
						new TestShuffleSong(2, 0)
				),
				new TurboShuffle.SongPool(
						new TestShuffleSong(2, 0),
						new TestShuffleSong(3, 0),
						new TestShuffleSong(4, 0)
				)
		);
	}
}
