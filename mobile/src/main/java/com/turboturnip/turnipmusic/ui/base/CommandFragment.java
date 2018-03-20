package com.turboturnip.turnipmusic.ui.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.ui.roots.MusicBrowserProvider;
import com.turboturnip.turnipmusic.utils.LogHelper;


public abstract class CommandFragment extends Fragment {
	private static final String TAG = LogHelper.makeLogTag(CommandFragment.class);

	// Any data sent back where the boolean assigned to this tag is true should be rerouted to the previous fragment if it exists.
	public static final String PASS_BACK_TAG = "PASSBACK";

	protected CommandFragmentListener mCommandListener = null;

	public boolean isRoot(){
		return false;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (!(context instanceof CommandFragmentListener))
			throw new RuntimeException("CommandFragments must ONLY be created as children of CommandFragmentListeners");

		mCommandListener = (CommandFragmentListener) context;
	}
	@Override
	public void onDetach(){
		super.onDetach();
		mCommandListener = null;
	}

	protected abstract void updateTitle();

	public void getDataFromChildFragment(Bundle data){
		if (!data.getBoolean(PASS_BACK_TAG, false))
			throw new RuntimeException("Data was passed back even though it didn't have the correct tag!");
	}

	public interface CommandFragmentListener {
		void setToolbarTitle(CharSequence title);
		void navigateToNewFragment(Class newFragmentClass, Bundle initData);
		void onItemSelected(String item);
		void onItemPlayed(String item);
		void getDataFromFragment(Bundle data);
		void navigateBack();
	}
}
