package com.turboturnip.turnipmusic.ui.roots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.turnipmusic.ui.base.ItemListCommandFragment;

public class JourneyListFragment extends ItemListCommandFragment {
	@Override
	public boolean isRoot(){
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View resultView = super.onCreateView(inflater, container, savedInstanceState);

		mBrowserAdapter.clear();
		mBrowserAdapter.addItem(new ListItemData("New Item", "Create a new Journey", null, new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				createNewJourney();
			}
		}));

		return resultView;
	}

	public void createNewJourney(){
		//mCommandListener.navigateToNewFragment(JourneyEditorFragment.class, );
	}
}
