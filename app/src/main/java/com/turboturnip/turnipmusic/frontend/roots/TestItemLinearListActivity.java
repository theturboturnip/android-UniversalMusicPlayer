package com.turboturnip.turnipmusic.frontend.roots;

import android.content.Intent;
import android.os.Bundle;

import com.turboturnip.turboui.fragment.CommandFragment;
import com.turboturnip.turnipmusic.frontend.base.BaseActivity;

public class TestItemLinearListActivity extends BaseActivity {
    @Override
    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        CommandFragment currentFragment = getCurrentFragment();
        if (currentFragment == null)
            navigateToNewFragment(TestItemLinearListFragment.class, new Bundle());
    }
}
