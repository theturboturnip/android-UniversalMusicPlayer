package com.turboturnip.turboshuffle;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AlgorithmTests {

	private static class TestResults {
		private static class SingleResult {
			Map<SongPoolKey, Integer> songOccurrences;
			int[] poolOccurences;
			float unclumpiness;
			float poolRatioDifference;

			SingleResult(TurboShuffle.Config config, TurboShuffle.State finalState) {
				songOccurrences = finalState.songOccurrences;
				poolOccurences = finalState.poolOccurrences;

				int totalChanges = 0;
				for (int i = 1; i < finalState.songHistory.size(); i++){
					if (finalState.songHistory.get(i).poolIndex != finalState.songHistory.get(i - 1).poolIndex)
						totalChanges++;
				}
				unclumpiness = (float)totalChanges / (float)finalState.songHistory.size();

				poolRatioDifference = 0;
				for (int j = 0; j < config.poolWeights.length; j++) {
					float expected = config.sumToOnePoolWeights[j] * (finalState.songHistory.size());
					float actual = finalState.poolOccurrences[j];
					float chiSquared = (float) Math.pow(1 - actual / expected, 2);
					poolRatioDifference += chiSquared;
				}
			}
		}

		List<SingleResult> results;
		Map<SongPoolKey, Double> songOccurrenceMeans, songOccurrenceStdDevs;
		double[] poolOccurenceMeans, poolOccurenceStdDevs;
		SummaryStatistics unclumpinessStats;
		SummaryStatistics poolRatioDifferenceStats;

		public TestResults(SingleResult... results){
			this.results = Arrays.asList(results);
			poolOccurenceMeans = new double[results[0].poolOccurences.length];
			poolOccurenceStdDevs = new double[results[0].poolOccurences.length];
			for(int i = 0; i < poolOccurenceMeans.length; i++){
				SummaryStatistics stats = new SummaryStatistics();
				for (SingleResult result : results){
					stats.addValue(result.poolOccurences[i]);
				}
				poolOccurenceMeans[i] = stats.getMean();
				poolOccurenceStdDevs[i] = stats.getStandardDeviation();
			}

			songOccurrenceMeans = new HashMap<>();
			songOccurrenceStdDevs = new HashMap<>();
			for(SongPoolKey key : results[0].songOccurrences.keySet()){
				SummaryStatistics stats = new SummaryStatistics();
				for (SingleResult result : results){
					stats.addValue(result.songOccurrences.getOrDefault(key, 0));
				}
				songOccurrenceMeans.put(key, stats.getMean());
				songOccurrenceStdDevs.put(key, stats.getStandardDeviation());
			}

			unclumpinessStats = new SummaryStatistics();
			for(SingleResult result : results){
				unclumpinessStats.addValue(result.unclumpiness);
			}

			poolRatioDifferenceStats = new SummaryStatistics();
			for(SingleResult result : results) {
				poolRatioDifferenceStats.addValue(result.poolRatioDifference);
			}
		}

		@Override
		public String toString(){
			return toString(true);
		}
		public String toString(boolean printSongOccurrences){
			StringBuilder resultBuilder = new StringBuilder("Result:");
			resultBuilder.append("\n\tPool Occurrences:");
			for (int i = 0; i < poolOccurenceMeans.length; i++) {
				resultBuilder.append("\n\t\tMean: ");
				resultBuilder.append(poolOccurenceMeans[i]);
				resultBuilder.append("\n\t\tStd-Dev: ");
				resultBuilder.append(poolOccurenceStdDevs[i]);
			}
			resultBuilder.append("\n\tPool Ratio Difference:");
			resultBuilder.append("\n\t\tMean: ");
			resultBuilder.append(poolRatioDifferenceStats.getMean());
			resultBuilder.append("\n\t\tStd-Dev: ");
			resultBuilder.append(poolRatioDifferenceStats.getStandardDeviation());
			if (printSongOccurrences) {
				resultBuilder.append("\n\tSong Occurrences:");
				for (SongPoolKey key : songOccurrenceMeans.keySet()) {
					resultBuilder.append("\n\t\tMean: ");
					resultBuilder.append(songOccurrenceMeans.get(key));
					resultBuilder.append("\n\t\tStd-Dev: ");
					resultBuilder.append(songOccurrenceStdDevs.get(key));
				}
			}
			resultBuilder.append("\n\tUnclumpiness:");
			resultBuilder.append("\n\t\tMean: ");
			resultBuilder.append(unclumpinessStats.getMean());
			resultBuilder.append("\n\t\tStd-Dev: ");
			resultBuilder.append(unclumpinessStats.getStandardDeviation());

			return resultBuilder.toString();
		}
	}


	private TestResults RunShufflerXTimesWithYReps(TurboShuffle shuffler, int X, int reps){
		TestResults.SingleResult[] results = new TestResults.SingleResult[reps];
		for (int i = 0; i < reps; i++){
			results[i] = new TestResults.SingleResult(shuffler.config, TestCommon.RunShufflerNTimes(shuffler, X));
		}
		return new TestResults(results);
	}
	private TestResults[] TestConfigVariable(String varName, float[] values, TurboShuffle.Config base, int poolCount, int songsPerPool, int songLength){
		Field varField;
		try {
			varField = TurboShuffle.Config.class.getField(varName);
			varField.setAccessible(true);
		}catch (NoSuchFieldException e){
			System.out.println("No Such Field!");
			return null;
		}

		TestResults[] results = new TestResults[values.length];
		for (int i = 0; i < values.length; i++){
			try {
				varField.setFloat(base, values[i]);
			}catch(IllegalAccessException e){
				System.out.println("IllegalAccessException for "+varName);
			}
			TurboShuffle shuffler = TestCommon.CreateTestCaseShuffler(base, poolCount, songsPerPool, songLength);
			results[i] = RunShufflerXTimesWithYReps(shuffler, TestCommon.CONFIG_TEST_SONGS, TestCommon.CONFIG_TEST_REPS);
		}
		return results;
	}

	@Test
	public void shuffle_Run1000Times10Reps(){
		TurboShuffle.Config config = new TurboShuffle.Config(
				0.5f, 0f, 0f, 0f, 50f,
				TurboShuffle.Config.ProbabilityMode.BySong, 1000, TestCommon.UnevenWeights(3)
		);
		TurboShuffle shuffler = TestCommon.CreateTestCaseShuffler(config, 3, 3, 120);
		System.out.println(RunShufflerXTimesWithYReps(shuffler, 1000, 10));
	}
	@Test
	public void shuffle_TestClumpType(){
		final int runsPerTest = 1000, poolCount = 3, songsPerPool = 3, songLength = 120;

		TurboShuffle.Config baseConfig = new TurboShuffle.Config(
				0.0f,
				1f, 0.8f, 0f, 10f, TurboShuffle.Config.ProbabilityMode.BySong, runsPerTest, TestCommon.UnevenWeights(poolCount)
		);
		TestResults[] results = TestConfigVariable("clumpType", new float[]{0.0f, 0.5f, 1.0f}, baseConfig, poolCount, songsPerPool, songLength);
		Assert.assertTrue(results != null);
		Assert.assertTrue(results.length == 3);

		System.out.print("0.0 ");
		System.out.println(results[0].toString(false));
		System.out.print("0.5 ");
		System.out.println(results[1].toString(false));
		System.out.print("1.0 ");
		System.out.println(results[2].toString(false));

		Assert.assertTrue(results[0].unclumpinessStats.getMean() > results[1].unclumpinessStats.getMean());
		Assert.assertTrue(results[1].unclumpinessStats.getMean() > results[2].unclumpinessStats.getMean());
		Assert.assertTrue(results[0].unclumpinessStats.getMean() > results[2].unclumpinessStats.getMean());
	}
	@Test
	public void shuffle_TestClumpWeight(){
		final int runsPerTest = 1000, poolCount = 3, songsPerPool = 3, songLength = 120;

		TurboShuffle.Config baseConfig = new TurboShuffle.Config(
				1.0f, 1.0f,
				0f,
				0f, 0f, TurboShuffle.Config.ProbabilityMode.BySong, runsPerTest, TestCommon.UnevenWeights(poolCount)
		);
		TestResults[] results = TestConfigVariable("clumpWeight", new float[]{0.0f, 0.5f, 0.99f}, baseConfig, poolCount, songsPerPool, songLength);
		Assert.assertTrue(results != null);

		int i;
		System.out.print(0);
		System.out.print(" ");
		System.out.println(results[0].toString(false));
		for(i = 0; i < results.length - 1; i++){
			System.out.print(i + 1);
			System.out.print(" ");
			System.out.println(results[i + 1].toString(false));

			Assert.assertTrue(results[i + 1].unclumpinessStats.getMean() < results[i].unclumpinessStats.getMean());
		}
	}
	@Test
	public void shuffle_TestHistorySeverity(){
		final int runsPerTest = 1000, poolCount = 3, songsPerPool = 3, songLength = 120;

		TurboShuffle.Config baseConfig = new TurboShuffle.Config(
				0.5f, 0f, 0f, 0f,
				10f,
				TurboShuffle.Config.ProbabilityMode.BySong, runsPerTest, TestCommon.UnevenWeights(poolCount)
		);
		TestResults[] results = TestConfigVariable("historySeverity", new float[]{0.0f, 10.0f}, baseConfig, poolCount, songsPerPool, songLength);
		Assert.assertTrue(results != null);

		int i;
		System.out.print(0);
		System.out.print(" ");
		System.out.println(results[0].toString(false));
		for(i = 0; i < results.length - 1; i++) {
			System.out.print(i + 1);
			System.out.print(" ");
			System.out.println(results[i + 1].toString(false));

			Assert.assertTrue(results[i + 1].poolRatioDifferenceStats.getStandardDeviation() < results[i].poolRatioDifferenceStats.getStandardDeviation());
		}
	}
	@Test
	public void shuffle_TestClumpSeverity() {
		final int runsPerTest = 1000, poolCount = 3, songsPerPool = 3, songLength = 120;

		TurboShuffle.Config baseConfig = new TurboShuffle.Config(
				1.0f,
				0f,
				0.99f, 0f, 0f, TurboShuffle.Config.ProbabilityMode.BySong, runsPerTest, TestCommon.UnevenWeights(poolCount)
		);
		TestResults[] results = TestConfigVariable("clumpSeverity", new float[]{0.0f, 1.0f, 10.0f}, baseConfig, poolCount, songsPerPool, songLength);
		Assert.assertTrue(results != null);

		int i;
		System.out.print(0);
		System.out.print(" ");
		System.out.println(results[0].toString(false));
		for(i = 0; i < results.length - 1; i++){
			System.out.print(i + 1);
			System.out.print(" ");
			System.out.println(results[i + 1].toString(false));

			Assert.assertTrue(results[i + 1].unclumpinessStats.getMean() < results[i].unclumpinessStats.getMean());
		}

	}
}
