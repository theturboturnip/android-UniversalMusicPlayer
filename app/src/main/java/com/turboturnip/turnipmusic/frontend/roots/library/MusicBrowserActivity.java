/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.turboturnip.turnipmusic.frontend.roots.library;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.MenuItem;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turboui.fragment.CommandFragment;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.frontend.base.MusicBrowserCommandFragment;
import com.turboturnip.turnipmusic.frontend.base.MusicListCommandFragment;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;
import com.turboturnip.turnipmusic.frontend.base.BaseActivity;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
public class MusicBrowserActivity extends BaseActivity
        implements CommandFragment.CommandFragmentListener{

	private static final String TAG = LogHelper.makeLogTag(MusicBrowserActivity.class);
	private static final String SAVED_MUSIC_FILTER = "com.turboturnip.turnipmusic.MEDIA_ID";

	public static final String NEW_FILTER_EXTRA = "com.turboturnip.turnipmusic.NEW_FILTER_EXTRA";

	private Bundle mVoiceSearchParams;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		String musicFilter = getFilter();
		if (musicFilter != null) {
			outState.putString(SAVED_MUSIC_FILTER, musicFilter);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemSelected(String filter) {
		LogHelper.d(TAG, "onMediaItemSelected, musicFilter=" + filter);

		MusicFilter compiledFilter = new MusicFilter(filter);
		if (compiledFilter.isValid()) {
			navigateToBrowser(filter);
		} else {
			LogHelper.w(TAG, "Ignoring MediaItem that is not browsable: ",
					"musicFilter=", filter);
		}
	}

	protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
		String mediaFilterString = null;
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			mediaFilterString = new MusicFilter(MusicFilterType.Search, query).toString();
		} else if (intent.hasExtra(NEW_FILTER_EXTRA)){
			mediaFilterString = intent.getStringExtra(NEW_FILTER_EXTRA);
		} else {
			if (savedInstanceState != null) {
				// If there is a saved media ID, use it
				mediaFilterString = savedInstanceState.getString(SAVED_MUSIC_FILTER);
			}
		}
		if (mediaFilterString != null)
			navigateToBrowser(mediaFilterString);
		else
			navigateToNewFragment(HubFragment.class, new Bundle());
	}

	private void navigateToBrowser(String mediaFilter) {
		LogHelper.d(TAG, "navigateToBrowser, mediaFilter=" + mediaFilter);

		Bundle args = new Bundle(1);
		args.putString(MusicBrowserFragment.ARG_MUSIC_FILTER, mediaFilter);
		navigateToNewFragment(MusicBrowserFragment.class, args);
	}

	public String getFilter() {
		CommandFragment fragment = getCurrentFragment();
		if (fragment == null) {
			return null;
		}
		if (!(fragment instanceof MusicListCommandFragment)) return "";
		MusicFilter filter = ((MusicListCommandFragment)fragment).getFilter();
		if (filter == null)
			return MusicFilter.rootFilter().toString();
		return filter.toString();
	}

	@Override
	protected void onMediaControllerConnected() {
		((MusicBrowserCommandFragment)getCurrentFragment()).connectToMediaBrowser();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				onSearchRequested();
				return true;
			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void getDataFromFragment(Bundle data) {
		LogHelper.e(TAG, "Get Data from Fragment");
		if (data.getString("REQUEST", "NONE").equals("PLAY")){
			LogHelper.e(TAG,"Got Fragment Play Request!");
			MediaControllerCompat.getMediaController(MusicBrowserActivity.this).getTransportControls().playFromMediaId("FILTER", data);
		}
	}

	@Override
	protected int getNavMenuItemId() {
		return R.id.navigation_allmusic;
	}
}