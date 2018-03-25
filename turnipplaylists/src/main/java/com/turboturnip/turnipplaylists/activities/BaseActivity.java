package com.turboturnip.turnipplaylists.activities;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.turboturnip.turboui.activity.BasicCommandFragmentHolder;
import com.turboturnip.turnipplaylists.R;

public abstract class BaseActivity extends BasicCommandFragmentHolder {
	private static boolean hasFfmpegBinary = false;

	@Override
	protected int getToolbarMenu() {
		return R.menu.toolbar;
	}
	@Override
	protected int getNavMenu() {
		return R.menu.drawer;
	}

	@Override
	public void onItemActioned(String item) {}
	@Override
	public void onItemSelected(String item) {}

	@Override
	protected Class getActivityClassForSelectedItem(int item) {
		return PlaylistActivity.class;
	}

	@Override
	protected void onStart() {
 		super.onStart();

		if (hasFfmpegBinary) return;
		FFmpeg ffmpeg = FFmpeg.getInstance(this);
		try {
			ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

				@Override
				public void onStart() {}

				@Override
				public void onFailure() {}

				@Override
				public void onSuccess() {}

				@Override
				public void onFinish() {}
			});
		} catch (FFmpegNotSupportedException e) {
			// Handle if FFmpeg is not supported by device
			e.printStackTrace();
			finish();
		}
		hasFfmpegBinary = true;
	}
}
