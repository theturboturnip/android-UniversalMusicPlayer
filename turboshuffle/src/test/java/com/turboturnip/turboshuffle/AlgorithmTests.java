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
			Map<TurboShuffle.SongPoolKey, Integer> songOccurrences;
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
		Map<TurboShuffle.SongPoolKey, Double> songOccurrenceMeans, songOccurrenceStdDevs;
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
			for(TurboShuffle.SongPoolKey key : results[0].songOccurrences.keySet()){
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
				for (TurboShuffle.SongPoolKey key : songOccurrenceMeans.keySet()) {
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

	private float[] EvenWeights(int poolCount){
		float[] weights = new float[poolCount];
		while(poolCount > 0){
			weights[poolCount - 1] = 1.0f;
			poolCount--;
		}
		return weights;
	}
	private float[] UnevenWeights(int poolCount){
		float[] weights = new float[poolCount];
		while(poolCount > 0){
			weights[poolCount - 1] = poolCount;
			poolCount--;
		}
		return weights;
	}
	private TurboShuffle.SongPool[] CreateTestCasePools(int pools, int songsPerPool, int songLength){
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
	private TurboShuffle CreateTestCaseShuffler(TurboShuffle.Config config, int pools, int songsPerPool, int songLength){
		return new TurboShuffle(config, CreateTestCasePools(pools, songsPerPool, songLength));
	}
	private TestResults.SingleResult RunShufflerNTimes(TurboShuffle shuffler, int N){
		TurboShuffle.State state = shuffler.new State();
		Random rng = new Random();
		for (int i = 0; i < N; i++){
			TurboShuffle.SongPoolKey nextKey = shuffler.NextKey(state, rng);
			state.IncrementHistory(nextKey);
		}
		return new TestResults.SingleResult(shuffler.config, state);
	}
	private TestResults RunShufflerXTimesWithYReps(TurboShuffle shuffler, int X, int reps){
		TestResults.SingleResult[] results = new TestResults.SingleResult[reps];
		for (int i = 0; i < reps; i++){
			results[i] = RunShufflerNTimes(shuffler, X);
		}
		return new TestResults(results);
	}
	private TestResults[] TestConfigVariable(String varName, float[] values, TurboShuffle.Config base, int poolCount, int songsPerPool, int songLength, int totalSongsToPlay, int reps){
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
			TurboShuffle shuffler = CreateTestCaseShuffler(base, poolCount, songsPerPool, songLength);
			results[i] = RunShufflerXTimesWithYReps(shuffler, totalSongsToPlay, reps);
		}
		return results;
	}

	@Test
	public void shuffle_Run1000Times10Reps(){
		TurboShuffle.Config config = new TurboShuffle.Config(
				0.5f, 0f, 0f, 0f, 50f,
				TurboShuffle.Config.ProbabilityMode.BySong, 1000, UnevenWeights(3)
		);
		TurboShuffle shuffler = CreateTestCaseShuffler(config, 3, 3, 120);
		System.out.println(RunShufflerXTimesWithYReps(shuffler, 1000, 10));
	}
	@Test
	public void shuffle_TestClumpType(){
		final int runsPerTest = 1000, poolCount = 3, songsPerPool = 3, songLength = 120, reps = 10;

		TurboShuffle.Config baseConfig = new TurboShuffle.Config(
				0.0f,
				1f, 0.8f, 0f, 10f, TurboShuffle.Config.ProbabilityMode.BySong, runsPerTest, UnevenWeights(poolCount)
		);
		TestResults[] results = TestConfigVariable("clumpType", new float[]{0.0f, 0.5f, 1.0f}, baseConfig, poolCount, songsPerPool, songLength, 1000, reps);
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
		final int runsPerTest = 1000, poolCount = 3, songsPerPool = 3, songLength = 120, reps = 10;

		TurboShuffle.Config baseConfig = new TurboShuffle.Config(
				0.0f, 0f,
				0f,
				0f, 0f, TurboShuffle.Config.ProbabilityMode.BySong, runsPerTest, UnevenWeights(poolCount)
		);
		TestResults[] results = TestConfigVariable("clumpWeight", new float[]{0.75f, 0.9f, 0.99f}, baseConfig, poolCount, songsPerPool, songLength, 1000, reps);
		Assert.assertTrue(results != null);

		int i;
		for(i = 0; i < results.length - 1; i++){
			System.out.print(i);
			System.out.print(" ");
			System.out.println(results[i].toString(false));

			Assert.assertTrue(results[i + 1].unclumpinessStats.getStandardDeviation() < results[i].unclumpinessStats.getStandardDeviation());
		}
		System.out.print(i);
		System.out.print(" ");
		System.out.println(results[i].toString(false));
	}
	@Test
	public void shuffle_TestHistorySeverity(){
		final int runsPerTest = 1000, poolCount = 3, songsPerPool = 3, songLength = 120, reps = 10;

		TurboShuffle.Config baseConfig = new TurboShuffle.Config(
				0.0f, 0f, 0f, 0f,
				10f,
				TurboShuffle.Config.ProbabilityMode.BySong, runsPerTest, UnevenWeights(poolCount)
		);
		TestResults[] results = TestConfigVariable("historySeverity", new float[]{0.0f, 10.0f}, baseConfig, poolCount, songsPerPool, songLength, 1000, reps);
		Assert.assertTrue(results != null);

		int i;
		for(i = 0; i < results.length - 1; i++){
			System.out.print(i);
			System.out.print(" ");
			System.out.println(results[i].toString(false));

			Assert.assertTrue(results[i + 1].poolRatioDifferenceStats.getStandardDeviation() < results[i].poolRatioDifferenceStats.getStandardDeviation());
		}
		System.out.print(i);
		System.out.print(" ");
		System.out.println(results[i].toString(false));
	}
}
