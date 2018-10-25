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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.model.MusicProvider;

import org.json.JSONException;

import java.util.List;

/**
 * Manage the interactions among the container service, the queue manager and the actual playback.
 */
public class PlaybackManager implements Playback.Callback {

    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);
    // Action to thumbs up a media item
    //private static final String CUSTOM_ACTION_THUMBS_UP = "com.turboturnip.turnipmusic.THUMBS_UP";

    private static final long BACK_SKIP_MAX_TIME = 10 * 1000; // The maximum point in milliseconds a song can be within where pressing the "previous" button will skip it back to the start.

    private MusicProvider mMusicProvider;
    private Resources mResources;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;
    private QueueManager mQueueManager;
    private Context mContext;

    public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources,
                           MusicProvider musicProvider,
                           Playback playback, Context context) {
        mMusicProvider = musicProvider;
        mServiceCallback = serviceCallback;
        mResources = resources;
        mQueueManager = QueueManager.getInstance();
        mMediaSessionCallback = new MediaSessionCallback();
        mPlayback = playback;
        mPlayback.setCallback(this);
        mContext = context;
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    public void handlePlayRequest() {
        LogHelper.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentCompiledQueueItem();
        if (currentMusic != null) {
	        mQueueManager.updateMetadata();
	        mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic);
        }
    }

    public void handlePauseRequest() {
        LogHelper.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    public void handleStopRequest(String withError) {
        LogHelper.d(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        mQueueManager.stop();
        updatePlaybackState(withError);
    }


    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        //setCustomAction(stateBuilder);
        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentCompiledQueueItem();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    /*private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        // Set appropriate "Favorite" icon on Custom action:
	    Song currentSong = mQueueManager.getCurrentSong();
	    if (currentSong == null) return;
        int songID = currentSong.getId();
        int favoriteIcon = mMusicProvider.isFavorite(songID) ?
                R.drawable.ic_star_on : R.drawable.ic_star_off;
        LogHelper.d(TAG, "updatePlaybackState, setting Favorite custom action of music ",
		        songID, " current favorite=", mMusicProvider.isFavorite(songID));
        Bundle customActionExtras = new Bundle();
        WearHelper.setShowCustomActionOnWear(customActionExtras, true);
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_THUMBS_UP, mResources.getString(R.string.favorite), favoriteIcon)
                .setExtras(customActionExtras)
                .build());
    }*/

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.MusicCatalogCallback interface
     */
    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        if (mQueueManager.next()) {
        	LogHelper.e(TAG, "onCompletion");
            handlePlayRequest();
        } else {
            // If skipping was not possible, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        LogHelper.d(TAG, "setCurrentMediaId", mediaId);
        mQueueManager.updateHistoryFromMediaIDFromCompiledQueue(mediaId);
    }


    /**
     * Switch to a different Playback instance, maintaining all playback state, if possible.
     *
     * @param playback switch to this playback
     */
    public void switchToPlayback(Playback playback, boolean resumePlaying) {
        if (playback == null) {
            throw new IllegalArgumentException("Playback cannot be null");
        }
        // Suspends current state.
        int oldState = mPlayback.getState();
        long pos = mPlayback.getCurrentStreamPosition();
        String currentMediaId = mPlayback.getCurrentMediaId();
        mPlayback.stop(false);
        playback.setCallback(this);
        playback.setCurrentMediaId(currentMediaId);
        playback.seekTo(pos < 0 ? 0 : pos);
        playback.start();
        // Swaps instance.
        mPlayback = playback;
        switch (oldState) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayback.pause();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentCompiledQueueItem();
                if (resumePlaying && currentMusic != null) {
                    mPlayback.play(currentMusic);
                } else if (!resumePlaying) {
                    mPlayback.pause();
                } else {
                    mPlayback.stop(true);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            default:
                LogHelper.d(TAG, "Default called. Old state is ", oldState);
        }
    }


    /*private static class PlayNewJourneyAsyncTask extends AsyncTask<Journey, Void, Void>{
        private PlaybackManager playbackManager;

        PlayNewJourneyAsyncTask(PlaybackManager playbackManager){
            this.playbackManager = playbackManager;
        }

        @Override
        protected Void doInBackground(Journey... params){
            if (params.length == 0) throw new RuntimeException("Tried to set the Queue Manager to have no journey!");
            playbackManager.mQueueManager.startJourney(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void status){
	        playbackManager.mMediaSessionCallback.onSkipToNext();
        }
    }*/
    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            LogHelper.d(TAG, "play");
            if (mQueueManager.getCurrentSong() == null) {
                if (mQueueManager.next())
                	handlePlayRequest();
            }else
                handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            LogHelper.d(TAG, "OnSkipToQueueItem:" + queueId);
            mQueueManager.updateHistoryFromNewCompiledQueueIndex((int)queueId);
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSeekTo(long milliseconds) {
            LogHelper.d(TAG, "onSeekTo:", milliseconds);
            mPlayback.seekTo((int) milliseconds);
        }

        @Override
	    public void onPlayFromMediaId(String data, Bundle extras) {
            LogHelper.d(TAG, "playFromMediaId data:", data, "  extras=", extras);
            /*try {
            	Journey journey = new Journey(data);
            	new PlayNewJourneyAsyncTask(PlaybackManager.this).execute(journey);
                return;
            }catch (JSONException e){
            	LogHelper.i(TAG, "Data rejected as JSON because '" + e.getMessage() + "'");
            }

            MusicFilter filter = new MusicFilter(data);
            if (filter.isValid()){
            	new PlayNewJourneyAsyncTask(PlaybackManager.this).execute(new Journey("Generated Journey", filter));
            }else if (mQueueManager.addToExplicitQueue(mContext, mMusicProvider.getSong(data))) {
	            handlePlayRequest();
            }*/
        }

        @Override
        public void onPause() {
            LogHelper.d(TAG, "pause. current state=" + mPlayback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            LogHelper.d(TAG, "stop. current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            LogHelper.d(TAG, "skipToNext");
            if (mQueueManager.next()) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
        }

        @Override
        public void onSkipToPrevious() {
            if (mPlayback.getCurrentStreamPosition() > BACK_SKIP_MAX_TIME)
            	mPlayback.seekTo(0);
            else if (mQueueManager.previous()) {
                handlePlayRequest();
            } else {
                handleStopRequest(null);
            }
        }

        /*@Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                LogHelper.i(TAG, "onCustomAction: favorite for current track");
                Song currentSong = mQueueManager.getCurrentSong();
                if (currentSong != null) {
					int songID = currentSong.getId();
	                mMusicProvider.setFavorite(songID, !mMusicProvider.isFavorite(songID));
                }
                // playback state needs to be updated because the "Favorite" icon on the
                // custom action will change to reflect the new favorite state.
                updatePlaybackState(null);
            } else {
                LogHelper.e(TAG, "Unsupported action: ", action);
            }
        }*/

        /**
         * Handle free and contextual searches.
         * <p/>
         * All voice searches on Android Auto are sent to this method through a connected
         * {@link android.support.v4.media.session.MediaControllerCompat}.
         * <p/>
         * Threads and async handling:
         * Search, as a potentially slow operation, should run in another thread.
         * <p/>
         * Since this method runs on the main thread, most apps with non-trivial metadata
         * should defer the actual search to another thread (for example, by using
         * an {@link AsyncTask} as we do here).
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
        	// TODO: This should be an insert to the front of the explicit queue, and a next() to immediately play it.
            LogHelper.d(TAG, "playFromSearch  query=", query, " extras=", extras);

            mPlayback.setState(PlaybackStateCompat.STATE_CONNECTING);
            /*mMusicProvider.retrieveMediaAsync(mContext, new MusicProvider.MusicCatalogCallback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    if (!success) {
                        updatePlaybackState("Could not load catalog");
                    }

                    boolean successSearch = mQueueManager.setQueueFromSearch(query, extras);
                    if (successSearch) {
                        handlePlayRequest();
                        mQueueManager.updateMetadata();
                    } else {
                        updatePlaybackState("Could not find music");
                    }
                }
            });*/
        }
    }


    public interface PlaybackServiceCallback {
        void onPlaybackStart();
        void onNotificationRequired();
        void onPlaybackStop();
        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);
        void onMetadataRetrieveError();
        void onCurrentQueueIndexUpdated(int queueIndex);
        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newCompiledQueue);
    }
}
