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
package com.turboturnip.turnipmusic.ui.base;

import android.app.ActivityManager;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.CardView;

import com.google.android.exoplayer2.C;
import com.turboturnip.turnipmusic.MusicService;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.ui.FullScreenPlayerActivity;
import com.turboturnip.turnipmusic.ui.roots.MusicBrowserProvider;
import com.turboturnip.turnipmusic.ui.PlaybackControlsFragment;
import com.turboturnip.turnipmusic.utils.JSONHelper;
import com.turboturnip.turnipmusic.utils.LogHelper;
import com.turboturnip.turnipmusic.utils.ResourceHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class BaseActivity extends ActionBarCastActivity implements CommandFragment.CommandFragmentListener {

    private static final String TAG = LogHelper.makeLogTag(BaseActivity.class);
    private static final String FRAGMENT_TAG = "turnipmusic_fragment_container";
	public static final String EXTRA_START_FULLSCREEN =
			"com.turboturnip.turnipmusic.EXTRA_START_FULLSCREEN";

	/**
	 * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
	 * the {@link FullScreenPlayerActivity}, speeding up the screen rendering
	 * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
	 */
	public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
			"com.turboturnip.turnipmusic.CURRENT_MEDIA_DESCRIPTION";

	private static final String IS_SAVED_STATE_KEY = "is_saved_state";
	private static final String FRAGMENT_STACK_KEY = "fragment_stack";
	private static final String FRAGMENT_BUNDLE_KEY = "fragment_bundle";

    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;
    private CardView mControlsCardView;

    private class FragmentStackEntry{
	    WeakReference<CommandFragment> fragment;
	    Bundle args;

	    public FragmentStackEntry(WeakReference<CommandFragment> fragment, Bundle args){
	    	this.fragment = fragment;
	    	this.args = args;
	    }
    }
    private List<FragmentStackEntry> fragmentStack = new ArrayList<>();

    private boolean isConnecting = false;

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

        if (Build.VERSION.SDK_INT >= 21) {
            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
            // list gets weird. We need to change either the icon or the color
            // of the TaskDescription.
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
                    getTitle().toString(),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_white),
                    ResourceHelper.getThemeColor(this, R.attr.colorPrimary,
                            android.R.color.darker_gray));
            setTaskDescription(taskDesc);
        }

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
            new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogHelper.d(TAG, "Activity onStart");

        mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager()
            .findFragmentById(R.id.fragment_playback_controls);
        if (mControlsFragment == null) {
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }
        mControlsCardView = findViewById(R.id.controls_container);

        hidePlaybackControls();

        if (!isConnecting) {
	        mMediaBrowser.connect();
	        isConnecting = true;
        }
    }

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (!savedInstanceState.getBoolean(IS_SAVED_STATE_KEY, false)) return;
		Class[] fragmentClasses;
		Parcelable[] fragmentArguments;
		try {
			String[] fragmentClassNames = savedInstanceState.getStringArray(FRAGMENT_STACK_KEY);
			fragmentClasses = new Class[fragmentClassNames.length];
			fragmentArguments = savedInstanceState.getParcelableArray(FRAGMENT_BUNDLE_KEY);
			for (int i = 0; i < fragmentClassNames.length; i++){
				fragmentClasses[i] = Class.forName(fragmentClassNames[i]);
			}

			for (int i = 0; i < fragmentClasses.length; i++){
				navigateToNewFragment(fragmentClasses[i], (Bundle)fragmentArguments[i]);
			}
		}catch (NullPointerException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}
		LogHelper.e(TAG, "Success loading from args!");
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (fragmentStack.size() == 0) return;
		outState.putBoolean(IS_SAVED_STATE_KEY, true);
		String[] fragmentClassNames = new String[fragmentStack.size()];
		Parcelable[] fragmentArguments = new Parcelable[fragmentStack.size()];
		for (int i = 0; i < fragmentStack.size(); i++){
			fragmentClassNames[i] = fragmentStack.get(i).fragment.get().getClass().getName();
			fragmentArguments[i] = fragmentStack.get(i).args;
		}
		outState.putStringArray(FRAGMENT_STACK_KEY, fragmentClassNames);
		outState.putParcelableArray(FRAGMENT_BUNDLE_KEY, fragmentArguments);
	}

	@Override
    protected void onStop() {
        super.onStop();
        LogHelper.d(TAG, "Activity onStop");
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCallback);
        }
        mMediaBrowser.disconnect();
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

	protected void startFullScreenActivityIfNeeded(Intent intent) {
		if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
			Intent fullScreenIntent = new Intent(this, FullScreenPlayerActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
							Intent.FLAG_ACTIVITY_CLEAR_TOP)
					.putExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION,
							intent.getParcelableExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION));
			startActivity(fullScreenIntent);
		}
	}

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    protected void onMediaControllerConnected() {
        isConnecting = false;
    }

	@Override
	public void onMediaItemPlayed(MediaBrowserCompat.MediaItem item) {
		LogHelper.d(TAG, "onMediaItemPlayed, musicFilter=" + item.getMediaId());

		MediaControllerCompat.getMediaController(this).getTransportControls()
				.playFromMediaId(item.getMediaId(), null);
	}

    protected void showPlaybackControls() {
        LogHelper.d(TAG, "showPlaybackControls");
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(
                R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
            .show(mControlsFragment)
            .commit();
    }

    protected void hidePlaybackControls() {
        LogHelper.d(TAG, "hidePlaybackControls");
        // TODO: Animate the card view
        getSupportFragmentManager().beginTransaction()
            .hide(mControlsFragment)
            .commit();
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if (mediaController == null ||
            mediaController.getMetadata() == null ||
            mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
            	LogHelper.e(TAG, mediaController.getPlaybackState().getState());
                return true;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mMediaControllerCallback);

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            LogHelper.d(TAG, "connectionCallback.onConnected: " +
                "hiding controls because metadata is null");
            hidePlaybackControls();
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }

        onMediaControllerConnected();
    }

    protected abstract void initializeFromParams(Bundle savedInstanceState, Intent intent);
    public void navigateToNewFragment(Class fragmentClass, Bundle data){
        CommandFragment fragment = getCurrentFragment();

        if (fragment == null || !fragment.getArguments().equals(data) || !fragmentClass.isInstance(fragment)){
            try {
                fragment = (CommandFragment)fragmentClass.newInstance();
            }catch (InstantiationException e){
                e.printStackTrace();
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
            fragment.setArguments(data);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                    R.animator.slide_in_from_left, R.animator.slide_out_to_right);
            transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
            if (!fragment.isRoot())
                transaction.addToBackStack(null);
            transaction.commit();

            fragmentStack.add(new FragmentStackEntry(new WeakReference<>(fragment), data));
        }
    }
    protected CommandFragment getCurrentFragment() {
    	if (fragmentStack.isEmpty()) return null;
        return fragmentStack.get(fragmentStack.size() - 1).fragment.get();
    }
    protected CommandFragment getPreviousFragment() {
    	if (fragmentStack.size() < 2) return null;
    	return fragmentStack.get(fragmentStack.size() - 2).fragment.get();
    }
	@Override
	public void navigateBack() {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
    	LogHelper.e(TAG, "BACCC");
		if (!fragmentStack.isEmpty())
			fragmentStack.remove(fragmentStack.size() - 1);
		super.onBackPressed();
	}

	@Override
	public void getDataFromFragment(Bundle data) {
		if (data.getBoolean(CommandFragment.PASS_BACK_TAG, false) && getPreviousFragment() != null)
			getPreviousFragment().getDataFromChildFragment(data);
	}

	// MusicCatalogCallback that ensures that we are showing the controls
    private final MediaControllerCompat.Callback mMediaControllerCallback =
        new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                if (shouldShowControls()) {
                    showPlaybackControls();
                } else {
                    LogHelper.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " +
                            "hiding controls because state is ", state.getState());
                    hidePlaybackControls();
                }
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                if (shouldShowControls()) {
                    showPlaybackControls();
                } else {
                    LogHelper.d(TAG, "mediaControllerCallback.onMetadataChanged: " +
                        "hiding controls because metadata is null");
                    hidePlaybackControls();
                }
            }
        };

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
        new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                LogHelper.d(TAG, "onConnected");
                try {
                    connectToSession(mMediaBrowser.getSessionToken());
                } catch (RemoteException e) {
                    LogHelper.e(TAG, e, "could not connect media controller");
                    hidePlaybackControls();
                }
            }
        };

}
