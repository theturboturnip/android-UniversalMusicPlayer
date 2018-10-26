package com.turboturnip.turnipmusic.frontend.base;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turboui.fragment.CommandFragment;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.frontend.roots.library.MusicBrowserProvider;

public abstract class MediaCommandFragment extends CommandFragment {

	private static final String TAG = LogHelper.makeLogTag(MediaCommandFragment.class);

	public static final String ARG_MUSIC_FILTER = "music_filter";
	protected MusicFilter mMusicFilter;

	MusicBrowserProvider browserProvider;

	@Override
	public void onStart() {
		super.onStart();

		browserProvider = (MusicBrowserProvider)mCommandListener;
		MediaBrowserCompat mediaBrowser = browserProvider.getMediaBrowser();

		LogHelper.d(TAG, "fragment.onStart, musicFilter=", mMusicFilter,
				"  onConnected=" + mediaBrowser.isConnected());

		if (mediaBrowser.isConnected()) {
			onConnected();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		MediaBrowserCompat mediaBrowser = browserProvider.getMediaBrowser();
		if (mediaBrowser != null && mediaBrowser.isConnected() && mMusicFilter != null) {
			mediaBrowser.unsubscribe(mMusicFilter.toString());
		}
	}

	public MusicFilter getFilter() {
		Bundle args = getArguments();
		if (args != null) {
			String musicFilterString = args.getString(ARG_MUSIC_FILTER);
			if (musicFilterString != null)
				return new MusicFilter(musicFilterString);
		}
		return null;
	}

	// Called when the MediaBrowser is connected. This method is either called by the
	// fragment.onStart() or explicitly by the activity in the case where the connection
	// completes after the onStart()
	public void onConnected() {
		if (isDetached()) {
			return;
		}
		mMusicFilter = getFilter();
		if (mMusicFilter == null) {
			mMusicFilter = new MusicFilter(browserProvider.getMediaBrowser().getRoot());
			//LogHelper.d(TAG,"media ID was null, now ", mMediaId);
		}
		updateTitle();
	}
}
