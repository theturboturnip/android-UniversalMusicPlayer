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
package com.turboturnip.turnipmusic.ui;

import android.app.FragmentTransaction;
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
import com.turboturnip.turnipmusic.utils.LogHelper;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
public class MusicBrowserActivity extends BrowserActivity
        implements CommandFragment.CommandFragmentListener{

	private static final String TAG = LogHelper.makeLogTag(MusicBrowserActivity.class);
	private static final String SAVED_MUSIC_FILTER = "com.turboturnip.turnipmusic.MEDIA_ID";
	private static final String FRAGMENT_TAG = "turnipmusic_fragment_container";

	public static final String NEW_FILTER_EXTRA = "com.turboturnip.turnipmusic.NEW_FILTER_EXTRA";

	public static final String EXTRA_START_FULLSCREEN =
			"com.turboturnip.turnipmusic.EXTRA_START_FULLSCREEN";

	/**
	 * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
	 * the {@link FullScreenPlayerActivity}, speeding up the screen rendering
	 * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
	 */
	public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
			"com.turboturnip.turnipmusic.CURRENT_MEDIA_DESCRIPTION";

	private Bundle mVoiceSearchParams;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.d(TAG, "Activity onCreate");

		setContentView(R.layout.activity_player);

		initializeToolbar();
		initializeFromParams(savedInstanceState, getIntent());

		// Only check if a full screen player is needed on the first time:
		if (savedInstanceState == null) {
			startFullScreenActivityIfNeeded(getIntent());
		}
	}

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
	@Override
	public void onMediaItemPlayed(MediaBrowserCompat.MediaItem item) {
		LogHelper.d(TAG, "onMediaItemPlayed, musicFilter=" + item.getMediaId());

		MediaControllerCompat.getMediaController(MusicBrowserActivity.this).getTransportControls()
					.playFromMediaId(item.getMediaId(), null);
	}

	@Override
	public void setToolbarTitle(CharSequence title) {
		LogHelper.d(TAG, "Setting toolbar title to ", title);
		if (title == null) {
			title = getString(R.string.app_name);
		}
		setTitle(title);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		LogHelper.d(TAG, "onNewIntent, intent=" + intent);
		initializeFromParams(null, intent);
		startFullScreenActivityIfNeeded(intent);
	}

	private void startFullScreenActivityIfNeeded(Intent intent) {
		if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
			Intent fullScreenIntent = new Intent(this, FullScreenPlayerActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
							Intent.FLAG_ACTIVITY_CLEAR_TOP)
					.putExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION,
							intent.getParcelableExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION));
			startActivity(fullScreenIntent);
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

	public void navigateToNewFragment(Class fragmentClass, Bundle data){
		CommandFragment fragment = getCurrentFragment();

		if (fragment == null || !fragment.getArguments().equals(data) || !fragmentClass.isInstance(fragment)){
			try {
				fragment = (CommandFragment)fragmentClass.newInstance();
			}catch (InstantiationException e){
				e.printStackTrace();
				return;
			}catch (IllegalAccessException e){
				e.printStackTrace();
				return;
			}
			fragment.setArguments(data);

			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.setCustomAnimations(
					R.animator.slide_in_from_right, R.animator.slide_out_to_left,
					R.animator.slide_in_from_left, R.animator.slide_out_to_right);
			transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
			if (!fragment.isRoot())
				transaction.addToBackStack(null);
			transaction.commit();
		}
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

	private CommandFragment getCurrentFragment() {
		return (CommandFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
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
}