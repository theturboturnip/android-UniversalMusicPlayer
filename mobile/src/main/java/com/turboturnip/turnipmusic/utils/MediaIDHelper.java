package com.turboturnip.turnipmusic.utils;

import android.app.Activity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

public class MediaIDHelper {
	public static boolean isMediaItemPlaying(Activity context, MediaBrowserCompat.MediaItem mediaItem) {
		boolean isPlaying = false;

		MediaControllerCompat controller = MediaControllerCompat.getMediaController(context);
		if (controller != null && controller.getMetadata() != null) {
			String currentPlayingMediaId = controller.getMetadata().getDescription().getMediaId();
			isPlaying = currentPlayingMediaId.equals(mediaItem.getMediaId());
		}
		return isPlaying;
	}
}
