package com.turboturnip.turnipmusic.frontend.roots.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.frontend.base.ItemListCommandFragment;
import com.turboturnip.turnipmusic.frontend.base.LinearItemListCommandFragment;
import com.turboturnip.turnipmusic.frontend.base.ListableHeader;
import com.turboturnip.turnipmusic.frontend.base.ListableItem;
import com.turboturnip.turnipmusic.frontend.roots.library.legacy.MusicBrowserFragment;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;

public class HubFragment extends LinearItemListCommandFragment {
	private static final String TAG = LogHelper.makeLogTag(HubFragment.class);

	@Override
	public boolean isRoot(){
		return true;
	}

	private class HubButton extends ListableItem {
        private MusicFilter filter;

        HubButton(ItemListCommandFragment owner, CharSequence title, CharSequence subtitle, MusicFilterType toExplore) {
            super(owner, title, subtitle, null, true, false);
            filter = new MusicFilter(MusicFilterType.Explore, toExplore.toString());
        }

        @Override
        public void onBrowse(){
            Bundle args = new Bundle(1);
            args.putString(MusicBrowserFragment.ARG_MUSIC_FILTER, filter.toString());
            mCommandListener.navigateToNewFragment(AlbumListFragment.class, new Bundle());
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View resultView = super.onCreateView(inflater, container, savedInstanceState);

		mAdapter.items.clear();
        mAdapter.items.add(new ListableHeader(this, "Built In Filters"));
        mAdapter.items.add(new HubButton(this, "Filter by Album", "", MusicFilterType.ByAlbum));
        mAdapter.items.add(new HubButton(this, "Filter by Artist", "", MusicFilterType.ByArtist));
        mAdapter.items.add(new HubButton(this, "Filter by Tag", "", MusicFilterType.ByTag));

        mAdapter.items.add(new ListableHeader(this, "Tests"));
		/*mBrowserAdapter.addItem(new ListItemData("Repeat AC", "", null, new JourneyTestOnClickListener(
				new Journey("Test Repeat AC", new MusicFilter(MusicFilterType.ByAlbum, "Assassination Classroom"))
		)));
		mBrowserAdapter.addItem(new ListItemData("Shuffle Persona", "", null,
				new JourneyTestOnClickListener(
						new Journey("Test Shuffle Persona",
								new Journey.Stage("", Journey.Stage.PlayType.Shuffle, 0, null,
										new CompositeMusicFilter(new MusicFilter(MusicFilterType.ByAlbum, "Persona")))
						)
				))
		);
		mBrowserAdapter.addItem(new ListItemData("Shuffle AC, D, Persona"));*/
        mAdapter.notifyDataSetChanged();

		return resultView;
	}

	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle(null);
	}

	@Override
	public void connectToMediaBrowser() {}
	@Override
	public void disconnectFromMediaBrowser() {}
}
