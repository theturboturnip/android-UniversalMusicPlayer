package com.turboturnip.turnipmusic.ui.roots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.ui.base.CommandFragment;

public class PlaceholderFragment extends CommandFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_placeholder, container, false);
	}
}
