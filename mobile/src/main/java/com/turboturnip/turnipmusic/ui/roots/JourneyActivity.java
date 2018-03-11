package com.turboturnip.turnipmusic.ui.roots;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.turboturnip.turnipmusic.ui.base.BrowserActivity;

public class JourneyActivity extends BrowserActivity {
	@Override
	protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
		navigateToNewFragment(JourneyListFragment.class, new Bundle());
	}

	@Override
	public void onMediaItemSelected(MediaBrowserCompat.MediaItem filter) {}
}
