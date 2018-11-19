package com.turboturnip.turboui.activity;

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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turboui.fragment.CommandFragment;

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

		FragmentStackEntry(WeakReference<CommandFragment> fragment, Bundle args){
			this.fragment = fragment;
			this.args = args;
		}
	}
	private List<FragmentStackEntry> fragmentStack = new ArrayList<>();

	private int mItemToOpenWhenDrawerCloses = -1;
	protected Toolbar mToolbar;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private NavigationView mNavigationView;

	private boolean mToolbarInitialized;

	protected abstract Class getActivityClassForSelectedItem(int item);
	private final DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
		@Override
		public void onDrawerClosed(@NonNull View drawerView) {
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
		public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
			if (mDrawerToggle != null) mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerOpened(@NonNull View drawerView) {
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

	protected int getToolbarMenu(){
		return -1;
	}
	protected int getNavMenu(){
		return -1;
	}
	protected int getNavHeaderLayout(){
		return R.layout.nav_header;
	}
	protected int getContentViewLayout(){
		return R.layout.command_fragment_frame;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogHelper.d(TAG, "Activity onCreate");

		setContentView(getContentViewLayout());
		mNavigationView = new NavigationView(this);
		if (getNavHeaderLayout() >= 0)
			mNavigationView.inflateHeaderView(getNavHeaderLayout());
		if (getNavMenu() >= 0)
			mNavigationView.inflateMenu(getNavMenu());
		DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.START;
		mNavigationView.setLayoutParams(params);
		DrawerLayout layout = findViewById(R.id.drawer_layout);
		layout.addView(mNavigationView);
		layout.closeDrawers();


		initializeToolbar();
		initializeFromParams(savedInstanceState, getIntent());
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mToolbarInitialized) {
			throw new IllegalStateException("You must run super.initializeToolbar at " +
					"the end of your onCreate method");
		}
		if (mDrawerLayout != null)
			mDrawerLayout.addDrawerListener(mDrawerListener);

		// Whenever the fragment back stack changes, we may need to update the
		// action bar toggle: only top level screens show the hamburger-like icon, inner
		// screens - either Activities or fragments - show the "Up" icon instead.
		getSupportFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
	}

	@Override
	protected void onStop(){
		super.onStop();
		getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);

		if (mDrawerLayout != null)
			mDrawerLayout.removeDrawerListener(mDrawerListener);
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
		if (getToolbarMenu() < 0) return true;
		getMenuInflater().inflate(getToolbarMenu(), menu);
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
		// If not handled by drawerToggle, home needs to be handled by returning to moveToPrevious
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
		// Otherwise, it may return to the moveToPrevious fragment stack
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
		mToolbar = findViewById(R.id.toolbar);
		if (mToolbar == null) {
			throw new IllegalStateException("Layout is required to include a Toolbar with id " +
					"'toolbar'");
		}
		if (getToolbarMenu() >= 0)
			mToolbar.inflateMenu(getToolbarMenu());

		mDrawerLayout = findViewById(R.id.drawer_layout);
		if (mDrawerLayout != null) {

			// Create an ActionBarDrawerToggle that will handle opening/closing of the drawer:
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
					mToolbar, R.string.open_content_drawer, R.string.close_content_drawer);
			populateDrawerItems(mNavigationView);
			setSupportActionBar(mToolbar);
			updateDrawerToggle();
		} else {
			setSupportActionBar(mToolbar);
		}

		mToolbarInitialized = true;
	}

	protected int getNavMenuItemId(){
		return -1;
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
		int menuItem = getNavMenuItemId();
		if (menuItem >= 0)
			navigationView.setCheckedItem(menuItem);
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
			fragmentArguments = savedInstanceState.getParcelableArray(FRAGMENT_BUNDLE_KEY);
			if (fragmentClassNames == null || fragmentArguments == null) return;
			fragmentClasses = new Class[fragmentClassNames.length];
			for (int i = 0; i < fragmentClassNames.length; i++){
				fragmentClasses[i] = Class.forName(fragmentClassNames[i]);
			}

			for (int i = 0; i < fragmentClasses.length; i++){
				navigateToNewFragment(fragmentClasses[i], (Bundle)fragmentArguments[i]);
			}
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

		if (fragment == null || !data.equals(fragment.getArguments()) || !fragmentClass.isInstance(fragment)){
			try {
				fragment = (CommandFragment)fragmentClass.newInstance();
			}catch (InstantiationException e){
				e.printStackTrace();
				return;
			}catch (IllegalAccessException e){
				e.printStackTrace();
				return;
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
