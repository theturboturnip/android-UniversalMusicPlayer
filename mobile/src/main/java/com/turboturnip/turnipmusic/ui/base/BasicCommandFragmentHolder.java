package com.turboturnip.turnipmusic.ui.base;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.ui.roots.JourneyActivity;
import com.turboturnip.turnipmusic.ui.roots.MusicBrowserActivity;
import com.turboturnip.turnipmusic.ui.roots.PlaceholderActivity;
import com.turboturnip.turnipmusic.ui.roots.QueueActivity;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class BasicCommandFragmentHolder extends AppCompatActivity implements CommandFragment.CommandFragmentListener {

	private static final String TAG = LogHelper.makeLogTag(BasicCommandFragmentHolder.class);

	private static final String FRAGMENT_TAG = "turnipmusic_fragment_container";

	private static final String IS_SAVED_STATE_KEY = "is_saved_state";
	private static final String FRAGMENT_STACK_KEY = "fragment_stack";
	private static final String FRAGMENT_BUNDLE_KEY = "fragment_bundle";

	private class FragmentStackEntry{
		WeakReference<CommandFragment> fragment;
		Bundle args;

		public FragmentStackEntry(WeakReference<CommandFragment> fragment, Bundle args){
			this.fragment = fragment;
			this.args = args;
		}
	}
	private List<FragmentStackEntry> fragmentStack = new ArrayList<>();

	private int mItemToOpenWhenDrawerCloses = -1;
	protected Toolbar mToolbar;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;

	private boolean mToolbarInitialized;

	protected abstract Class getActivityClassForSelectedItem(int item);
	private final DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
		@Override
		public void onDrawerClosed(View drawerView) {
			if (mDrawerToggle != null) mDrawerToggle.onDrawerClosed(drawerView);
			if (mItemToOpenWhenDrawerCloses >= 0) {
				Bundle extras = ActivityOptions.makeCustomAnimation(
						BasicCommandFragmentHolder.this, R.anim.fade_in, R.anim.fade_out).toBundle();

				Class activityClass = getActivityClassForSelectedItem(mItemToOpenWhenDrawerCloses);
				if (activityClass != null) {
					startActivity(new Intent(BasicCommandFragmentHolder.this, activityClass), extras);
					finish();
				}
			}
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			if (mDrawerToggle != null) mDrawerToggle.onDrawerStateChanged(newState);
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			if (mDrawerToggle != null) mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			if (mDrawerToggle != null) mDrawerToggle.onDrawerOpened(drawerView);
			if (getSupportActionBar() != null) getSupportActionBar()
					.setTitle(R.string.app_name);
		}
	};

	private final FragmentManager.OnBackStackChangedListener mBackStackChangedListener =
			new FragmentManager.OnBackStackChangedListener() {
				@Override
				public void onBackStackChanged() {
					updateDrawerToggle();
				}
			};

	@Override
	protected void onStart() {
		super.onStart();
		if (!mToolbarInitialized) {
			throw new IllegalStateException("You must run super.initializeToolbar at " +
					"the end of your onCreate method");
		}

		// Whenever the fragment back stack changes, we may need to update the
		// action bar toggle: only top level screens show the hamburger-like icon, inner
		// screens - either Activities or fragments - show the "Up" icon instead.
		getSupportFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
	}

	@Override
	protected void onStop(){
		super.onStop();
		getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerToggle != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// If not handled by drawerToggle, home needs to be handled by returning to previous
		if (item != null && item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// If the drawer is open, back will close it
		if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawers();
			return;
		}
		// Otherwise, it may return to the previous fragment stack
		FragmentManager fragmentManager = getSupportFragmentManager();
		if (fragmentManager.getBackStackEntryCount() > 0) {
			if (!fragmentStack.isEmpty())
				fragmentStack.remove(fragmentStack.size() - 1);
			fragmentManager.popBackStack();
		} else {
			// Lastly, it will rely on the system behavior for back
			super.onBackPressed();
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		mToolbar.setTitle(title);
	}

	@Override
	public void setTitle(int titleId) {
		super.setTitle(titleId);
		mToolbar.setTitle(titleId);
	}

	protected void initializeToolbar() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar == null) {
			throw new IllegalStateException("Layout is required to include a Toolbar with id " +
					"'toolbar'");
		}
		mToolbar.inflateMenu(R.menu.main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (mDrawerLayout != null) {
			NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
			if (navigationView == null) {
				throw new IllegalStateException("Layout requires a NavigationView " +
						"with id 'nav_view'");
			}

			// Create an ActionBarDrawerToggle that will handle opening/closing of the drawer:
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
					mToolbar, R.string.open_content_drawer, R.string.close_content_drawer);
			mDrawerLayout.setDrawerListener(mDrawerListener);
			populateDrawerItems(navigationView);
			setSupportActionBar(mToolbar);
			updateDrawerToggle();
		} else {
			setSupportActionBar(mToolbar);
		}

		mToolbarInitialized = true;
	}

	protected void populateDrawerItems(NavigationView navigationView){
		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
						menuItem.setChecked(true);
						mItemToOpenWhenDrawerCloses = menuItem.getItemId();
						mDrawerLayout.closeDrawers();
						return true;
					}
				});
	}

	protected void updateDrawerToggle() {
		if (mDrawerToggle == null) {
			return;
		}
		boolean isRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
		mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
		LogHelper.d(TAG, "Is currently root: ", isRoot, " because backStackEntryCount = ", getSupportFragmentManager().getBackStackEntryCount());
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
			getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
			getSupportActionBar().setHomeButtonEnabled(!isRoot);
		}
		if (isRoot) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (!savedInstanceState.getBoolean(IS_SAVED_STATE_KEY, false)) return;
		Class[] fragmentClasses;
		Parcelable[] fragmentArguments;
		try {
			String[] fragmentClassNames = savedInstanceState.getStringArray(FRAGMENT_STACK_KEY);
			fragmentClasses = new Class[fragmentClassNames.length];
			fragmentArguments = savedInstanceState.getParcelableArray(FRAGMENT_BUNDLE_KEY);
			for (int i = 0; i < fragmentClassNames.length; i++){
				fragmentClasses[i] = Class.forName(fragmentClassNames[i]);
			}

			for (int i = 0; i < fragmentClasses.length; i++){
				navigateToNewFragment(fragmentClasses[i], (Bundle)fragmentArguments[i]);
			}
		}catch (NullPointerException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}
		LogHelper.e(TAG, "Success loading from args!");
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (fragmentStack.size() == 0) return;
		outState.putBoolean(IS_SAVED_STATE_KEY, true);
		String[] fragmentClassNames = new String[fragmentStack.size()];
		Parcelable[] fragmentArguments = new Parcelable[fragmentStack.size()];
		for (int i = 0; i < fragmentStack.size(); i++){
			fragmentClassNames[i] = fragmentStack.get(i).fragment.get().getClass().getName();
			fragmentArguments[i] = fragmentStack.get(i).args;
		}
		outState.putStringArray(FRAGMENT_STACK_KEY, fragmentClassNames);
		outState.putParcelableArray(FRAGMENT_BUNDLE_KEY, fragmentArguments);
	}

	@Override
	public void setToolbarTitle(CharSequence title) {
		LogHelper.d(TAG, "Setting toolbar title to ", title);
		if (title == null) {
			title = getString(R.string.app_name);
		}
		setTitle(title);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		LogHelper.d(TAG, "onNewIntent, intent=" + intent);
		initializeFromParams(null, intent);
	}

	protected abstract void initializeFromParams(Bundle savedInstanceState, Intent intent);
	public void navigateToNewFragment(Class fragmentClass, Bundle data){
		CommandFragment fragment = getCurrentFragment();

		if (fragment == null || !fragment.getArguments().equals(data) || !fragmentClass.isInstance(fragment)){
			try {
				fragment = (CommandFragment)fragmentClass.newInstance();
			}catch (InstantiationException e){
				e.printStackTrace();
			}catch (IllegalAccessException e){
				e.printStackTrace();
			}
			fragment.setArguments(data);

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.setCustomAnimations(
					R.animator.slide_in_from_right, R.animator.slide_out_to_left,
					R.animator.slide_in_from_left, R.animator.slide_out_to_right);
			transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
			if (!fragment.isRoot())
				transaction.addToBackStack(null);
			transaction.commit();

			fragmentStack.add(new FragmentStackEntry(new WeakReference<>(fragment), data));
		}
	}
	protected CommandFragment getCurrentFragment() {
		if (fragmentStack.isEmpty()) return null;
		return fragmentStack.get(fragmentStack.size() - 1).fragment.get();
	}
	protected CommandFragment getPreviousFragment() {
		if (fragmentStack.size() < 2) return null;
		return fragmentStack.get(fragmentStack.size() - 2).fragment.get();
	}
	@Override
	public void navigateBack() {
		onBackPressed();
	}

	@Override
	public void getDataFromFragment(Bundle data) {
		if (data.getBoolean(CommandFragment.PASS_BACK_TAG, false) && getPreviousFragment() != null)
			getPreviousFragment().getDataFromChildFragment(data);
	}


}
