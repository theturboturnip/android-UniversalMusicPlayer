package com.turboturnip.turnipmusic.ui.roots;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.ui.base.SingleFragmentActivity;

public class QueueActivity extends SingleFragmentActivity {
	protected Class getFragmentClass(){
		return QueueFragment.class;
	}

	@Override
	protected int getNavMenuItemId() {
		return R.id.navigation_queue;
	}
}
