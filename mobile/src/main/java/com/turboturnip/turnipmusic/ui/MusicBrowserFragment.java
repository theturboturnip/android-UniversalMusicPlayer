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

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.MusicFilterType;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.utils.LogHelper;
import com.turboturnip.turnipmusic.utils.MediaIDHelper;
import com.turboturnip.turnipmusic.utils.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the {@link com.turboturnip.turnipmusic.MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */
public class MusicBrowserFragment extends ItemListCommandFragment {

    private static final String TAG = LogHelper.makeLogTag(MusicBrowserFragment.class);

    // Extra states for the list items to have
	public static final int STATE_PLAYABLE = 1;
	public static final int STATE_PAUSED = 2;
	public static final int STATE_PLAYING = 3;

	private static ColorStateList sColorStatePlaying;
	private static ColorStateList sColorStateNotPlaying;

    protected BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        private boolean oldOnline = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            // We don't care about network changes while this fragment is not associated
            // with a media ID (for example, while it is being initialized)
            if (mMusicFilter != null || !mMusicFilter.isValid()) {
                boolean isOnline = NetworkHelper.isOnline(context);
                if (isOnline != oldOnline) {
                    oldOnline = isOnline;
                    checkForUserVisibleErrors(false);
                    if (isOnline) {
                        mBrowserAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
    @Override
    public void onStart(){
    	super.onStart();

	    // Registers BroadcastReceiver to track network connection changes.
	    this.getActivity().registerReceiver(mConnectivityChangeReceiver,
			    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    @Override
    public void onStop(){
    	super.onStop();
	    MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
	    if (controller != null) {
		    controller.unregisterCallback(mMediaControllerCallback);
	    }
	    this.getActivity().unregisterReceiver(mConnectivityChangeReceiver);
    }

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null) {
                return;
            }
            LogHelper.d(TAG, "Received metadata change to media ",
                    metadata.getDescription().getMediaId());
            mBrowserAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            LogHelper.d(TAG, "Received state change: ", state);
            checkForUserVisibleErrors(false);
            mBrowserAdapter.notifyDataSetChanged();
        }
    };

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
        new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId,
                                         @NonNull List<MediaBrowserCompat.MediaItem> children) {
                try {
                    LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                        "  count=" + children.size());
                    checkForUserVisibleErrors(children.isEmpty());
                    mBrowserAdapter.clear();
                    for (MediaBrowserCompat.MediaItem item : children) {
                        mBrowserAdapter.addItem(getDataForListItem(item));
                    }
                    mBrowserAdapter.notifyDataSetChanged();
                } catch (Throwable t) {
                    LogHelper.e(TAG, "Error on childrenloaded", t);
                }
            }

            @Override
            public void onError(@NonNull String id) {
                LogHelper.e(TAG, "browse fragment subscription onError, id=" + id);
                Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
                checkForUserVisibleErrors(true);
            }
        };

    ListItemData getDataForListItem(final MediaBrowserCompat.MediaItem item){
    	ListItemData itemData = new ListItemData();
	    MediaDescriptionCompat description = item.getDescription();
	    itemData.title = description.getTitle();
	    itemData.subtitle = description.getSubtitle();
	    itemData.internalData = item;
	    itemData.onItemClick = new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    checkForUserVisibleErrors(false);
			    mCommandListener.onMediaItemSelected(item);
		    }
	    };
	    itemData.onDrawableClick = new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    checkForUserVisibleErrors(false);
			    mCommandListener.onMediaItemPlayed(item);
		    }
	    };

	    return itemData;
    }

    @Override
	int getNewListItemState(ListItemData data){
		int state = STATE_PLAYABLE;
		MediaBrowserCompat.MediaItem mediaItem = (MediaBrowserCompat.MediaItem) data.internalData;
		// Set state to playable first, then override to playing or paused state if needed
		if (mediaItem.isPlayable() && MediaIDHelper.isMediaItemPlaying(getActivity(), mediaItem)) {
			MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
			PlaybackStateCompat pbState = controller.getPlaybackState();
			if (pbState == null ||
					pbState.getState() == PlaybackStateCompat.STATE_ERROR) {
				state = STATE_NONE;
			} else if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
				state = STATE_PLAYING;
			} else {
				state = STATE_PAUSED;
			}
		}

		return state;
	}

    @Override
    Drawable getDrawableFromListItemState(int state) {
	    if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
		    initializeColorStateLists(getActivity());
	    }

	    switch (state) {
		    case STATE_PLAYABLE:
			    Drawable pauseDrawable = ContextCompat.getDrawable(getActivity(),
					    R.drawable.ic_play_arrow_black_36dp);
			    DrawableCompat.setTintList(pauseDrawable, sColorStateNotPlaying);
			    return pauseDrawable;
		    case STATE_PLAYING:
			    AnimationDrawable animation = (AnimationDrawable)
					    ContextCompat.getDrawable(getActivity(), R.drawable.ic_equalizer_white_36dp);
			    DrawableCompat.setTintList(animation, sColorStatePlaying);
			    animation.start();
			    return animation;
		    case STATE_PAUSED:
			    Drawable playDrawable = ContextCompat.getDrawable(getActivity(),
					    R.drawable.ic_equalizer1_white_36dp);
			    DrawableCompat.setTintList(playDrawable, sColorStatePlaying);
			    return playDrawable;
		    default:
			    return null;
	    }
    }

	private static void initializeColorStateLists(Context ctx) {
		sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(
				R.color.media_item_icon_not_playing));
		sColorStatePlaying = ColorStateList.valueOf(ctx.getResources().getColor(
				R.color.media_item_icon_playing));
	}

    @Override
	public void onConnected(){
    	super.onConnected();

		// Unsubscribing before subscribing is required if this filter already has a subscriber
		// on this MediaBrowser instance. Subscribing to an already subscribed filter will replace
		// the callback, but won't trigger the initial callback.onChildrenLoaded.
		//
		// This is temporary: A bug is being fixed that will make subscribe
		// consistently call onChildrenLoaded initially, no matter if it is replacing an existing
		// subscriber or not. Currently this only happens if the filter has no previous
		// subscriber or if the media content changes on the service side, so we need to
		// unsubscribe first.
		mCommandListener.getMediaBrowser().unsubscribe(mMusicFilter.toString());

		// This sends the request to get children.
		mCommandListener.getMediaBrowser().subscribe(mMusicFilter.toString(), mSubscriptionCallback);

		// Add MediaController callback so we can redraw the list when metadata changes:
		MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
		if (controller != null) {
			controller.registerCallback(mMediaControllerCallback);
		}
	}

    @Override
    protected void updateTitle() {
        if (mMusicFilter.filterType == MusicFilterType.Root || !mMusicFilter.isValid()) {
            mCommandListener.setToolbarTitle(null);
        }else
        	mCommandListener.setToolbarTitle(mMusicFilter.toString());


        /*MediaBrowserCompat mediaBrowser = mCommandListener.getMediaBrowser();
        LogHelper.e(TAG, "getItem("+mMusicFilter.toString()+")");
        mediaBrowser.getItem(mMusicFilter.toString(), new MediaBrowserCompat.ItemCallback() {
            @Override
            public void onItemLoaded(MediaBrowserCompat.MediaItem item) {
            	LogHelper.e(TAG, item.getDescription().getTitle());
                mCommandListener.setToolbarTitle(
                        item.getDescription().getTitle());
            }
        });*/
    }
}
