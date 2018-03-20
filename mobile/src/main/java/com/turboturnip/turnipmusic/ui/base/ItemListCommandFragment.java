package com.turboturnip.turnipmusic.ui.base;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.utils.LogHelper;
import com.turboturnip.turnipmusic.utils.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemListCommandFragment extends MediaCommandFragment {
	private static final String TAG = LogHelper.makeLogTag(ItemListCommandFragment.class);

	static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

	protected boolean loadedItems = false;

	protected BrowseAdapter mBrowserAdapter;
	private View mErrorView;
	private View mIndeterminateProgressView;
	private TextView mErrorMessage;
	private TextView mNoItemsText;

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

		mIndeterminateProgressView = rootView.findViewById(R.id.progress_bar);
		mNoItemsText = rootView.findViewById(R.id.no_data_text);
		updateLoadedState(0);

		return rootView;
	}

	protected void checkForUserVisibleErrors(boolean forceError){
		checkForUserVisibleErrors(forceError, R.string.error_loading_media);
	}
	protected void checkForUserVisibleErrors(boolean forceError, int forceErrorString) {
		boolean showError = forceError;
		// If offline, message is about the lack of connectivity:
		/*if (!NetworkHelper.isOnline(getActivity())) {
			mErrorMessage.setText(R.string.error_no_connection);
			showError = true;
		} else {*/
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
				mErrorMessage.setText(forceErrorString);
				showError = true;
			}
		//}
		mErrorView.setVisibility(showError ? View.VISIBLE : View.GONE);
		LogHelper.d(TAG, "checkForUserVisibleErrors. forceError=", forceError,
				" showError=", showError,
				" isOnline=", NetworkHelper.isOnline(getActivity()));
	}

	protected int getNewListItemState(ListItemData data){
		return STATE_NONE;
	}
	protected Drawable getDrawableFromListItemState(int itemState){
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

		int newState = getNewListItemState(item);
		if (cachedState == STATE_INVALID || cachedState != newState){
			Drawable drawable = getDrawableFromListItemState(newState);
			if (drawable == null)
				cachedViews.playButtonView.setVisibility(View.GONE);
			else {
				cachedViews.playButtonView.setVisibility(View.VISIBLE);
				cachedViews.playImageView.setImageDrawable(drawable);
			}
			itemView.setTag(R.id.tag_mediaitem_state_cache, newState);
		}

		item.applyToViews(cachedViews);

		return itemView;
	}
	private View getListSeparatorItemView(ListItemData item, View itemView, @NonNull ViewGroup parent){
		ListSeparatorItemCachedViews cachedViews;

		if (itemView == null) {
			itemView = LayoutInflater.from(this.getActivity())
					.inflate(R.layout.media_list_header, parent, false);
			cachedViews = new ListSeparatorItemCachedViews(itemView);
			itemView.setTag(cachedViews);
		}else {
			// If it isn't null, it has itemData already
			cachedViews = (ListSeparatorItemCachedViews) itemView.getTag();
		}

		item.applyToHeaderViews(cachedViews);

		return itemView;
	}

	private class ListItemCachedViews {
		final View itemView;
		final View playButtonView;
		final TextView playTextView;
		final View intoButtonView;
		final ImageView playImageView;
		final ImageView intoImageView;
		final TextView titleView;
		final TextView subtitleView;

		ListItemCachedViews(View itemView){
			this.itemView = itemView;
			playButtonView = itemView.findViewById(R.id.play_button);
			intoButtonView = itemView.findViewById(R.id.into_button);
			playImageView = itemView.findViewById(R.id.play_drawable);
			intoImageView = itemView.findViewById(R.id.into_drawable);
			playTextView = itemView.findViewById(R.id.play_text);
			titleView = itemView.findViewById(R.id.title);
			subtitleView = itemView.findViewById(R.id.subtitle);
		}
	}
	private class ListSeparatorItemCachedViews {
		final View itemView;
		final TextView textView;

		ListSeparatorItemCachedViews(View itemView){
			this.itemView = itemView;
			textView = itemView.findViewById(R.id.header_text);
		}
	}
	public class ListItemData {
		public CharSequence title = null, subtitle = null, playText = null;
		public View.OnClickListener onPlayClick = null, onIntoClick = null;
		public boolean playable = false, browsable = false;

		public Object internalData;

		public ListItemData(){}
		public ListItemData(CharSequence title){
			this.title = title;
		}
		public ListItemData(CharSequence title, CharSequence subtitle, View.OnClickListener onIntoClick, View.OnClickListener onPlayClick){
			this.title = title;
			this.subtitle = subtitle;
			this.onIntoClick = onIntoClick;
			this.onPlayClick = onPlayClick;
			this.playable = this.onPlayClick != null;
			this.browsable = this.onIntoClick != null;
		}

		void applyToHeaderViews(ListSeparatorItemCachedViews views){
			views.textView.setText(title);
		}
		void applyToViews(ListItemCachedViews views){
			views.titleView.setText(title);
			views.subtitleView.setText(subtitle);
			views.playButtonView.setVisibility(playable ? View.VISIBLE : View.GONE);
			views.playButtonView.setOnClickListener(onPlayClick);
			if (playable && playText != null) views.playTextView.setText(playText);
			views.intoButtonView.setVisibility(browsable ? View.VISIBLE : View.GONE);
			views.intoButtonView.setOnClickListener(onIntoClick);
		}
	}

	protected void updateLoadedState(int itemCount){
		mIndeterminateProgressView.setVisibility(loadedItems ? View.GONE : View.VISIBLE);
		mNoItemsText.setVisibility((!loadedItems || itemCount > 0) ? View.GONE : View.VISIBLE);
	}

	// An adapter for showing the list of browsed MediaItem's
	protected class BrowseAdapter extends BaseAdapter {

		private static final int TYPE_ITEM = 0;
		private static final int TYPE_SEPARATOR = 1;

		private ArrayList<ListItemData> mData = new ArrayList<>();
		private List<Integer> headers = new ArrayList<>();

		BrowseAdapter(Activity context) {
		}

		public void clear(){
			mData.clear();
			headers.clear();
		}

		public void addItem(final ListItemData item) {
			mData.add(item);
		}

		public void addHeader(final ListItemData item) {
			headers.add(mData.size());
			mData.add(item);
		}

		@Override
		public int getItemViewType(int position) {
			return headers.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public ListItemData getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position){
			return false;
		}

		@Override
		public void notifyDataSetChanged(){
			super.notifyDataSetChanged();
			if (!loadedItems) loadedItems = true;
			updateLoadedState(mData.size());
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			int rowType = getItemViewType(position);
			final ListItemData item = getItem(position);

			if (rowType == TYPE_SEPARATOR) return getListSeparatorItemView(item, convertView, parent);
			else return getListItemView(item, convertView, parent);
		}
	}
}
