package com.turboturnip.turnipmusic.ui.roots;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.playback.QueueManager;
import com.turboturnip.turnipmusic.ui.base.MusicListCommandFragment;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class QueueFragment extends MusicListCommandFragment implements QueueManager.MetadataUpdateListener{
	private static final String TAG = LogHelper.makeLogTag(QueueFragment.class);

	@Override
	public boolean isRoot(){
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();

		QueueManager.addMetadataListener(this);
		QueueManager instance = QueueManager.getInstance();
		if (instance == null)
			onQueueUpdated("", new ArrayList<MediaSessionCompat.QueueItem>(), 0);
		else
			onQueueUpdated("", instance.getCompiledQueue(), instance.getCompiledQueueIndex());
	}
	@Override
	public void onStop() {
		super.onStop();

		QueueManager.removeMetadataListener(this);
	}

	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle("Queue");
	}

	@Override
	public MusicFilter getFilter() {
		return null;
	}

	@Override
	public void onMetadataChanged(MediaMetadataCompat metadata) {}
	@Override
	public void onMetadataRetrieveError() {}
	@Override
	public void onCurrentQueueIndexUpdated(int queueIndex) {}
	@Override
	public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newCompiledQueue, int queueIndex) {
		mBrowserAdapter.clear();
		int i = 0;
		if (queueIndex > 0) mBrowserAdapter.addHeader(new ListItemData("History"));
		for (MediaSessionCompat.QueueItem item : newCompiledQueue) {
			if (i == queueIndex){
				mBrowserAdapter.addHeader(new ListItemData("Current Song"));
			}else if (i == queueIndex + 1)
				mBrowserAdapter.addHeader(new ListItemData("Next Up"));
			mBrowserAdapter.addItem(getDataForListItem(new MediaBrowserCompat.MediaItem(item.getDescription(),
					MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)));
			i++;
		}
		mBrowserAdapter.notifyDataSetChanged();
	}
}
