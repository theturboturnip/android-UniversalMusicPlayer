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

package com.turboturnip.turnipmusic.backend;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.model.Album;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;
import com.turboturnip.turnipmusic.model.Song;
import com.turboturnip.turnipmusic.model.db.SongDatabase;
import com.turboturnip.turnipmusic.utils.AsyncHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class MusicProvider {

    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    private MusicProviderSource mSource;

    private static SongDatabase mDatabase;

    private ConcurrentMap<String, Song> mSongListByLibraryId;
	private ConcurrentMap<String, Album> mAlbumListByLibraryId;

    private Context context;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface MusicCatalogCallback {
        void onMusicCatalogReady(boolean success);
    }
	public interface FilteredSongCallback {
		void getFilteredSongs(Collection<Song> success);
	}
	public interface GetChildrenCallback {
    	void getChildren(List<MediaBrowserCompat.MediaItem> children);
	}

    public MusicProvider(Context c) {
        this(c, new DeviceMusicSource());
    }
    public MusicProvider(Context c, MusicProviderSource source) {
        this.context = c;
        mSource = source;

        mSongListByLibraryId = new ConcurrentHashMap<>();
        mAlbumListByLibraryId = new ConcurrentHashMap<>();

        /*if (instance != null)
        	throw new RuntimeException("Tried to create a new MusicProvider when one already existed!");
        instance = this;*/
    }

    /*public static MusicProvider getInstance(Context context){
    	if (instance == null)
    		new MusicProvider(context);
    	return instance;
    }*/

	/**
	 * Get the list of music tracks from a server and caches the track information
	 * for future reference, keying tracks by songLibraryId and grouping by genre.
	 */
	public void retrieveMediaAsync(final Context context, final MusicCatalogCallback callback) {
		LogHelper.d(TAG, "retrieveMediaAsync called");


		// Asynchronously load the music catalog in a separate thread
		new AsyncRetrieveMediaTask(callback).execute(this);
	}
	private static class AsyncRetrieveMediaTask extends AsyncTask<MusicProvider, Void, Void>{
		private final MusicCatalogCallback callback;

		AsyncRetrieveMediaTask(MusicCatalogCallback callback){
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(MusicProvider... params) {
			if (params[0].mCurrentState == State.INITIALIZED) {
				if (callback != null) {
					// Nothing to do, execute callback immediately
					callback.onMusicCatalogReady(true);
				}
				return null;
			}
			params[0].retrieveMedia();
			if (callback != null) {
				callback.onMusicCatalogReady(params[0].mCurrentState == State.INITIALIZED);
			}
			return null;
		}
	}

	synchronized void retrieveMedia() {
		try {
			if (mCurrentState == State.NON_INITIALIZED) {
				mCurrentState = State.INITIALIZING;

				mDatabase = SongDatabase.getInstance(context);

				for (Album album : mSource.albums(context)){
				    mAlbumListByLibraryId.put(album.libraryId, album);
                }

				for(Song song : mSource.songs(context, mAlbumListByLibraryId, mDatabase)) {
					mSongListByLibraryId.put(song.getLibraryId(), song);
				}

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

    public Song getSong(String songLibraryId) {
    	return mSongListByLibraryId.containsKey(songLibraryId) ? mSongListByLibraryId.get(songLibraryId) : null;
    }

    public synchronized void updateMusicArt(String songLibraryId, Bitmap albumArt, Bitmap icon) {
        Song song = getSong(songLibraryId);
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

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }


    public Collection<Song> getFilteredSongs(MusicFilter filter){
	    AsyncHelper.ThrowIfOnMainThread("getFilteredSongs()");
    	List<Song> songs = new ArrayList<>();
	    List<String> libraryIds;
	    switch(filter.filterType){
		    case ByAlbum:
				libraryIds = mSource.songMediaIdsForAlbumLibraryId(context, filter.filterValue);//mDatabase.songDao().getSongIdsInAlbum(mDatabase.albumDao().getAlbumByName(filter.filterValue).getId());
			    break;
		    case ByTag:
		    	//ids = mDatabase.songTagJoinDao().getSongIdsForTag(mDatabase.tagDao().getTag(filter.filterValue).id);
	            //break;
		    case Search:
                //libraryIds = mDatabase.songDao().orderedSearchForIds(filter.filterValue);
		    	//break;
		    case ByArtist:
                libraryIds = new ArrayList<>();
		    	break;
		    default:
			    return mSongListByLibraryId.values();
	    }
	    for(String id : libraryIds)
	    	songs.add(getSong(id));
    	return songs;
    }
    public static class GetFilteredSongsAsyncTask extends AsyncTask<MusicFilter, Void, Collection<Song>>{
    	private final FilteredSongCallback callback;
    	private final WeakReference<Collection<Song>> collectionRef;
    	private final MusicProvider musicProvider;

    	public GetFilteredSongsAsyncTask(MusicProvider musicProvider, FilteredSongCallback callback){
    		this.callback = callback;
    		this.collectionRef = null;
    		this.musicProvider = musicProvider;
	    }
	    public GetFilteredSongsAsyncTask(MusicProvider musicProvider, WeakReference<Collection<Song>> collectionRef){
    		this.callback = null;
    		this.collectionRef = collectionRef;
			this.musicProvider = musicProvider;
	    }

	    @Override
	    protected Collection<Song> doInBackground(MusicFilter... params) {
    		return musicProvider.getFilteredSongs(params[0]);
	    }
	    @Override
	    protected void onPostExecute(Collection<Song> collection){
	    	if (callback != null) callback.getFilteredSongs(collection);
	    	if (collectionRef != null){
	    		collectionRef.get().clear();
	    		collectionRef.get().addAll(collection);
		    }
	    }
    }

    private List<MediaBrowserCompat.MediaItem> getChildren(MusicFilter musicFilter) {
	    AsyncHelper.ThrowIfOnMainThread("getChildren()");
	    List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

	    if (musicFilter.filterType == MusicFilterType.Explore){
			if (MusicFilterType.ByAlbum.equals(musicFilter.filterValue)) {
			    List<Album> albums = new ArrayList<>(mAlbumListByLibraryId.values());
                Collections.sort(albums, new Comparator<Album>() {
                    @Override
                    public int compare(Album album1, Album album2) {
                        return album1.name.compareTo(album2.name);
                    }
                });

				for (Album album : albums)
					mediaItems.add(createBrowsableMediaItemForPossibleFilterValue(MusicFilterType.valueFor(musicFilter.filterValue), album.libraryId, album.name, album.artist));
			}

	    }else{
	    	// We are filtering the songs, not showing what filter values exist
			Collection<Song> songs = getFilteredSongs(musicFilter);

		    for (Song song : songs){
			    mediaItems.add(createMediaItem(song.getMetadata()));
		    }
	    }

        return mediaItems;
    }
    public static class GetChildrenAsyncTask extends AsyncTask<Void, Void, List<MediaBrowserCompat.MediaItem>>{
    	private final MusicFilter filter;
    	private final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result;
		private final MusicProvider musicProvider;

    	public GetChildrenAsyncTask(MusicProvider musicProvider, MusicFilter filter, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result){
    		this.filter = filter;
    		this.result = result;
    		this.musicProvider = musicProvider;
	    }

	    @Override
	    protected List<MediaBrowserCompat.MediaItem> doInBackground(Void... params) {
		    return musicProvider.getChildren(filter);
	    }
	    @Override
	    protected void onPostExecute(List<MediaBrowserCompat.MediaItem> children) {
		    result.sendResult(children);
	    }
    }

	private MediaBrowserCompat.MediaItem createBrowsableMediaItemForPossibleFilterValue(MusicFilterType filterType, String filterValue) {
		return createBrowsableMediaItemForPossibleFilterValue(filterType, filterValue, filterValue, "");
	}
    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForPossibleFilterValue(MusicFilterType filterType, String filterValue, String title, String subtitle){
	    MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
			    .setMediaId(new MusicFilter(filterType, filterValue).toString())
			    .setTitle(title)
			    .setSubtitle(subtitle)
			    .build();
	    return new MediaBrowserCompat.MediaItem(description,
			    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        return new MediaBrowserCompat.MediaItem(metadata.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

}
