package com.turboturnip.turnipmusic.ui.roots;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.CompositeMusicFilter;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;
import com.turboturnip.turnipmusic.ui.base.ItemListCommandFragment;
import com.turboturnip.turnipmusic.utils.LogHelper;

public class HubFragment extends ItemListCommandFragment {
	private static final String TAG = LogHelper.makeLogTag(HubFragment.class);

	private static final int STATE_PLAYABLE = 2;

	@Override
	public boolean isRoot(){
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View resultView = super.onCreateView(inflater, container, savedInstanceState);

		mBrowserAdapter.clear();
		mBrowserAdapter.addHeader(new ListItemData("Built In Filters"));
		mBrowserAdapter.addItem(new ListItemData("Filter by Album", "", new HubButtonOnClickListener(MusicFilterType.ByAlbum), null));
		mBrowserAdapter.addItem(new ListItemData("Filter by Artist", "", new HubButtonOnClickListener(MusicFilterType.ByArtist), null));
		mBrowserAdapter.addItem(new ListItemData("Filter by Tag", "", new HubButtonOnClickListener(MusicFilterType.ByTag), null));

		mBrowserAdapter.addHeader(new ListItemData("Tests"));
		mBrowserAdapter.addItem(new ListItemData("Repeat AC", "", null, new JourneyTestOnClickListener(
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
		mBrowserAdapter.addItem(new ListItemData("Shuffle AC, D, Persona"));
		mBrowserAdapter.notifyDataSetChanged();

		return resultView;
	}

	@Override
	protected int getNewListItemState(ListItemData itemData){
		if (itemData.playable) return STATE_PLAYABLE;
		return STATE_NONE;
	}
	@Override
	protected Drawable getDrawableFromListItemState(int state){
		if (state == STATE_PLAYABLE) return ContextCompat.getDrawable(getActivity(),R.drawable.ic_play_arrow_black_36dp);
		return null;
	}

	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle(null);
	}

	private class JourneyTestOnClickListener implements View.OnClickListener{
		private Journey journey;

		JourneyTestOnClickListener(Journey journey){
			this.journey = journey;
		}

		public void onClick(View button){
			mCommandListener.onMediaItemPlayed(new MediaBrowserCompat.MediaItem(
					new MediaDescriptionCompat.Builder().setMediaId(journey.toString()).build(), 0
			));
			/*Bundle args = new Bundle(1);
			args.putString(MusicBrowserFragment.ARG_MUSIC_FILTER, filter.toString());
			mCommandListener.navigateToNewFragment(MusicBrowserFragment.class, args);*/
		}
	}
	private class HubButtonOnClickListener implements View.OnClickListener{
		private MusicFilter filter;

		HubButtonOnClickListener(MusicFilterType toExplore){
			this.filter = new MusicFilter(MusicFilterType.Explore, toExplore.toString());
		}

		public void onClick(View button){
			Bundle args = new Bundle(1);
			args.putString(MusicBrowserFragment.ARG_MUSIC_FILTER, filter.toString());
			mCommandListener.navigateToNewFragment(MusicBrowserFragment.class, args);
		}
	}
}
