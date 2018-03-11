package com.turboturnip.turnipmusic.ui.roots;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.db.SongDatabase;
import com.turboturnip.turnipmusic.model.db.entities.JourneyEntity;
import com.turboturnip.turnipmusic.ui.base.ItemListCommandFragment;

import java.util.ArrayList;
import java.util.List;

interface JourneyReceiver{
	void getJourneys(List<Journey> journeys);
}
public class JourneyListFragment extends ItemListCommandFragment implements JourneyReceiver {
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
		new GetJourneysAsyncTask(this, db).execute();
	}
	private static class GetJourneysAsyncTask extends AsyncTask<Void, Void, List<Journey>> {
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
			for (int i = 0; i < journeyEntities.size(); i++){
				journeys.add(db.createJourney(journeyEntities.get(i)));
			}
			return journeys;
		}

		@Override
		protected void onPostExecute(List<Journey> journeys) {
			callback.getJourneys(journeys);
		}
	}

	public void getJourneys(List<Journey> journeys){
		mBrowserAdapter.clear();
		for(Journey j : journeys){
			mBrowserAdapter.addItem(
					new ListItemData(
							j.name,
							"",
							null,
							new PlayJourneyOnClickListener(j)
					)
			);
		}
		mBrowserAdapter.addItem(new ListItemData(
				"New Journey",
				"Create a new Journey",
				null,
				null
		));
		mBrowserAdapter.notifyDataSetChanged();
	}
	private class PlayJourneyOnClickListener implements View.OnClickListener{
		final Journey toPlay;
		public PlayJourneyOnClickListener(Journey toPlay){
			this.toPlay = toPlay;
		}

		@Override
		public void onClick(View view) {
			mCommandListener.onMediaItemPlayed(
					new MediaBrowserCompat.MediaItem(
							new MediaDescriptionCompat.Builder().setMediaId(toPlay.toString()).build(),
							MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
					)
			);
		}
	}

	public void createNewJourney(){
		//mCommandListener.navigateToNewFragment(JourneyEditorFragment.class, );
	}
}
