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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.model.db.SongDatabase;
import com.turboturnip.turnipmusic.utils.AsyncHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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

    private static MusicProvider instance;

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
	    //mMusicListByAlbum = new ConcurrentHashMap<>();
	    //mMusicListByGenre = new ConcurrentHashMap<>();
        mSongListByLibraryId = new ConcurrentHashMap<>();
        mAlbumListByLibraryId = new ConcurrentHashMap<>();
        //mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        if (instance != null)
        	throw new RuntimeException("Tried to create a new MusicProvider when one already existed!");
        instance = this;
    }

    public static MusicProvider getInstance(Context context){
    	if (instance == null)
    		new MusicProvider(context);
    	return instance;
    }

	/**
	 * Get the list of music tracks from a server and caches the track information
	 * for future reference, keying tracks by songLibraryId and grouping by genre.
	 */
	public void retrieveMediaAsync(final Context context, final MusicCatalogCallback callback) {
		LogHelper.d(TAG, "retrieveMediaAsync called");


		// Asynchronously load the music catalog in a separate thread
		new AsyncRetrieveMediaTask(callback).execute(context);
	}
	private static class AsyncRetrieveMediaTask extends AsyncTask<Context, Void, Void>{
		private final MusicCatalogCallback callback;

		AsyncRetrieveMediaTask(MusicCatalogCallback callback){
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(Context... params) {
			if (MusicProvider.getInstance(params[0]).mCurrentState == State.INITIALIZED) {
				if (callback != null) {
					// Nothing to do, execute callback immediately
					callback.onMusicCatalogReady(true);
				}
				return null;
			}
			MusicProvider.getInstance(params[0]).retrieveMedia();
			if (callback != null) {
				callback.onMusicCatalogReady(MusicProvider.getInstance(params[0]).mCurrentState == State.INITIALIZED);
			}
			return null;
		}
	}

	synchronized void retrieveMedia() {
		try {
			if (mCurrentState == State.NON_INITIALIZED) {
				mCurrentState = State.INITIALIZING;

				mDatabase = SongDatabase.getInstance(context);

				Collection<Album> albums = mSource.albums(context);
				Iterator<Album> albumIterator = albums.iterator();
				while (albumIterator.hasNext()){
				    Album item = albumIterator.next();
				    mAlbumListByLibraryId.put(item.libraryId, item);
                }

				Collection<Song> songs = mSource.songs(context, mAlbumListByLibraryId, mDatabase);
				Iterator<Song> songIterator = songs.iterator();
				while (songIterator.hasNext()) {
					Song item = songIterator.next();
					mSongListByLibraryId.put(item.getLibraryId(), item);
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
    	private final Context context;

    	public GetFilteredSongsAsyncTask(Context c, FilteredSongCallback callback){
    		this.callback = callback;
    		this.collectionRef = null;
    		this.context = c;
	    }
	    public GetFilteredSongsAsyncTask(Context c, WeakReference<Collection<Song>> collectionRef){
    		this.callback = null;
    		this.collectionRef = collectionRef;
			this.context = c;
	    }

	    @Override
	    protected Collection<Song> doInBackground(MusicFilter... params) {
    		return getInstance(context).getFilteredSongs(params[0]);
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
				for (Album album : mAlbumListByLibraryId.values())
					mediaItems.add(createBrowsableMediaItemForPossibleFilterValue(MusicFilterType.valueFor(musicFilter.filterValue), album.libraryId, album.name));
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
    	private final Context context;

    	public GetChildrenAsyncTask(Context c, MusicFilter filter, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result){
    		this.filter = filter;
    		this.result = result;
    		this.context = c;
	    }

	    @Override
	    protected List<MediaBrowserCompat.MediaItem> doInBackground(Void... params) {
		    return MusicProvider.getInstance(context).getChildren(filter);
	    }
	    @Override
	    protected void onPostExecute(List<MediaBrowserCompat.MediaItem> children) {
		    result.sendResult(children);
	    }
    }

	private MediaBrowserCompat.MediaItem createBrowsableMediaItemForPossibleFilterValue(MusicFilterType filterType, String filterValue) {
		return createBrowsableMediaItemForPossibleFilterValue(filterType, filterValue, filterValue);
	}
    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForPossibleFilterValue(MusicFilterType filterType, String filterValue, String title){
	    MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
			    .setMediaId(new MusicFilter(filterType, filterValue).toString())
			    .setTitle(title)
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
