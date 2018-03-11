package com.turboturnip.turnipmusic.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.turboturnip.turnipmusic.ui.roots.QueueActivity;
import com.turboturnip.turnipmusic.utils.LogHelper;

public abstract class SingleFragmentActivity extends BrowserActivity {
	private static final String TAG = LogHelper.makeLogTag(QueueActivity.class);

	@Override
	protected void initializeFromParams(Bundle savedInstanceState, Intent intent){
		CommandFragment currentFragment = getCurrentFragment();
		if (currentFragment == null)
			navigateToNewFragment(getFragmentClass(), new Bundle());
	}

	protected abstract Class getFragmentClass();

	@Override
	public void onMediaItemSelected(MediaBrowserCompat.MediaItem filter) {}

	@Override
	public void getDataFromFragment(Bundle data) {}
}
