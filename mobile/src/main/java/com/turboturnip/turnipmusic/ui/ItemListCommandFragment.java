package com.turboturnip.turnipmusic.ui;

import android.app.Activity;
import android.app.LauncherActivity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.utils.LogHelper;
import com.turboturnip.turnipmusic.utils.NetworkHelper;

import java.util.ArrayList;

public class ItemListCommandFragment extends CommandFragment {
	private static final String TAG = LogHelper.makeLogTag(ItemListCommandFragment.class);

	protected BrowseAdapter mBrowserAdapter;
	private View mErrorView;
	private TextView mErrorMessage;

	public static final int STATE_INVALID = -1;
	public static final int STATE_NONE = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		LogHelper.d(TAG, "fragment.onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		mErrorView = rootView.findViewById(R.id.playback_error);
		mErrorMessage = (TextView) mErrorView.findViewById(R.id.error_message);

		mBrowserAdapter = new BrowseAdapter(getActivity());

		ListView listView = (ListView) rootView.findViewById(R.id.list_view);
		listView.setAdapter(mBrowserAdapter);

		return rootView;
	}

	protected void checkForUserVisibleErrors(boolean forceError) {
		boolean showError = forceError;
		// If offline, message is about the lack of connectivity:
		if (!NetworkHelper.isOnline(getActivity())) {
			mErrorMessage.setText(R.string.error_no_connection);
			showError = true;
		} else {
			// otherwise, if state is ERROR and metadata!=null, use playback state error message:
			MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
			if (controller != null
					&& controller.getMetadata() != null
					&& controller.getPlaybackState() != null
					&& controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_ERROR
					&& controller.getPlaybackState().getErrorMessage() != null) {
				mErrorMessage.setText(controller.getPlaybackState().getErrorMessage());
				showError = true;
			} else if (forceError) {
				// Finally, if the caller requested to show error, show a generic message:
				mErrorMessage.setText(R.string.error_loading_media);
				showError = true;
			}
		}
		mErrorView.setVisibility(showError ? View.VISIBLE : View.GONE);
		LogHelper.d(TAG, "checkForUserVisibleErrors. forceError=", forceError,
				" showError=", showError,
				" isOnline=", NetworkHelper.isOnline(getActivity()));
	}

	int getNewListItemState(ListItemData data){
		return STATE_NONE;
	}
	Drawable getDrawableFromListItemState(int itemState){
		return null;
	}
	private View getListItemView(ListItemData item, View itemView, @NonNull ViewGroup parent){
		ListItemCachedViews cachedViews;

		Integer cachedState = STATE_INVALID;

		if (itemView == null) {
			itemView = LayoutInflater.from(this.getActivity())
					.inflate(R.layout.media_list_item, parent, false);
			cachedViews = new ListItemCachedViews(itemView);
			itemView.setTag(cachedViews);
		}else{
			// If it isn't null, it has itemData already
			cachedViews = (ListItemCachedViews) itemView.getTag();
			cachedState = (Integer) itemView.getTag(R.id.tag_mediaitem_state_cache);
		}

		item.applyToViews(cachedViews);

		int newState = getNewListItemState(item);
		if (cachedState == STATE_INVALID || cachedState != newState){
			Drawable drawable = getDrawableFromListItemState(newState);
			if (drawable == null)
				cachedViews.imageView.setVisibility(View.GONE);
			else {
				cachedViews.imageView.setVisibility(View.VISIBLE);
				cachedViews.imageView.setImageDrawable(drawable);
			}
			itemView.setTag(R.id.tag_mediaitem_state_cache, newState);
		}

		return itemView;
	}

	private class ListItemCachedViews {
		final View itemView;
		final ImageView imageView;
		final TextView titleView;
		final TextView subtitleView;

		ListItemCachedViews(View itemView){
			this.itemView = itemView;
			imageView = itemView.findViewById(R.id.play_eq);
			titleView = itemView.findViewById(R.id.title);
			subtitleView = itemView.findViewById(R.id.description);
		}
	}
	class ListItemData {
		CharSequence title = null, subtitle = null;
		View.OnClickListener onDrawableClick = null, onItemClick = null;

		Object internalData;

		ListItemData(){}
		ListItemData(CharSequence title, CharSequence subtitle, View.OnClickListener onItemClick, View.OnClickListener onDrawableClick){
			this.title = title;
			this.subtitle = subtitle;
			this.onItemClick = onItemClick;
			this.onDrawableClick = onDrawableClick;
		}

		void applyToViews(ListItemCachedViews views){
			views.titleView.setText(title);
			views.subtitleView.setText(subtitle);
			views.imageView.setOnClickListener(onDrawableClick);
			views.itemView.setOnClickListener(onItemClick);
		}
	}

	// An adapter for showing the list of browsed MediaItem's
	protected class BrowseAdapter extends ArrayAdapter<ListItemData> {

		BrowseAdapter(Activity context) {
			super(context, R.layout.media_list_item, new ArrayList<ListItemData>());
		}

		@NonNull
		@Override
		public View getView(int position, View itemView, @NonNull ViewGroup parent) {
			final ListItemData item = getItem(position);
			return getListItemView(item, itemView, parent);
		}
	}
}
