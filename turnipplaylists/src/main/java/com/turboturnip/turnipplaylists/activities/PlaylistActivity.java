package com.turboturnip.turnipplaylists.activities;

import android.content.Intent;
import android.os.Bundle;

import com.turboturnip.turboui.fragment.CommandFragment;
import com.turboturnip.turnipplaylists.fragments.PlaylistBrowserFragment;

public class PlaylistActivity extends BaseActivity {
	@Override
	protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
		CommandFragment currentFragment = getCurrentFragment();
		if (currentFragment == null)
			navigateToNewFragment(PlaylistBrowserFragment.class, new Bundle());
	}
}
