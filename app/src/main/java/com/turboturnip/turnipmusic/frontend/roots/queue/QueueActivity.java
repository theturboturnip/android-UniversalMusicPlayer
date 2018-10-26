package com.turboturnip.turnipmusic.frontend.roots.queue;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.frontend.base.SingleFragmentActivity;

public class QueueActivity extends SingleFragmentActivity {
	protected Class getFragmentClass(){
		return QueueFragment.class;
	}

	@Override
	protected int getNavMenuItemId() {
		return R.id.navigation_queue;
	}
}
