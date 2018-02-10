package com.turboturnip.turnipmusic.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.utils.LogHelper;

public class HubFragment extends ItemListCommandFragment {
	private static final String TAG = LogHelper.makeLogTag(HubFragment.class);

	@Override
	public boolean isRoot(){
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View resultView = super.onCreateView(inflater, container, savedInstanceState);

		mBrowserAdapter.clear();
		mBrowserAdapter.add(new ListItemData("Filter by Album", "", new HubButtonOnClickListener(MusicFilter.FILTER_BY_ALBUM), null));
		mBrowserAdapter.add(new ListItemData("Filter by Artist", "", new HubButtonOnClickListener(MusicFilter.FILTER_BY_ARTIST), null));
		mBrowserAdapter.add(new ListItemData("Filter by Tag", "", new HubButtonOnClickListener(MusicFilter.FILTER_BY_TAG), null));
		mBrowserAdapter.notifyDataSetChanged();

		return resultView;
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
			args.putString(MusicBrowserFragment.ARG_MUSIC_FILTER, filter.toString());
			mCommandListener.navigateToNewFragment(MusicBrowserFragment.class, args);
		}
	}
}
