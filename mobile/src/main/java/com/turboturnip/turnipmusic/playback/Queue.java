package com.turboturnip.turnipmusic.playback;

import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.turboturnip.turnipmusic.MusicFilter;

import java.util.List;

/*public class Queue {
	private List<MediaSessionCompat.QueueItem> explicitQueue;
	MusicFilter implicitQueue;
	boolean shuffled;

	public Queue(List<MediaSessionCompat.QueueItem> list, MusicFilter filter, boolean isShuffled){
		explicitQueue = list;
		implicitQueue = filter;
		shuffled = isShuffled;
	}
	public boolean isEmpty(){
		return explicitQueue.isEmpty();
	}
	public boolean isIndexPlayable(int index) {
		return (index >= 0 && index < explicitQueue.size());
	}
	public int size(){
		return explicitQueue.size();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (o.getClass() != Queue.class) return false;
		Queue queue = (Queue)o;
		if (explicitQueue.size() != queue.explicitQueue.size()) {
			return false;
		}
		for (int i=0; i<explicitQueue.size(); i++) {
			if (explicitQueue.get(i).getQueueId() != queue.explicitQueue.get(i).getQueueId()) {
				return false;
			}
			if (!TextUtils.equals(explicitQueue.get(i).getDescription().getMediaId(),
					queue.explicitQueue.get(i).getDescription().getMediaId())) {
				return false;
			}
		}
		return true;
	}
}
*/