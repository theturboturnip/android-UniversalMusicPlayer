package com.turboturnip.turnipmusic.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.utils.LogHelper;

public class HubFragment extends CommandFragment {
	private static final String TAG = LogHelper.makeLogTag(HubFragment.class);

	@Override
	public boolean isRoot(){
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		LogHelper.d(TAG, "fragment.onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_hub, container, false);

		rootView.findViewById(R.id.hub_album_button).setOnClickListener(new HubButtonOnClickListener(MusicFilter.FILTER_BY_ALBUM));
		rootView.findViewById(R.id.hub_artist_button).setOnClickListener(new HubButtonOnClickListener(MusicFilter.FILTER_BY_ARTIST));
		rootView.findViewById(R.id.hub_tags_button).setOnClickListener(new HubButtonOnClickListener(MusicFilter.FILTER_BY_TAG));

		return rootView;
	}

	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle(null);
	}

	private class HubButtonOnClickListener implements View.OnClickListener{
		private MusicFilter filter;

		HubButtonOnClickListener(String toExplore){
			this.filter = new MusicFilter(new MusicFilter.SubFilter(MusicFilter.EXPLORE_FILTER, toExplore));
		}

		public void onClick(View button){
			Bundle args = new Bundle(1);
			args.putString(MediaBrowserFragment.ARG_MUSIC_FILTER, filter.toString());
			mCommandListener.navigateToNewFragment(MediaBrowserFragment.class, args);
		}
	}
}
