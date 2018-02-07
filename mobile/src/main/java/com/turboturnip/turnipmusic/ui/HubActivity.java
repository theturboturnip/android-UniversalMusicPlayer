package com.turboturnip.turnipmusic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.R;

public class HubActivity extends BrowserActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hub);

		findViewById(R.id.hub_album_button).setOnClickListener(new HubButtonOnClickListener(MusicFilter.FILTER_BY_ALBUM));
		findViewById(R.id.hub_artist_button).setOnClickListener(new HubButtonOnClickListener(MusicFilter.FILTER_BY_ALBUM));
		findViewById(R.id.hub_tags_button).setOnClickListener(new HubButtonOnClickListener(MusicFilter.FILTER_BY_ALBUM));

		super.initializeToolbar();
	}

	private class HubButtonOnClickListener implements View.OnClickListener{
		private MusicFilter filter;

		HubButtonOnClickListener(String toExplore){
			this.filter = new MusicFilter(new MusicFilter.SubFilter(MusicFilter.EXPLORE_FILTER, toExplore));
		}

		public void onClick(View button){
			Intent intent = new Intent();
			intent.setClass(HubActivity.this, MusicBrowserActivity.class);
			intent.putExtra(MusicBrowserActivity.NEW_FILTER_EXTRA, filter.toString());
			startActivity(intent);
			finish();
		}
	}
}
