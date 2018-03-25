package com.turboturnip.turnipmusic.ui.roots;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.db.SongDatabase;
import com.turboturnip.turnipmusic.model.db.entities.JourneyEntity;
import com.turboturnip.turnipmusic.ui.base.ItemListCommandFragment;

import java.util.ArrayList;
import java.util.List;

public class JourneyListFragment extends ItemListCommandFragment {
	private final static String TAG = LogHelper.makeLogTag(JourneyListFragment.class);

	private final static int STATE_PLAYABLE = 1;

	@Override
	public boolean isRoot(){
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		loadedItems = false;
		updateLoadedState(0);
		SongDatabase db = SongDatabase.getInstance(getActivity());
		new GetJourneysAsyncTask(new GetJourneysAsyncTask.JourneyReceiver() {
			@Override
			public void getJourneys(List<Journey> journeys) {
				JourneyListFragment.this.getJourneys(journeys);
			}
		}, db).execute();
	}
	private static class GetJourneysAsyncTask extends AsyncTask<Void, Void, List<Journey>> {
		interface JourneyReceiver{
			void getJourneys(List<Journey> journeys);
		}

		JourneyReceiver callback;
		SongDatabase db;
		public GetJourneysAsyncTask(JourneyReceiver callback, SongDatabase db){
			this.callback = callback;
			this.db = db;
		}

		@Override
		protected List<Journey> doInBackground(Void... params) {
			List<JourneyEntity> journeyEntities = db.journeyDao().getJourneys();
			List<Journey> journeys = new ArrayList<>(journeyEntities.size());
			for (JourneyEntity e : journeyEntities)
				journeys.add(db.createJourney(e));
			return journeys;
		}

		@Override
		protected void onPostExecute(List<Journey> journeys) {
			callback.getJourneys(journeys);
		}
	}

	@Override
	protected int getNewListItemState(ListItemData data) {
		return (data.onIntoClick == null) ? STATE_NONE : STATE_PLAYABLE;
	}

	@Override
	protected Drawable getDrawableFromListItemState(int itemState) {
		switch(itemState){
			case STATE_NONE:
				return ContextCompat.getDrawable(getActivity(),
						R.drawable.ic_add_black);
			case STATE_PLAYABLE:
				return ContextCompat.getDrawable(getActivity(),
						R.drawable.ic_play_arrow_black_36dp);
			default:
				return null;
		}
	}

	private void getJourneys(List<Journey> journeys){
		mBrowserAdapter.clear();
		for(Journey j : journeys){
			LogHelper.e(TAG, j.toString());
			mBrowserAdapter.addItem(
					new ListItemData(
							j.name,
							j.stages.size() + " stages",
							new EditJourneyOnClickListener(j),
							new PlayJourneyOnClickListener(j)
					)
			);
		}
		mBrowserAdapter.addItem(new ListItemData(
				"New Journey",
				"Create a new Journey",
				null,
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Bundle data = new Bundle();
						data.putBoolean(JourneyEditFragment.IS_NEW_JOURNEY_KEY, true);
						mCommandListener.navigateToNewFragment(JourneyEditFragment.class, data);
					}
				}
		));
		mBrowserAdapter.notifyDataSetChanged();
	}
	private class EditJourneyOnClickListener implements View.OnClickListener{
		final Journey toEdit;
		public EditJourneyOnClickListener(Journey toEdit){
			this.toEdit = toEdit;
		}

		@Override
		public void onClick(View view) {
			Bundle data = new Bundle();
			data.putBoolean(JourneyEditFragment.IS_NEW_JOURNEY_KEY, false);
			data.putString(JourneyEditFragment.JOURNEY_TO_EDIT_KEY, toEdit.toString());
			mCommandListener.navigateToNewFragment(JourneyEditFragment.class, data);
		}
	}
	private class PlayJourneyOnClickListener implements View.OnClickListener{
		final Journey toPlay;
		public PlayJourneyOnClickListener(Journey toPlay){
			this.toPlay = toPlay;
		}

		@Override
		public void onClick(View view) {
			mCommandListener.onItemActioned(toPlay.toString());
		}
	}

	@Override
	protected void updateTitle() {
		mCommandListener.setToolbarTitle("Journeys");
	}
}
