/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.turboturnip.turnipmusic.playback;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turboshuffle.TurboShuffleSong;
import com.turboturnip.turnipmusic.AlbumArtCache;
import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.model.Song;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
public class QueueManager {
    private static final String TAG = LogHelper.makeLogTag(QueueManager.class);

    private MusicProvider mMusicProvider;
    private MetadataUpdateListener mListener;
    private Resources mResources;

    // "Now playing" queue:
	private List<Song> mHistory;
    private List<Song> mExplicitQueue;
    private ImplicitQueue mImplicitQueue;
    private Song mCurrentSong;
    private int mCurrentCompiledQueueIndex;
    private static final int MAX_LOOKBACK = 10;

	private List<MediaSessionCompat.QueueItem> mCompiledQueue;

	public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener) {
        mMusicProvider = musicProvider;
        mListener = listener;
        mResources = resources;

        mCompiledQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mExplicitQueue = Collections.synchronizedList(new ArrayList<Song>());
        mHistory = Collections.synchronizedList(new ArrayList<Song>());
        mImplicitQueue = new OrderedImplicitQueue();
	    mCurrentCompiledQueueIndex = -1;
		mCurrentSong = null;
    }

	public boolean next(){
		return takeNewSongFromExplicitQueue() || takeNewSongFromImplicitQueue();
	}
	public boolean previous(){
		return takeNewSongFromHistory();
	}
	public boolean addToExplicitQueue(Context context, Song song) {
		if (mCurrentSong == null){
			// If we aren't playing anything, just take this one
			mCurrentSong = song;
			mImplicitQueue.onSongPlayed(song);
			updateCompiledQueue();
			return true;
		}else if (mExplicitQueue.contains(song)){
			// If this is in the explicit queue, promote it to the current song.
			mHistory.add(mCurrentSong);
			mCurrentSong = song;
			// Cast it to object, so the explicit queue knows not to treat it as a removeAt(songIndex).
			mExplicitQueue.remove(song);
			updateCompiledQueue();
			return true;
		}
		Toast.makeText(context, "Added "+mMusicProvider.getMusic(song.getId()).getMetadata().getDescription().getTitle()+" to the queue.", Toast.LENGTH_SHORT).show();
		mExplicitQueue.add(song);
		updateCompiledQueue();
		return false;
	}
	private boolean takeNewSongFromExplicitQueue(){
		if (mExplicitQueue.size() == 0) return false;
		if (mCurrentSong != null)
			mHistory.add(mCurrentSong);
		mCurrentSong = mExplicitQueue.remove(0);
		mImplicitQueue.onSongPlayed(mCurrentSong);
		if (mExplicitQueue.size() == 0)
			updateCompiledQueue();
		else
			mCurrentCompiledQueueIndex++;
		return true;
	}
	private boolean takeNewSongFromHistory(){
		if (mHistory.size() == 0) return false;
		if (mCurrentSong != null)
			mExplicitQueue.add(0, mCurrentSong);
		mCurrentSong = mHistory.remove(mHistory.size() - 1);
		updateCompiledQueue();
		return true;
	}
	private boolean takeNewSongFromImplicitQueue(){
		if (mExplicitQueue.size() > 0)
			LogHelper.e(TAG, "Taking song from implicit queue when the explicit queue is not empty!!!");
		if (mCurrentSong != null)
			mHistory.add(mCurrentSong);
		mCurrentSong = (Song)mImplicitQueue.nextSong();
		if (mCurrentSong == null)
			return false;
		mImplicitQueue.onSongPlayed(mCurrentSong);
		updateCompiledQueue();
		return true;
	}
	private void updateCompiledQueue(){
		ArrayList<MediaSessionCompat.QueueItem> newCompiledQueue = new ArrayList<>();
		mCurrentCompiledQueueIndex = 0;

		// Add songs from the history
		int historyStartingIndex = mHistory.size() - MAX_LOOKBACK;
		if (historyStartingIndex < 0) historyStartingIndex = 0;
		for (int i = historyStartingIndex; i < mHistory.size(); i++){
			newCompiledQueue.add(queueItemFromSong(mHistory.get(i), mCurrentCompiledQueueIndex));
			mCurrentCompiledQueueIndex += 1;
		}
		if (mCurrentSong != null) {
			newCompiledQueue.add(queueItemFromSong(mCurrentSong, mCurrentCompiledQueueIndex));
			// Add songs from the explicit queue
			for (int i = 0; i < mExplicitQueue.size(); i++) {
				newCompiledQueue.add(queueItemFromSong(mExplicitQueue.get(i), mCurrentCompiledQueueIndex + i + 1));
			}

			// If we don't have any future songs, get one from the implicit queue
			if (mCurrentCompiledQueueIndex == newCompiledQueue.size() - 1)
				newCompiledQueue.add(queueItemFromSong(mImplicitQueue.nextSong(), newCompiledQueue.size()));
		}

		LogHelper.i(TAG, "updating compiled queue: [");
		for (MediaSessionCompat.QueueItem item : newCompiledQueue)
			LogHelper.i(TAG, "\t", (item.getQueueId() == mCurrentCompiledQueueIndex) ? "  > " : "    ", item.getDescription().getMediaId());
		LogHelper.i(TAG, "]");

		mCompiledQueue = newCompiledQueue;
		mListener.onQueueUpdated("Now Playing", mCompiledQueue);
		mListener.onCurrentQueueIndexUpdated(mCurrentCompiledQueueIndex);
	}
	private MediaSessionCompat.QueueItem queueItemFromSong(Song song, int queueIndex) {
		return new MediaSessionCompat.QueueItem(mMusicProvider.getMusic(song.getId()).getMetadata().getDescription(), queueIndex);
	}

	public Song getCurrentSong(){
		return mCurrentSong;
	}
	public MediaSessionCompat.QueueItem getCurrentCompiledQueueItem(){
		if (mCurrentCompiledQueueIndex < 0 || mCompiledQueue.size() == 0) return null;
		return mCompiledQueue.get(mCurrentCompiledQueueIndex);
	}
    // TODO: startJourney()
	public void updateHistoryFromNewCompiledQueueIndex(int newCompiledQueueIndex){
		if (newCompiledQueueIndex < 0 || mCurrentSong == null || mCurrentCompiledQueueIndex < 0) return;
		if (newCompiledQueueIndex == mCurrentCompiledQueueIndex) return;
		if (newCompiledQueueIndex >= mCompiledQueue.size()){
			LogHelper.e(TAG, "updateHistoryFromCompiledQueueIndex called with a higher index than possible!");
			newCompiledQueueIndex = mCompiledQueue.size() - 1;
		}

		if (newCompiledQueueIndex < mCurrentCompiledQueueIndex){
			// We've gone backwards, move all the history up to that point into the current song index and explicit queue.
			int startingPoint = mHistory.size() - (mCurrentCompiledQueueIndex - newCompiledQueueIndex);
			if (startingPoint < 0) startingPoint = 0;
			int endingPoint = mHistory.size();
			for (int i = startingPoint; i < endingPoint; i++) {
				mExplicitQueue.add(mCurrentSong);
				mCurrentSong = mHistory.get(i);
			}
			mHistory.removeAll(mHistory.subList(startingPoint, endingPoint));
			mCurrentCompiledQueueIndex = newCompiledQueueIndex;
		}else{
			while (mCurrentCompiledQueueIndex < newCompiledQueueIndex){
				mHistory.add(mCurrentSong);
				if (mExplicitQueue.size() > 0)
					mCurrentSong = mExplicitQueue.remove(0);
				else {
					mCurrentSong = mImplicitQueue.nextSong();
					if (mCurrentSong == null){
						LogHelper.e(TAG, "updateHistoryFromCompiledQueueIndex had a future index which is no longer valid!");
						break;
					}
				}
				mCurrentCompiledQueueIndex += 1;
			}
		}
		updateCompiledQueue();
	}
	public void updateHistoryFromMediaIDFromCompiledQueue(String mediaID){
		for (int i = 0; i < mCompiledQueue.size(); i++){
			if (mCompiledQueue.get(i).getDescription().getMediaId().equals(mediaID)) {
				updateHistoryFromNewCompiledQueueIndex(i);
				return;
			}
		}
	}

	public void setNewImplicitQueueFilter(MusicFilter filter){
		List<Song> songs = mMusicProvider.getFilteredSongs(filter);
		mImplicitQueue.initialize(
					new SongPool[]{
						new SongPool(songs.toArray(new TurboShuffleSong[songs.size()]))
					}
				);
		updateCompiledQueue();
	}
	public void initImplicitQueue(){
		setNewImplicitQueueFilter(new MusicFilter(new MusicFilter.SubFilter(MusicFilter.FILTER_EMPTY, "")));
	}

	/*private void moveOn(int newSong) {
		if (mCurrentSongIndex >= 0) {
			mHisory.add(mCurrentSongIndex);
		}
		mImplicitQueue.onIndexPlayed(newIndex);
	}*/

    /*private void setCurrentQueueIndex(int index) {
        if (index >= 0 && index < mPlayingQueue.size()) {
            mCurrentIndex = index;
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
    }

    public boolean setCurrentQueueItem(long queueId) {
        // set the current index on queue from the queue Id:
        int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, queueId);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean setCurrentQueueItem(String mediaId) {
        // set the current index on queue from the music Id:
        int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, mediaId);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {
            // skip forwards when in last song will cycle back to start of the queue
            index %= mPlayingQueue.size();
        }
        if (!mPlayingQueue.isIndexPlayable(index)) {
            LogHelper.e(TAG, "Cannot increment queue index by ", amount,
                    ". Current=", mCurrentIndex, " queue length=", mPlayingQueue.size());
            return false;
        }
        mCurrentIndex = index;
        return true;
    }

    /*public boolean setQueueFromSearch(String query, Bundle extras) {
        Queue queue =
                QueueHelper.getPlayingQueueFromSearch(query, extras, mMusicProvider);
        setCurrentQueue(mResources.getString(R.string.search_queue_title), queue);
        updateMetadata();
        return queue != null && !queue.isEmpty();
    }*/

    /*public void setRandomQueue() {
        setCurrentQueue(mResources.getString(R.string.random_queue_title),
                QueueHelper.getRandomQueue(mMusicProvider));
        updateMetadata();
    }*/

    /*public void setQueueFromMusic(String mediaId) {
        LogHelper.d(TAG, "setQueueFromMusic", mediaId);

        // The mediaId used here is not the unique musicId. This one comes from the
        // MediaBrowser, and is actually a "hierarchy-aware mediaID": a concatenation of
        // the hierarchy in MediaBrowser and the actual unique musicID. This is necessary
        // so we can build the correct playing queue, based on where the track was
        // selected from.
        boolean canReuseQueue = false;
        if (isSameBrowsingCategory(mediaId)) {
            canReuseQueue = setCurrentQueueItem(mediaId);
        }
        if (!canReuseQueue) {
            String queueTitle = mResources.getString(R.string.browse_musics_by_genre_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId));
            setCurrentQueue(queueTitle,
                    QueueHelper.getPlayingQueue(mediaId, mMusicProvider), mediaId);
        }
        updateMetadata();
    }*/

    /*public MediaSessionCompat.QueueItem getCurrentMusic() {
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
            return null;
        }
        return mPlayingQueue.get(mCurrentIndex);
    }*/

    /*public int getCurrentQueueSize() {
        if (mPlayingQueue == null) {
            return 0;
        }
        return mPlayingQueue.size();
    }*/

    /*protected void setCurrentQueue(String title, Queue newQueue) {
        setCurrentQueue(title, newQueue, null);
    }*/

    /*protected void setCurrentQueue(String title, Queue newQueue,
                                   String initialMediaId) {
        mPlayingQueue = newQueue;
        int index = 0;
        if (initialMediaId != null) {
            index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, initialMediaId);
        }
        mCurrentIndex = Math.max(index, 0);
        mListener.onQueueUpdated(title, newQueue);
    }*/

    public void updateMetadata() {
        if (mCurrentSong == null) {
            mListener.onMetadataRetrieveError();
            return;
        }
        final Song song = getCurrentSong();
        MediaMetadataCompat metadata = song.getMetadata();

        mListener.onMetadataChanged(metadata);

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (metadata.getDescription().getIconBitmap() == null &&
                metadata.getDescription().getIconUri() != null) {
            String albumUri = metadata.getDescription().getIconUri().toString();
            AlbumArtCache.getInstance().fetch(albumUri, new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    mMusicProvider.updateMusicArt(song.getId(), bitmap, icon);

                    // If we are still playing the same music, notify the listeners:
                    Song currentSong = getCurrentSong();
                    if (currentSong == null) {
                        return;
                    }
                    if (song == currentSong) {
                        mListener.onMetadataChanged(currentSong.getMetadata());
                    }
                }
            });
        }
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);
        void onMetadataRetrieveError();
        void onCurrentQueueIndexUpdated(int queueIndex);
        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newCompiledQueue);
    }
}
