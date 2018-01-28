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

package com.turboturnip.turnipmusic.utils;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.VoiceSearchParams;
import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to help on queue related tasks.
 */
public class QueueHelper {

    private static final String TAG = LogHelper.makeLogTag(QueueHelper.class);

    private static final int RANDOM_QUEUE_SIZE = 10;

    /*public static Queue getPlayingQueue(String musicFilter,
            MusicProvider musicProvider) {

        // extract the browsing hierarchy from the media ID:
	    MusicFilter parsedMusicFilter = new MusicFilter(musicFilter);

        if (parsedMusicFilter.isEmpty() || parsedMusicFilter.isRoot()) {
            LogHelper.e(TAG, "Could not build a playing queue for this mediaId: ", musicFilter);
            return null;
        }

        LogHelper.d(TAG, "Creating playing queue for ", musicFilter);

        Iterable<Integer> tracks = null;
        // This sample only supports genre and by_search category types.
	    // TODO: This is inefficient, because the get...Filter methods are called twice
        if (parsedMusicFilter.getGenreFilter() != null) {
            tracks = musicProvider.getMusicsByGenre(parsedMusicFilter.getGenreFilter());
        } else if (parsedMusicFilter.getSearchFilter() != null) {
            tracks = musicProvider.searchMusicBySongTitle(parsedMusicFilter.getSearchFilter());
        }

        if (tracks == null) {
            LogHelper.e(TAG, "Invalid filter: ", musicFilter);
            return null;
        }

        return convertToQueue(musicProvider, tracks, parsedMusicFilter, false);
    }

    public static Queue getPlayingQueueFromSearch(String query,
            Bundle queryParams, MusicProvider musicProvider) {

        LogHelper.d(TAG, "Creating playing queue for musics from search: ", query,
            " params=", queryParams);

        VoiceSearchParams params = new VoiceSearchParams(query, queryParams);

        LogHelper.d(TAG, "VoiceSearchParams: ", params);

        if (params.isAny) {
            // If isAny is true, we will play anything. This is app-dependent, and can be,
            // for example, favorite playlists, "I'm feeling lucky", most recent, etc.
            return getRandomQueue(musicProvider);
        }

        List<Integer> result = null;
        if (params.isAlbumFocus) {
            result = musicProvider.searchMusicByAlbum(params.album);
        } else if (params.isGenreFocus) {
            result = musicProvider.getMusicsByGenre(params.genre);
        } else if (params.isArtistFocus) {
            result = musicProvider.searchMusicByArtist(params.artist);
        } else if (params.isSongFocus) {
            result = musicProvider.searchMusicBySongTitle(params.song);
        }

        // If there was no results using media focus parameter, we do an unstructured query.
        // This is useful when the user is searching for something that looks like an artist
        // to Google, for example, but is not. For example, a user searching for Madonna on
        // a PodCast application wouldn't get results if we only looked at the
        // Artist (podcast author). Then, we can instead do an unstructured search.
        if (params.isUnstructured || result == null || !result.iterator().hasNext()) {
            // To keep it simple for this example, we do unstructured searches on the
            // song title and genre only. A real world application could search
            // on other fields as well.
            result = musicProvider.searchMusicBySongTitle(query);
            if (result.isEmpty()) {
                result = musicProvider.searchMusicByGenre(query);
            }
        }

        return convertToQueue(musicProvider, result, new MusicFilter(new MusicFilter.SubFilter(MusicFilter.FILTER_BY_SEARCH, query)), false);
    }


    public static int getMusicIndexOnQueue(Queue queue,
             String mediaId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue.explicitQueue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static int getMusicIndexOnQueue(Queue queue,
             long queueId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue.explicitQueue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static Queue convertToQueue(
    		MusicProvider musicProvider,
            Iterable<Integer> tracks, MusicFilter implicitQueue, boolean wasShuffled) {

        ArrayList<MediaSessionCompat.QueueItem> explicitQueue = new ArrayList<>();
        int count = 0;
        for (int index : tracks) {
            Song song = musicProvider.getMusic(index);
            MediaMetadataCompat metadata = song.getMetadata();

            MediaMetadataCompat.Builder b = new MediaMetadataCompat.Builder(metadata).putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, )

            // We don't expect queues to change after created, so we use the item index as the
            // queueId. Any other number unique in the queue would work.
            MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(
                    metadata.getDescription(), count++);
            explicitQueue.add(item);
        }
        return new Queue(explicitQueue, implicitQueue, wasShuffled);
    }

    /**
     * Create a random queue with at most {@link #RANDOM_QUEUE_SIZE} elements.
     *
     * @param musicProvider the provider used for fetching music.
     * @return list containing {@link MediaSessionCompat.QueueItem}'s
     *
    public static Queue getRandomQueue(MusicProvider musicProvider) {
        List<Integer> result = new ArrayList<>(RANDOM_QUEUE_SIZE);
        Iterable<Integer> shuffled = musicProvider.getShuffledMusic();
        for (int index: shuffled) {
            if (result.size() == RANDOM_QUEUE_SIZE) {
                break;
            }
            result.add(index);
        }
        LogHelper.d(TAG, "getRandomQueue: result.size=", result.size());

        return convertToQueue(musicProvider, result, new MusicFilter(new MusicFilter.SubFilter(MusicFilter.FILTER_BY_SEARCH, "")), true);
    }

    /**
     * Determine if queue item matches the currently playing queue item
     *
     * @param context for retrieving the {@link MediaControllerCompat}
     * @param queueItem to compare to currently playing {@link MediaSessionCompat.QueueItem}
     * @return boolean indicating whether queue item matches currently playing queue item
     */
    public static boolean isQueueItemPlaying(Activity context,
                                             MediaSessionCompat.QueueItem queueItem) {
        // Queue item is considered to be playing or paused based on both the controller's
        // current media id and the controller's active queue item id
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(context);
        if (controller != null && controller.getPlaybackState() != null) {
            long currentPlayingQueueId = controller.getPlaybackState().getActiveQueueItemId();
            String currentPlayingMediaId = controller.getMetadata().getDescription()
                    .getMediaId();
            String itemMusicId = queueItem.getDescription().getMediaId();
            LogHelper.d(TAG, currentPlayingMediaId, "==", itemMusicId);
            if (queueItem.getQueueId() == currentPlayingQueueId
                    && currentPlayingMediaId != null
                    && TextUtils.equals(currentPlayingMediaId, itemMusicId)) {
                return true;
            }
        }
        return false;
    }

	public static boolean equals(List<MediaSessionCompat.QueueItem> list1,
                                 List<MediaSessionCompat.QueueItem> list2) {
		if (list1 == list2) {
			return true;
		}
		if (list1 == null || list2 == null) {
			return false;
		}
		if (list1.size() != list2.size()) {
			return false;
		}
		for (int i = 0; i < list1.size(); i++) {
			if (list1.get(i).getQueueId() != list2.get(i).getQueueId()) {
				return false;
			}
			if (!TextUtils.equals(list1.get(i).getDescription().getMediaId(),
					list2.get(i).getDescription().getMediaId())) {
				return false;
			}
		}
		return true;
	}
}