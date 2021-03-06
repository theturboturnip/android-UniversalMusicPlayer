package com.turboturnip.turnipmusic.frontend.roots.queue;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.backend.queue.QueueManager;
import com.turboturnip.turnipmusic.frontend.base.legacy.MusicListCommandFragment;

import java.util.ArrayList;

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
			onQueueUpdated("", new QueueManager.QueueData.CompiledData(new ArrayList<MediaSessionCompat.QueueItem>(), 0));
		else {
			onQueueUpdated("", instance.getCompiledQueueData());
		}
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
	public void onQueueUpdated(String title, QueueManager.QueueData.CompiledData compiledQueue) {
		mBrowserAdapter.clear();
		int i = 0;
		if (compiledQueue.mCurrentCompiledQueueIndex > 0) mBrowserAdapter.addHeader(new ListItemData("History"));
		for (MediaSessionCompat.QueueItem item : compiledQueue.mCompiledQueue) {
			if (i == compiledQueue.mCurrentCompiledQueueIndex) {
				mBrowserAdapter.addHeader(new ListItemData("Current Song"));
			} else if (i == compiledQueue.mCurrentCompiledQueueIndex + 1)
				mBrowserAdapter.addHeader(new ListItemData("Next Up"));
			if (item.getDescription().getMediaId() == null) {
				mBrowserAdapter.addHeader(new ListItemData(item.getDescription().getTitle()));
			} else {
				mBrowserAdapter.addItem(getDataForListItem(new MediaBrowserCompat.MediaItem(item.getDescription(),
						MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)));
			}
			i++;
		}
		mBrowserAdapter.notifyDataSetChanged();
	}
}
