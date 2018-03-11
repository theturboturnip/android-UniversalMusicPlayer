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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.widget.Toast;

import com.turboturnip.turnipmusic.model.MusicFilterType;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.ui.base.MusicListCommandFragment;
import com.turboturnip.turnipmusic.utils.LogHelper;
import com.turboturnip.turnipmusic.utils.NetworkHelper;

import java.util.List;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the {@link com.turboturnip.turnipmusic.MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */
public class MusicBrowserFragment extends MusicListCommandFragment {

    private static final String TAG = LogHelper.makeLogTag(MusicBrowserFragment.class);

    protected BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        private boolean oldOnline = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            // We don't care about network changes while this fragment is not associated
            // with a media ID (for example, while it is being initialized)
            if (mMusicFilter != null && !mMusicFilter.isValid()) {
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
	    this.getActivity().unregisterReceiver(mConnectivityChangeReceiver);
    }

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
        new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId,
                                         @NonNull List<MediaBrowserCompat.MediaItem> children) {
                try {
                    LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                        "  count=" + children.size());
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
	}

    @Override
    protected void updateTitle() {
        if (mMusicFilter.filterType == MusicFilterType.Root || mMusicFilter.filterType == MusicFilterType.Empty || !mMusicFilter.isValid()) {
            mCommandListener.setToolbarTitle(null);
        }else {
	        mCommandListener.setToolbarTitle(mMusicFilter.toString());
	        switch (mMusicFilter.filterType){
		        case Explore:
		        	for (MusicFilterType explorableFilter : MusicFilterType.explorableTypes){
		        		if (explorableFilter.toString().equals(mMusicFilter.filterValue)) {
					        mCommandListener.setToolbarTitle("Exploring " + mMusicFilter.filterValue + "s");
				            return;
		        		}
			        }
		        default:
		        	mCommandListener.setToolbarTitle(mMusicFilter.filterType + ": " + mMusicFilter.filterValue);
	        }
        }
    }
}
