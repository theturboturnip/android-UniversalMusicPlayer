package com.turboturnip.turnipmusic.ui.base;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.ui.roots.MusicBrowserProvider;
import com.turboturnip.turnipmusic.utils.LogHelper;


public class CommandFragment extends Fragment {
	private static final String TAG = LogHelper.makeLogTag(CommandFragment.class);
	public static final String ARG_MUSIC_FILTER = "music_filter";

	protected CommandFragmentListener mCommandListener = null;
	protected MusicFilter mMusicFilter;

	public boolean isRoot(){
		return false;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// If used on an activity that doesn't implement MediaFragmentListener, it
		// will throw an exception as expected:
		mCommandListener = (CommandFragmentListener) activity;
	}
	@Override
	public void onDetach(){
		super.onDetach();
		mCommandListener = null;
	}

	@Override
	public void onStart() {
		super.onStart();

		MediaBrowserCompat mediaBrowser = mCommandListener.getMediaBrowser();

		LogHelper.d(TAG, "fragment.onStart, musicFilter=", mMusicFilter,
				"  onConnected=" + mediaBrowser.isConnected());

		if (mediaBrowser.isConnected()) {
			onConnected();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		MediaBrowserCompat mediaBrowser = mCommandListener.getMediaBrowser();
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
			mMusicFilter = new MusicFilter(mCommandListener.getMediaBrowser().getRoot());
			//LogHelper.d(TAG,"media ID was null, now ", mMediaId);
		}
		updateTitle();
	}

	protected void updateTitle(){
		mCommandListener.setToolbarTitle("Default Title");
	}

	public interface CommandFragmentListener extends MusicBrowserProvider {
		void setToolbarTitle(CharSequence title);
		CommandFragment navigateToNewFragment(Class newFragmentClass, Bundle initData);
		void onMediaItemSelected(MediaBrowserCompat.MediaItem filter);
		void onMediaItemPlayed(MediaBrowserCompat.MediaItem filter);
		void getDataFromFragment(Bundle data);
		void navigateBack();
	}
}
