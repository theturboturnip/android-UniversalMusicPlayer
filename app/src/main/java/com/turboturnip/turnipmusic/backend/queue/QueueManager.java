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

package com.turboturnip.turnipmusic.backend.queue;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turboshuffle.TurboShuffleSong;
import com.turboturnip.turnipmusic.frontend.AlbumArtCache;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.backend.MusicProvider;
import com.turboturnip.turnipmusic.model.Song;

import java.util.ArrayList;
import java.util.Collection;
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
    private static List<MetadataUpdateListener> mListeners = new ArrayList<>();
    private Resources mResources;

    // "Now playing" queue:
	private List<Song> mHistory;
    private List<Song> mExplicitQueue;
    private ImplicitQueue mImplicitQueue;
    private Song mCurrentSong;
    private int mCurrentCompiledQueueIndex;
    private static final int MAX_LOOKBACK = 10;

    //private ConstJourney journey;

	private List<MediaSessionCompat.QueueItem> mCompiledQueue;

	//private static QueueManager instance = null;

	public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener) {
		/*if (instance != null){
			throw new RuntimeException("Tried to create a QueueManager when one already existed!");
		}
		instance = this;*/

        mMusicProvider = musicProvider;
        mListeners.add(listener);
        mResources = resources;

        mCompiledQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mExplicitQueue = Collections.synchronizedList(new ArrayList<Song>());
        mHistory = Collections.synchronizedList(new ArrayList<Song>());
		mImplicitQueue = null;
	    mCurrentCompiledQueueIndex = -1;
		mCurrentSong = null;
    }
    /*public static QueueManager getInstance(){
		return instance;
    }*/
    public static void addMetadataListener(MetadataUpdateListener l){
    	mListeners.add(l);
    }
    public static void removeMetadataListener(MetadataUpdateListener l){
    	mListeners.remove(l);
    }

    public int getExplicitQueueIndex(String songLibraryId){
    	Song s = mMusicProvider.getSong(songLibraryId);
    	if (s == null) return -1;
	    // Uncomment this to make things in the implicit queue look like they are explicitly queued.
    	/*int index = mExplicitQueue.indexOf(s);
    	if (index < 0 && mImplicitQueue != null && mImplicitQueue.nextSong().equals(s)) return mExplicitQueue.size();*/
    	return mExplicitQueue.indexOf(s);
    }
	public boolean next(){
		return takeNewSongFromExplicitQueue() || takeNewSongFromImplicitQueue();
	}
	public boolean previous(){
		return takeNewSongFromHistory();
	}
	boolean addToExplicitQueue(Context context, Song song) {
		if (mCurrentSong == null){
			// If we aren't playing anything, just take this one
			mCurrentSong = song;
			mImplicitQueue.onSongPlayed(song);
			updateCompiledQueue();
			return true;
		}else if (mCurrentSong.equals(song)){
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
		Toast.makeText(context, "Added "+song.getMetadata().getDescription().getTitle()+" to the queue.", Toast.LENGTH_SHORT).show();
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
		else {
			mCurrentCompiledQueueIndex++;
			for (MetadataUpdateListener l : mListeners)
				l.onQueueUpdated("Now Playing", mCompiledQueue, mCurrentCompiledQueueIndex);
		}
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
		mCurrentSong = mImplicitQueue.nextSong();
		if (mCurrentSong == null)
			return false;
		mImplicitQueue.onSongPlayed(mCurrentSong);
		updateCompiledQueue();
		return true;
	}
	private void updateCompiledQueue(){
		ArrayList<MediaSessionCompat.QueueItem> newCompiledQueue = new ArrayList<>();
		final int oldCompiledQueueIndex = mCurrentCompiledQueueIndex;
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
		for (MetadataUpdateListener l : mListeners)
			l.onQueueUpdated("Now Playing", mCompiledQueue, mCurrentCompiledQueueIndex);
		// TODO: Is this the right behaviour?
		//if (oldCompiledQueueIndex != mCurrentCompiledQueueIndex)
		//for(MetadataUpdateListener l : mListeners)
		//	l.onCurrentQueueIndexUpdated(mCurrentCompiledQueueIndex);
	}
	private MediaSessionCompat.QueueItem queueItemFromSong(Song song, int queueIndex) {
		return new MediaSessionCompat.QueueItem(song.getMetadata().getDescription(), queueIndex);
	}

	public Song getCurrentSong(){
		return mCurrentSong;
	}
	public MediaSessionCompat.QueueItem getCurrentCompiledQueueItem(){
		if (mCurrentCompiledQueueIndex < 0 || mCompiledQueue.size() == 0) return null;
		return mCompiledQueue.get(mCurrentCompiledQueueIndex);
	}
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

	/*void startJourney(Journey newJourney){
		AsyncHelper.ThrowIfOnMainThread("startJourney()");
		journey = new ConstJourney(newJourney);
		setJourneyStage(0);
	}
	void setJourneyStage(int stageIndex) {
		AsyncHelper.ThrowIfOnMainThread("setJourneyStage()");
		LogHelper.i(TAG, journey.toString());
		SongPool[] pools = new SongPool[journey.stages[stageIndex].pools.size()];
		for (int i = 0; i < journey.stages[stageIndex].pools.size(); i++){
			// TODO: Support collective filters
			LogHelper.e(TAG, journey.stages[stageIndex].pools.get(i).filters.get(0).filterValue);
			Collection<Song> songs = mMusicProvider.getFilteredSongs(journey.stages[stageIndex].pools.get(i).filters.get(0));
			for(Song s : songs){
				LogHelper.e(TAG, s != null);
			}
			pools[i] = new SongPool(
					songs.toArray(new TurboShuffleSong[songs.size()])
			);
		}
		mImplicitQueue =
				(journey.stages[stageIndex].playType == Journey.Stage.PlayType.Repeat) ?
						new OrderedImplicitQueue() : new ShuffledImplicitQueue();
		mImplicitQueue.initialize(pools);
		updateCompiledQueue();
	}*/

	public void initImplicitQueue(){
		/*startJourney(
				new Journey(
						"",
						new Journey.Stage(
								"",
								Journey.Stage.PlayType.Repeat,
								0,
								null,
								new CompositeMusicFilter()
						)
				)
		);*/
		Collection<Song> songs = mMusicProvider.getFilteredSongs(MusicFilter.emptyFilter());
		for(Song s : songs){
			LogHelper.e(TAG, s != null);
		}
		SongPool pool = new SongPool(
				songs.toArray(new TurboShuffleSong[0])
		);
		mImplicitQueue = new OrderedImplicitQueue();
		mImplicitQueue.initialize(new SongPool[]{pool});
		updateCompiledQueue();
	}
	public void playFromFilter(MusicFilter filter){
		Collection<Song> songs = mMusicProvider.getFilteredSongs(filter);
		SongPool pool = new SongPool(
				songs.toArray(new TurboShuffleSong[0])
		);
		mImplicitQueue = new OrderedImplicitQueue();
		mImplicitQueue.initialize(new SongPool[]{pool});
		updateCompiledQueue();
	}

	public void stop(){
		mCurrentSong = null;
		mExplicitQueue.clear();
		mHistory.clear();
		updateCompiledQueue();
	}

    public void updateMetadata() {
        if (mCurrentSong == null) {
	        for (MetadataUpdateListener l : mListeners)
		        l.onMetadataRetrieveError();
            return;
        }
        final Song song = getCurrentSong();
        MediaMetadataCompat metadata = song.getMetadata();

        LogHelper.e(TAG, "Total Queue Listeners for updateMetadata: ", mListeners.size());
	    for (MetadataUpdateListener l : mListeners)
		    l.onMetadataChanged(metadata);

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (metadata.getDescription().getIconBitmap() == null &&
                metadata.getDescription().getIconUri() != null) {
            String albumUri = metadata.getDescription().getIconUri().toString();
            AlbumArtCache.getInstance().fetch(albumUri, new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    mMusicProvider.updateMusicArt(song.getLibraryId(), bitmap, icon);

                    // If we are still playing the same music, notify the listeners:
                    Song currentSong = getCurrentSong();
                    if (currentSong == null) {
                        return;
                    }
                    if (song == currentSong) {
	                    for (MetadataUpdateListener l : mListeners)
		                    l.onMetadataChanged(currentSong.getMetadata());
                    }
                }
            });
        }
    }
    public final List<MediaSessionCompat.QueueItem> getCompiledQueue(){
		return mCompiledQueue;
    }
    public int getCompiledQueueIndex(){
    	return mCurrentCompiledQueueIndex;
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);
        void onMetadataRetrieveError();
        void onCurrentQueueIndexUpdated(int queueIndex);
        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newCompiledQueue, int queueIndex);
    }
}
