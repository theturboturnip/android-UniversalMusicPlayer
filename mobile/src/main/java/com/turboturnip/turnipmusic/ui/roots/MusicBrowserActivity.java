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
package com.turboturnip.turnipmusic.ui.roots;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.MenuItem;

import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.ui.base.BaseActivity;
import com.turboturnip.turnipmusic.ui.base.CommandFragment;
import com.turboturnip.turnipmusic.utils.LogHelper;

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
	protected void onSaveInstanceState(Bundle outState) {
		String musicFilter = getFilter();
		if (musicFilter != null) {
			outState.putString(SAVED_MUSIC_FILTER, musicFilter);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
		LogHelper.d(TAG, "onMediaItemSelected, musicFilter=" + item.getMediaId());
		if (item.isBrowsable()) {
			navigateToBrowser(item.getMediaId());
		} else {
			LogHelper.w(TAG, "Ignoring MediaItem that is not browsable: ",
					"musicFilter=", item.getMediaId());
		}
	}

	protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
		String mediaFilterString = null;
		// check if we were started from a "Play XYZ" voice search. If so, we save the extras
		// (which contain the query details) in a parameter, so we can reuse it later, when the
		// MediaSession is connected.
		if (intent.getAction() != null
				&& intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
			mVoiceSearchParams = intent.getExtras();
			// TODO: Actually start from the voice search query
			LogHelper.d(TAG, "Starting from voice search query=",
					mVoiceSearchParams.getString(SearchManager.QUERY));
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
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
		MusicFilter filter = fragment.getFilter();
		if (filter == null)
			return MusicFilter.rootFilter().toString();
		return filter.toString();
	}

	@Override
	protected void onMediaControllerConnected() {
		if (mVoiceSearchParams != null) {
			// If there is a bootstrap parameter to start from a search query, we
			// send it to the media session and set it to null, so it won't play again
			// when the activity is stopped/started or recreated:
			String query = mVoiceSearchParams.getString(SearchManager.QUERY);
			MediaControllerCompat.getMediaController(MusicBrowserActivity.this).getTransportControls()
					.playFromSearch(query, mVoiceSearchParams);
			mVoiceSearchParams = null;
		}
		getCurrentFragment().onConnected();
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
	public void getDataFromFragment(Bundle data) {}
}