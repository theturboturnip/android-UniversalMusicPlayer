package com.turboturnip.turnipmusic.ui.roots;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.db.SongDatabase;
import com.turboturnip.turnipmusic.ui.base.BaseActivity;

import org.json.JSONException;

public class JourneyActivity extends BaseActivity {

	private static final String TAG = LogHelper.makeLogTag(JourneyActivity.class);

	public static final String DATA_TYPE_KEY = "DATATYPE";
	public static final String NEW_JOURNEY_KEY = "NEWJOURNEY";
	public static final int SAVE_JOURNEY_REQUEST_TYPE = 0;

	@Override
	protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
		navigateToNewFragment(JourneyListFragment.class, new Bundle());
	}

	@Override
	public void onItemSelected(String item) {}

	@Override
	public void getDataFromFragment(Bundle data) {
		super.getDataFromFragment(data);

		int dataType = data.getInt(DATA_TYPE_KEY, -1);
		switch(dataType){
			case SAVE_JOURNEY_REQUEST_TYPE: {
				Journey newJourney;
				try {
					newJourney = new Journey(data.getString(NEW_JOURNEY_KEY));
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
				SongDatabase db = SongDatabase.getInstance(this);
				new SaveJourneyAsyncTask(new SaveJourneyFinishedCallback() {
					@Override
					public void onJourneySaved() {
						Toast.makeText(JourneyActivity.this, "Saved Journey", Toast.LENGTH_LONG).show();
					}
				}, db, newJourney).execute();
				break;
			}
			default:
				break;
		}
	}

	@Override
	protected int getNavMenuItemId() {
		return R.id.navigation_journeys;
	}

	private static class SaveJourneyAsyncTask extends AsyncTask<Void, Void, Void>{
		private final SaveJourneyFinishedCallback callback;
		private final SongDatabase db;
		private final Journey newJourney;

		public SaveJourneyAsyncTask(SaveJourneyFinishedCallback callback, SongDatabase db, Journey newJourney){
			this.db = db;
			this.newJourney = newJourney;
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			db.insertJourney(newJourney);
			return null;
		}

		@Override
		protected void onPostExecute(Void status) {
			callback.onJourneySaved();
		}
	}

	private interface SaveJourneyFinishedCallback{
		void onJourneySaved();
	}
}
