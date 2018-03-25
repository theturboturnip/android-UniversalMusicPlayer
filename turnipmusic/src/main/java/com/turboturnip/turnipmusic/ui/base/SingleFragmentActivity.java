package com.turboturnip.turnipmusic.ui.base;

import android.content.Intent;
import android.os.Bundle;

import com.turboturnip.turboui.fragment.CommandFragment;

public abstract class SingleFragmentActivity extends BaseActivity {

	@Override
	protected void initializeFromParams(Bundle savedInstanceState, Intent intent){
		CommandFragment currentFragment = getCurrentFragment();
		if (currentFragment == null)
			navigateToNewFragment(getFragmentClass(), new Bundle());
	}

	@Override
	public void onItemSelected(String item) {}
	@Override
	public void onItemActioned(String item) {}

	protected abstract Class getFragmentClass();

	@Override
	public void getDataFromFragment(Bundle data) {}
}
