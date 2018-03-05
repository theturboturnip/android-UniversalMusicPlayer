package com.turboturnip.turboshuffle;

import java.util.Arrays;
import java.util.List;

public class SongPool {
	public final List<TurboShuffleSong> songs;
	public final int averageLengthInSeconds;
	public final int averageLengthInMinutes;

	public SongPool(TurboShuffleSong... songs){
		this.songs = Arrays.asList(songs);
		int totalLengthInSeconds = 0;
		for (TurboShuffleSong song : songs) {
			totalLengthInSeconds += song.getLengthInSeconds();
		}
		averageLengthInSeconds = totalLengthInSeconds / songs.length;
		averageLengthInMinutes = Common.secondsToMinutes(averageLengthInSeconds);
	}
}