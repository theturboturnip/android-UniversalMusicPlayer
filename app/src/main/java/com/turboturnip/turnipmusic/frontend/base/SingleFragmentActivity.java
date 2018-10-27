package com.turboturnip.turnipmusic.frontend.base;

import android.content.Intent;
import android.os.Bundle;

import com.turboturnip.turboui.fragment.CommandFragment;

/**
 * An activity which only shows a single fragment
 */
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
