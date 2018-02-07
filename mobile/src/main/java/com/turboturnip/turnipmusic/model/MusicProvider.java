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

package com.turboturnip.turnipmusic.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.turboturnip.turnipmusic.MusicFilter;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class MusicProvider {

    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    private MusicProviderSource mSource;

    // Categorized caches for music track data:
    private ConcurrentMap<String, List<Integer>> mMusicListByAlbum;
	private ConcurrentMap<String, List<Integer>> mMusicListByGenre;
    private ConcurrentMap<String, Integer> mMusicListById;
    private ArrayList<Song> mSongs;

    private final Set<String> mFavoriteTracks;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider() {
        this(new DeviceMusicSource());
    }
    public MusicProvider(MusicProviderSource source) {
        mSource = source;
	    mMusicListByAlbum = new ConcurrentHashMap<>();
	    mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mSongs = new ArrayList<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

	/**
	 * Get the list of music tracks from a server and caches the track information
	 * for future reference, keying tracks by musicId and grouping by genre.
	 */
	public void retrieveMediaAsync(final Context context, final Callback callback) {
		LogHelper.d(TAG, "retrieveMediaAsync called");
		if (mCurrentState == State.INITIALIZED) {
			if (callback != null) {
				// Nothing to do, execute callback immediately
				callback.onMusicCatalogReady(true);
			}
			return;
		}

		// Asynchronously load the music catalog in a separate thread
		new AsyncTask<Void, Void, State>() {
			@Override
			protected State doInBackground(Void... params) {
				retrieveMedia(context);
				return mCurrentState;
			}

			@Override
			protected void onPostExecute(State current) {
				if (callback != null) {
					callback.onMusicCatalogReady(current == State.INITIALIZED);
				}
			}
		}.execute();
	}

	private synchronized void buildCacheLists() {
		ConcurrentMap<String, List<Integer>> newMusicListByGenre = new ConcurrentHashMap<>();
		ConcurrentMap<String, List<Integer>> newMusicListByAlbum = new ConcurrentHashMap<>();

		for (int index : mMusicListById.values()) {
			Song song = mSongs.get(index);
			String album = song.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
			List<Integer> albumList = newMusicListByAlbum.get(album);
			if (albumList == null) {
				albumList = new ArrayList<>();
				newMusicListByAlbum.put(album, albumList);
			}
			albumList.add(index);

			String genre = song.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_GENRE);
			List<Integer> genreList = newMusicListByGenre.get(genre);
			if (genreList == null) {
				genreList = new ArrayList<>();
				newMusicListByGenre.put(genre, genreList);
			}
			genreList.add(index);
		}

		mMusicListByGenre = newMusicListByGenre;
		mMusicListByAlbum = newMusicListByAlbum;
	}

	// TODO: This could grab the ArrayList used by the DeviceMusicSource internally, instead of constructing a new one
	public synchronized void retrieveMedia(Context context) {
		try {
			if (mCurrentState == State.NON_INITIALIZED) {
				mCurrentState = State.INITIALIZING;

				Iterator<Song> tracks = mSource.iterator(context);
				while (tracks.hasNext()) {
					Song item = tracks.next();
					String songID = item.getSongID();
					mMusicListById.put(songID, mSongs.size());
					mSongs.add(item);
				}
				buildCacheLists();
				mCurrentState = State.INITIALIZED;
			}
		} finally {
			if (mCurrentState != State.INITIALIZED) {
				// Something bad happened, so we reset state to NON_INITIALIZED to allow
				// retries (eg if the network connection is temporary unavailable)
				mCurrentState = State.NON_INITIALIZED;
			}
		}
	}

	public int songCount(){
		return mSongs.size();
	}

	public int getSongIndexFromID(String musicId){
		return mMusicListById.get(musicId);
	}

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }

    /**
     * Get an iterator over a shuffled collection of all songs
     */
    public Iterable<Integer> getShuffledMusic() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        List<Integer> shuffled = new ArrayList<>(mMusicListById.size());
        for (int index: mMusicListById.values()) {
            shuffled.add(index);
        }
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * Get music tracks of the given genre
     *
     */
    public List<Integer> getMusicsByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
    }

    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     *
     */
    public List<Integer> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     *
     */
    public List<Integer> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     *
     */
    public List<Integer> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with a genre containing
     * the given query.
     *
     */
    public List<Integer> searchMusicByGenre(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_GENRE, query);
    }

    private List<Integer> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<Integer> result = new ArrayList<>();
        query = query.toLowerCase(Locale.US);
        for (int index : mMusicListById.values()) {
        	Song song = mSongs.get(index);
            if (song.getMetadata().getString(metadataField).toLowerCase(Locale.US)
                .contains(query)) {
                result.add(index);
            }
        }
        return result;
    }


    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public Song getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mSongs.get(mMusicListById.get(musicId)) : null;
    }
    public Song getMusic(int index){
    	return mSongs.get(index);
    }

    public synchronized void updateMusicArt(String musicId, Bitmap albumArt, Bitmap icon) {
        Song song = getMusic(musicId);
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder(song.getMetadata())

                // set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is used, for
                // example, on the lockscreen background when the media session is active.
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)

                // set small version of the album art in the DISPLAY_ICON. This is used on
                // the MediaDescription and thus it should be small to be serialized if
                // necessary
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)

                .build();

        song.setMetadata(metadata);
    }

    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(musicId);
        } else {
            mFavoriteTracks.remove(musicId);
        }
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public boolean isFavorite(String musicId) {
        return mFavoriteTracks.contains(musicId);
    }

    public List<Integer> getFilteredSongIndices(MusicFilter filter){
	    final ArrayList<Integer> scores = new ArrayList<>();
	    ArrayList<Integer> songIndices = new ArrayList<>();
	    for (int i = 0; i < mSongs.size(); i++){
		    scores.add(filter.songStrength(mSongs.get(i)));
		    songIndices.add(i);
	    }
	    Collections.sort(songIndices, new Comparator<Integer>() {
		    @Override
		    public int compare(Integer index1, Integer index2) {
			    return scores.get(index1).compareTo(scores.get(index2));
		    }
	    });

	    int firstValidSong;
	    for (firstValidSong = 0; firstValidSong < mSongs.size(); firstValidSong++){
	    	if (scores.get(songIndices.get(firstValidSong)) >= 0) break;
	    }

	    return songIndices.subList(firstValidSong, songIndices.size());
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String musicFilter, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

	    MusicFilter parsedMusicFilter = new MusicFilter(musicFilter);

	    String exploredFilter = parsedMusicFilter.getExploreFilter();
	    if (exploredFilter != null){
	    	List<String> possibleValues = new ArrayList<>();
			if (exploredFilter.equals(MusicFilter.FILTER_BY_ALBUM)){
				LogHelper.e(TAG, "Exploring albums. Total albums: ", mMusicListByAlbum.keySet().size());
				possibleValues.addAll(mMusicListByAlbum.keySet());
			}else if (exploredFilter.equals(MusicFilter.FILTER_BY_GENRE)){
				possibleValues.addAll(mMusicListByGenre.keySet());
			}

			for (String possibleValue : possibleValues){
				mediaItems.add(createBrowsableMediaItemForPossibleFilterValue(exploredFilter, possibleValue, resources));
			}
	    }else{
	    	// We are filtering the songs, not showing what filter values exist
			List<Integer> songIndices = getFilteredSongIndices(parsedMusicFilter);

		    for (int i = 0; i < songIndices.size(); i++){
			    int songIndex = songIndices.get(i);
			    mediaItems.add(createMediaItem(mSongs.get(songIndex).getMetadata()));
		    }
	    }
        //if (!parsedMusicFilter.isBrowseable()) {
        //    return mediaItems;
        //}

        /*if (parsedMusicFilter.isRoot()) {
            mediaItems.add(createBrowsableMediaItemForRoot(resources));
        } else if (MusicFilter.EMPTY_FILTER_VALUE.equals(parsedMusicFilter.getGenreFilter())) {
            for (String genre : getGenres()) {
                mediaItems.add(createBrowsableMediaItemForGenre(genre, resources));
            }
        } else if (parsedMusicFilter.getGenreFilter() != null) {
        	LogHelper.i(TAG, musicFilter);
            String genre = parsedMusicFilter.getGenreFilter();
            for (int index : getMusicsByGenre(genre)) {
                mediaItems.add(createMediaItem(mSongs.get(index).getMetadata()));
            }
        } else {
            LogHelper.w(TAG, "Skipping unmatched filter: ", musicFilter);
        }*/

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(new MusicFilter(new MusicFilter.SubFilter(MusicFilter.FILTER_BY_GENRE)).toString())
                .setTitle(resources.getString(R.string.browse_genres))
                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
                .setIconUri(Uri.parse("android.resource://" +
                        "com.turboturnip.turnipmusic/drawable/ic_by_genre"))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForGenre(String genre,
                                                                    Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(new MusicFilter(new MusicFilter.SubFilter(MusicFilter.FILTER_BY_GENRE, genre)).toString())
                .setTitle(genre)
                .setSubtitle(resources.getString(
                        R.string.browse_musics_by_genre_subtitle, genre))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForPossibleFilterValue(String filterType, String filterValue, Resources resources){
	    MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
			    .setMediaId(new MusicFilter(new MusicFilter.SubFilter(filterType, filterValue)).toString())
			    .setTitle(filterValue)
			    .setSubtitle("")
			    .build();
	    return new MediaBrowserCompat.MediaItem(description,
			    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        return new MediaBrowserCompat.MediaItem(metadata.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    }

}
