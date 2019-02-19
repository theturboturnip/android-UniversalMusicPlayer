package com.turboturnip.turnipmusic.frontend.base.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.utils.MediaIDHelper;

import java.util.List;

@Deprecated
public abstract class MusicListCommandFragment extends ItemListCommandFragment {
	private static final String TAG = LogHelper.makeLogTag(MusicListCommandFragment.class);

	// Extra states for the list items to have
	public static final int STATE_PLAYABLE = 1;
	public static final int STATE_PAUSED = 2;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_QUEUED = 4;
	public static final int STATE_QUEUEABLE = 5;

	private static ColorStateList sColorStatePlaying;
	private static ColorStateList sColorStateNotPlaying;

	public static final String ARG_MUSIC_FILTER = "music_filter";
	protected MusicFilter mMusicFilter;

	@Override
    public void onStart() {
        super.onStart();
        LogHelper.d(TAG, "fragment.onStart, musicFilter=", mMusicFilter);
    }

	@Override
	public void onStop(){
		super.onStop();

		MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
		if (controller != null) {
			controller.unregisterCallback(mMediaControllerCallback);
		}
	}

	@Override
	public void connectToMediaBrowser(){
        mMusicFilter = getFilter();
        if (mMusicFilter == null) {
            mMusicFilter = new MusicFilter(mediaBrowser.getRoot());
            //LogHelper.d(TAG,"media ID was null, now ", mMediaId);
        }
        updateTitle();

		MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
		if (controller != null) {
			controller.registerCallback(mMediaControllerCallback);
		}
	}
    @Override
    public void disconnectFromMediaBrowser() {
		if (mMusicFilter != null)
        	mediaBrowser.unsubscribe(mMusicFilter.toString());
    }

    public MusicFilter getFilter() {
		Bundle args = getArguments();
		if (args != null) {
			String musicFilterString = args.getString(ARG_MUSIC_FILTER);
			if (musicFilterString != null)
				return new MusicFilter(musicFilterString);
		}
		return null;
	}

	// Receive callbacks from the MediaController. Here we update our state such as which queue
	// is being shown, the current title and description and the PlaybackState.
	protected final MediaControllerCompat.Callback mMediaControllerCallback =
			new MediaControllerCompat.Callback() {
				@Override
				public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
					super.onQueueChanged(queue);
					mBrowserAdapter.notifyDataSetChanged();
				}

				@Override
				public void onMetadataChanged(MediaMetadataCompat metadata) {
					super.onMetadataChanged(metadata);
					if (metadata == null) {
						return;
					}
					LogHelper.d(TAG, "Received metadata change to media ",
							metadata.getDescription().getMediaId());
					mBrowserAdapter.notifyDataSetChanged();
				}

				@Override
				public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
					super.onPlaybackStateChanged(state);
					LogHelper.d(TAG, "Received state change: ", state);
					checkForUserVisibleErrors(false);
					mBrowserAdapter.notifyDataSetChanged();
				}
			};

	private static void initializeColorStateLists(Context ctx) {
		sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(
				R.color.media_item_icon_not_playing));
		sColorStatePlaying = ColorStateList.valueOf(ctx.getResources().getColor(
				R.color.media_item_icon_playing));
	}
	@Override
	protected int getNewListItemState(ListItemData data){
		int state = STATE_PLAYABLE;
		MediaBrowserCompat.MediaItem mediaItem = (MediaBrowserCompat.MediaItem) data.internalData;
		data.playText = null;

		// Set state to playable first, then override to playing or paused state if needed
		if (mediaItem.isPlayable() && !mediaItem.isBrowsable()) {
			MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
			PlaybackStateCompat pbState = controller == null ? null : controller.getPlaybackState();

			if (MediaIDHelper.isMediaItemPlaying(getActivity(), mediaItem)) {
				if (pbState == null ||
						pbState.getState() == PlaybackStateCompat.STATE_ERROR) {
					state = STATE_NONE;
				} else if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
					state = STATE_PLAYING;
				} else if (pbState.getState() == PlaybackStateCompat.STATE_PAUSED){
					state = STATE_PAUSED;
				}
			}else{
				/*int explicitQueueIndex = QueueManager.getInstance().getExplicitQueueIndex(mediaItem.getMediaId());
				if (explicitQueueIndex >= 0) {
					state = STATE_QUEUED;
					data.playText = (explicitQueueIndex + 1) > 9 ? "+" : "" + (explicitQueueIndex + 1);
				}else if (pbState != null && (pbState.getState() == PlaybackStateCompat.STATE_PLAYING || pbState.getState() == PlaybackStateCompat.STATE_PAUSED))
					state = STATE_QUEUEABLE;*/
			}
		}

		return state;
	}

	@Override
	protected Drawable getDrawableFromListItemState(ListItemData data, int state) {
		Activity a = getActivity();
		if (a == null) return null;

		if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
			initializeColorStateLists(a);
		}

		if (data.actionType == ListItemData.ActionType.Browsable){
			return ContextCompat.getDrawable(a, R.drawable.ic_chevron_right_black_24dp);
		}

		switch (state) {
			case STATE_PLAYABLE: {
				Drawable playDrawable = ContextCompat.getDrawable(a,
						R.drawable.ic_play_arrow_black_36dp);
				DrawableCompat.setTintList(playDrawable, sColorStateNotPlaying);
				return playDrawable;
			}
			case STATE_PLAYING: {
				AnimationDrawable animation = (AnimationDrawable)
						ContextCompat.getDrawable(a, R.drawable.ic_equalizer_white_36dp);
				DrawableCompat.setTintList(animation, sColorStatePlaying);
				animation.start();
				return animation;
			}
			case STATE_PAUSED: {
				Drawable playDrawable = ContextCompat.getDrawable(a,
						R.drawable.ic_equalizer1_white_36dp);
				DrawableCompat.setTintList(playDrawable, sColorStatePlaying);
				return playDrawable;
			}
			case STATE_QUEUED: {
				Drawable queuedDrawable = ContextCompat.getDrawable(a, R.drawable.ic_circle_outline_black);
				DrawableCompat.setTintList(queuedDrawable, sColorStatePlaying);
				return queuedDrawable;
			}
			case STATE_QUEUEABLE: {
				Drawable queueableDrawable = ContextCompat.getDrawable(a,
						R.drawable.ic_queue_black);
				DrawableCompat.setTintList(queueableDrawable, sColorStateNotPlaying);
				return queueableDrawable;
			}
			default:
				return null;
		}
	}
	protected ListItemData getDataForListItem(final MediaBrowserCompat.MediaItem item){
		MediaDescriptionCompat description = item.getDescription();
		ListItemData itemData = new ListItemData(description.getTitle(), description.getSubtitle(), new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				checkForUserVisibleErrors(false);
				//mCommandListener.onItemActioned(item.getMediaId());
			}
		}, item.isBrowsable() ? ListItemData.ActionType.Browsable : ListItemData.ActionType.Playable);
		itemData.internalData = item;

		return itemData;
	}
}
