package com.turboturnip.turnipmusic.utils;

import android.os.Looper;

public class AsyncHelper {
	public static void ThrowIfOnMainThread(){
		ThrowIfOnMainThread("This function");
	}
	public static void ThrowIfOnMainThread(String functionDesc){
		if (Looper.myLooper() == Looper.getMainLooper())
			throw new IllegalThreadStateException(functionDesc + " can't be run on the main thread!");
	}
}
