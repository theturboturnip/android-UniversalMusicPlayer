package com.turboturnip.turboshuffle;

public class Common {
	public static int secondsToMinutes(int seconds){
		return (seconds / 60) + ((seconds % 60 > 0) ? 1 : 0);
	}
}
