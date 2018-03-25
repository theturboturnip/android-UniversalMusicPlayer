package com.turboturnip.turnipplaylists.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.turboturnip.common.utils.LogHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class MultithreadedPoolService extends IntentService {

	private static final String TAG = LogHelper.makeLogTag(MultithreadedPoolService.class);

	public static final String INPUT_STRING_ARRAY = "input";
	public static final String OUTPUT_STRING_ARRAY = "output";
	public static final String BROADCAST_STATUS_ACTION = "com.turboturnip.turnipplaylists.MULTITHREADED_POOL_ACTION";
	public static final String BROADCAST_PROGRESS_EXTRA = "progress";
	public static final String BROADCAST_FINISHED_EXTRA = "finished";

	private List<String> inputs;
	private int initialInputs;

	protected int getThreadCount(){
		return 8;
	}
	protected abstract File getOutputFile(String input);
	protected abstract MultithreadedAction createNewAction();

	MultithreadedPoolService(String name){
		super(name);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (intent == null || !intent.hasExtra(INPUT_STRING_ARRAY)){
			return;
		}

		inputs = Collections.synchronizedList(Arrays.asList(intent.getStringArrayExtra(INPUT_STRING_ARRAY)));
		initialInputs = inputs.size();

		List<ActionThread> threads = new ArrayList<>(getThreadCount());
		for (int i = 0; i < getThreadCount(); i++){
			threads.add(new ActionThread("MultithreadedAction-" + i, createNewAction()));
		}
		for(ActionThread thread : threads){
			thread.start();
		}
		for(ActionThread thread : threads){
			try {
				thread.join();
			}catch (InterruptedException e){
				LogHelper.e(TAG, "Thread was interrupted!");
			}
		}
		Intent localIntent =
				new Intent(BROADCAST_STATUS_ACTION)
						// Puts the status into the Intent
						.putExtra(BROADCAST_FINISHED_EXTRA, true);
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	}

	private void updateProgress(){
		Intent localIntent =
				new Intent(BROADCAST_STATUS_ACTION)
						// Puts the status into the Intent
						.putExtra(BROADCAST_PROGRESS_EXTRA, 1.0f - (inputs.size() * 1.0f / initialInputs));
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	}

	private class ActionThread implements Runnable{
		private Thread t;
		private String threadName;
		private MultithreadedAction action;

		ActionThread(String threadName, MultithreadedAction action){
			this.threadName = threadName;
			this.action = action;
		}

		@Override
		public void run() {
			while(inputs.size() > 0) {
				String input = inputs.remove(0);
				File output = getOutputFile(input);
				try {
					action.runOnData(input, output);
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					updateProgress();
				}
			}
		}

		void start () {
			if (t == null) {
				t = new Thread (this, threadName);
				t.start ();
			}
		}
		void join() throws InterruptedException{
			if (t != null && t.isAlive()) t.join();
		}
	}
	public interface MultithreadedAction {
		void runOnData(String input, File output);
	}
}
