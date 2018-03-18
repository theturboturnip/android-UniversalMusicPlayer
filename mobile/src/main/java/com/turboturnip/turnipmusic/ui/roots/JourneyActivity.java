package com.turboturnip.turnipmusic.ui.roots;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.db.SongDatabase;
import com.turboturnip.turnipmusic.ui.base.BaseActivity;
import com.turboturnip.turnipmusic.utils.LogHelper;

import org.json.JSONException;

interface AsyncTaskFinishedCallback {
	void onAsyncTaskFinished();
}
public class JourneyActivity extends BaseActivity implements AsyncTaskFinishedCallback{

	private static final String TAG = LogHelper.makeLogTag(JourneyActivity.class);

	public static final String DATA_TYPE_KEY = "DATATYPE";
	public static final String OLD_JOURNEY_KEY = "OLDJOURNEY";
	public static final String NEW_JOURNEY_KEY = "NEWJOURNEY";
	public static final int SAVE_JOURNEY_REQUEST_TYPE = 0;

	@Override
	protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
		navigateToNewFragment(JourneyListFragment.class, new Bundle());
	}

	@Override
	public void onMediaItemSelected(MediaBrowserCompat.MediaItem filter) {}

	@Override
	public void getDataFromFragment(Bundle data) {
		super.getDataFromFragment(data);

		int dataType = data.getInt(DATA_TYPE_KEY, -1);
		switch(dataType){
			case SAVE_JOURNEY_REQUEST_TYPE: {
				Journey newJourney, initialJourney;
				try {
					newJourney = new Journey(data.getString(NEW_JOURNEY_KEY));
					String initialJourneyJSON = data.getString(OLD_JOURNEY_KEY, null);
					initialJourney = (initialJourneyJSON == null) ? null : new Journey(initialJourneyJSON);
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
				SongDatabase db = SongDatabase.getInstance(this);
				new SaveJourneyAsyncTask(this, db, newJourney, initialJourney).execute();
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onAsyncTaskFinished() {
		navigateBack();
	}

	private static class SaveJourneyAsyncTask extends AsyncTask<Void, Void, Void>{
		private final AsyncTaskFinishedCallback callback;
		private final SongDatabase db;
		private final Journey newJourney, initialJourney;

		public SaveJourneyAsyncTask(AsyncTaskFinishedCallback callback, SongDatabase db, Journey newJourney, Journey initialJourney){
			this.db = db;
			this.newJourney = newJourney;
			this.initialJourney = initialJourney;
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			if (initialJourney != null && !initialJourney.name.equals(newJourney.name)){
				db.renameJourney(newJourney, initialJourney);
			}else{
				db.insertJourney(newJourney);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void status) {
			callback.onAsyncTaskFinished();
		}
	}
}