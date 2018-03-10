package com.turboturnip.turnipmusic.ui;

import android.content.Intent;
import android.os.Bundle;

import com.turboturnip.turnipmusic.utils.LogHelper;

public class QueueActivity extends MusicBrowserActivity {
	private static final String TAG = LogHelper.makeLogTag(QueueActivity.class);

	@Override
	protected void onNewIntent(Intent intent) {
		LogHelper.d(TAG, "onNewIntent, intent=" + intent);
		startFullScreenActivityIfNeeded(intent);
	}
	@Override
	protected void initializeFromParams(Bundle savedInstanceState, Intent intent){
		CommandFragment currentFragment = getCurrentFragment();
		if (currentFragment == null || !(currentFragment instanceof QueueFragment))
			navigateToNewFragment(QueueFragment.class, new Bundle());
	}
}
