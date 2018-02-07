package com.turboturnip.turnipmusic.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.utils.LogHelper;

/**
 * Created by Samuel on 07/02/2018.
 */

public class HubFragment extends CommandFragment {
	private static final String TAG = LogHelper.makeLogTag(HubFragment.class);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		LogHelper.d(TAG, "fragment.onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_hub, container, false);

		return rootView;
	}
}
