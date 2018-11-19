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
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turboshuffle.TurboShuffleSong;
import com.turboturnip.turnipmusic.frontend.AlbumArtCache;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.backend.MusicProvider;
import com.turboturnip.turnipmusic.model.Shuffle;
import com.turboturnip.turnipmusic.model.Song;
import com.turboturnip.turnipmusic.model.shuffles.ForesightShuffle;
import com.turboturnip.turnipmusic.model.shuffles.OrderedGroupShuffle;
import com.turboturnip.turnipmusic.model.shuffles.SingleSongShuffle;

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

    public static class QueueData {
		// Known data: These have previously played and are currently being played.
		List<Song> mHistory; // This has the most recent song at the back
		Song mCurrentSong;

		// This holds the shuffles that will be played
		List<Shuffle> mUpcoming; // Item #0 = the current shuffle. Will exist if a song is playing

		// Compiled data (Immutable)
		// This is used by UI
		public static class CompiledData {
			public List<MediaSessionCompat.QueueItem> mCompiledQueue;
			public final int mCurrentCompiledQueueIndex;

			public CompiledData(List<MediaSessionCompat.QueueItem> compiled, int index){
				mCompiledQueue = Collections.unmodifiableList(compiled);
				mCurrentCompiledQueueIndex = index;
			}
		}
		private CompiledData compiledData = null;

		private static final int MAX_LOOKBACK = 10;
		synchronized CompiledData recompile() {
			int compiledIndex = -1;
			List<MediaSessionCompat.QueueItem> compiledQueue = new ArrayList<>();

			// Add History
			{
				// TODO: Use an iterator over the history for better efficiency
				int startIndex = mHistory.size() - MAX_LOOKBACK;
				if (startIndex < 0) startIndex = 0;

				for (int i = startIndex; i < mHistory.size(); i++) {
					compiledQueue.add(new MediaSessionCompat.QueueItem(mHistory.get(i).getMetadata().getDescription(), i - startIndex));
				}
			}
			// Add current Song
			if (mCurrentSong != null){
				compiledIndex = compiledQueue.size();
				compiledQueue.add(new MediaSessionCompat.QueueItem(mCurrentSong.getMetadata().getDescription(), compiledIndex));
			}
			// Add future Songs
			{
				boolean hasForesight = true;
				for (Shuffle s : mUpcoming) {
					compiledQueue.add(new MediaSessionCompat.QueueItem(new MediaDescriptionCompat.Builder().setTitle(s.getClass().getName()).build(), compiledQueue.size()));
					if (hasForesight && s instanceof ForesightShuffle){
						List<Song> futureSongs = ((ForesightShuffle) s).guaranteedSongs();
						LogHelper.e(TAG, "Future Songs Length:", futureSongs.size(), " Remaining: ", s.getLengthRemaining());
						if (futureSongs.size() < s.getLengthRemaining())
							hasForesight = false;
						else {
							for (Song song : futureSongs) {
								compiledQueue.add(new MediaSessionCompat.QueueItem(song.getMetadata().getDescription(), compiledQueue.size()));
							}
						}
					}else {
						hasForesight = false;
					}
				}
			}

			compiledData = new CompiledData(compiledQueue, compiledIndex);
			return compiledData;
		}
	}

	private final QueueData data = new QueueData();


	private static QueueManager instance = null;

	public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener) {
		if (instance != null){
			throw new RuntimeException("Tried to create a QueueManager when one already existed!");
		}
		instance = this;

        mMusicProvider = musicProvider;
        mListeners.add(listener);

        synchronized (data) {
			data.mHistory = new ArrayList<>();
			data.mCurrentSong = null;

			data.mUpcoming = new ArrayList<>();

			data.recompile();
		}
	}
    public static QueueManager getInstance(){
		return instance;
    }
    public static void addMetadataListener(MetadataUpdateListener l){
    	mListeners.add(l);
    }
    public static void removeMetadataListener(MetadataUpdateListener l){
    	mListeners.remove(l);
    }

	/*
		PUBLIC FACING FUNCTIONS
	 */
	public boolean moveToNext(){
		synchronized (data){
			if (!unsynchronizedNext()) return false;
			updateQueueInListeners(data.recompile());
		}
		return true;
	}
	// This function assumes we're already synchronized on data
	private boolean unsynchronizedNext(){
		while (data.mUpcoming.size() > 0 && data.mUpcoming.get(0).getLengthRemaining() == 0){
			data.mUpcoming.remove(0);
		}
		if (data.mUpcoming.size() == 0) return false;
		if (data.mUpcoming.get(0).getTotalLength() == -1 && data.mUpcoming.size() > 1)
			data.mUpcoming.remove(0);

		if (data.mCurrentSong != null)
		    data.mHistory.add(data.mCurrentSong);

		data.mCurrentSong = data.mUpcoming.get(0).nextSong();
		data.mUpcoming.get(0).advance();
		if (data.mUpcoming.get(0).getLengthRemaining() == 0) data.mUpcoming.remove(0);
		return true;
	}
	public boolean moveToPrevious() {
		synchronized (data){
			if (!unsynchronizedPrevious()) return false;
			updateQueueInListeners(data.recompile());
		}
		return true;
	}
	private boolean unsynchronizedPrevious(){
		if (data.mHistory.size() == 0) return false;
		Song newSong = data.mHistory.remove(data.mHistory.size() - 1);
		if (data.mCurrentSong != null)
		    data.mUpcoming.add(0, new SingleSongShuffle(data.mCurrentSong, true));
		data.mCurrentSong = newSong;
		return true;
	}
	public void moveToCompiledQueueIndex(int compiledIndex){
		synchronized (data) {
			if (compiledIndex < data.compiledData.mCurrentCompiledQueueIndex){
				// Skipping backwards to an item in history
				int moveBackBy = data.compiledData.mCurrentCompiledQueueIndex - compiledIndex;
				for (int i = 0; i < moveBackBy; i++){
					if (!unsynchronizedPrevious()) throw new RuntimeException("Tried to jump backwards outside of the history!");
				}
			}else if (compiledIndex > data.compiledData.mCurrentCompiledQueueIndex){
				for (int i = data.compiledData.mCurrentCompiledQueueIndex; i < compiledIndex; i++){
					if (!unsynchronizedNext()) throw new RuntimeException("Tried to jump forwards where no songs existed!");
				}
			}
			updateQueueInListeners(data.recompile());
		}
	}
	public MediaSessionCompat.QueueItem getCurrentCompiledQueueItem(){
		synchronized (data){
			QueueData.CompiledData compiledData = data.compiledData;
			if (compiledData.mCurrentCompiledQueueIndex == -1) return null;
			return compiledData.mCompiledQueue.get(compiledData.mCurrentCompiledQueueIndex);
		}
	}
	public Song getCurrentSong(){
		synchronized (data){
			return data.mCurrentSong;
		}
	}
	public QueueData.CompiledData getCompiledQueueData(){
		synchronized(data){
			return data.compiledData;
		}
	}
	public void addToQueue(Shuffle s){
		synchronized (data){
			data.mUpcoming.add(s);
			updateQueueInListeners(data.recompile());
		}
	}

	public void initImplicitQueue(){
		//playFromFilter(MusicFilter.emptyFilter());
	}
	public void playFromFilter(MusicFilter filter){
		synchronized (data) {
			Collection<Song> songs = mMusicProvider.getFilteredSongs(filter);
			/*SongPool pool = new SongPool(
					songs.toArray(new TurboShuffleSong[0])
			);*/
			data.mUpcoming.add(new OrderedGroupShuffle(songs.toArray(new Song[0]), true));
			data.recompile();
		}
	}

	private void updateQueueInListeners(QueueData.CompiledData data){
		for (MetadataUpdateListener l : mListeners){
			l.onQueueUpdated("Now Playing", data);
		}
	}

	public void stop(){
		synchronized (data){
			data.mHistory.clear();
			data.mCurrentSong = null;
			data.mUpcoming.clear();
			data.recompile();
		}
	}

    public void updateMetadata() {
		synchronized(data) {
			if (data.mCurrentSong == null) {
				for (MetadataUpdateListener l : mListeners)
					l.onMetadataRetrieveError();
				return;
			}
			final Song song = data.mCurrentSong;
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
						synchronized(data) {
							Song currentSong = data.mCurrentSong;
							if (currentSong == null) {
								return;
							}
							if (song == currentSong) {
								for (MetadataUpdateListener l : mListeners)
									l.onMetadataChanged(currentSong.getMetadata());
							}
						}
					}
				});
			}
		}
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);
        void onMetadataRetrieveError();
        void onCurrentQueueIndexUpdated(int queueIndex);
        void onQueueUpdated(String title, QueueManager.QueueData.CompiledData data);
    }
}
