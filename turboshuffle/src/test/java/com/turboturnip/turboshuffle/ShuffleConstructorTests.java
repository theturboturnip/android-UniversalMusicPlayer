package com.turboturnip.turboshuffle;

import org.junit.Test;

public class ShuffleConstructorTests {

	@Test
	public void shuffle_CreateValidConfig_Works(){
		TestCommon.EvenConfig(5);
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
		TestCommon.RunShufflerNTimes(
				TestCommon.CreateTestCaseShuffler(TestCommon.EvenConfig(3), 3, 10, 120),
				TestCommon.CONFIG_TEST_SONGS
		);
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_ZeroPools_Throws(){
		new TurboShuffle(
				TestCommon.EvenConfig(1)
		);
	}
	@Test(expected=IllegalArgumentException.class)
	public void shuffle_ConfigInconsitency_Throws(){
		new TurboShuffle(
				TestCommon.EvenConfig(2),
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
				TestCommon.EvenConfig(2),
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
