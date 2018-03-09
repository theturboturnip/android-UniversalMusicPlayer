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
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.model.Song;
import com.turboturnip.turnipmusic.utils.LogHelper;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONException;
import org.json.JSONObject;

import static android.support.v4.media.session.MediaSessionCompat.QueueItem;

/**
 * An implementation of Playback that talks to Cast.
 */
public class CastPlayback implements Playback {

    private static final String TAG = LogHelper.makeLogTag(CastPlayback.class);

    private static final String MIME_TYPE_AUDIO_MPEG = "audio/mpeg";
    private static final String ITEM_ID = "itemId";

    private final MusicProvider mMusicProvider;
    private final Context mAppContext;
    private final RemoteMediaClient mRemoteMediaClient;
    private final RemoteMediaClient.Listener mRemoteMediaClientListener;

    private int mPlaybackState;

    /** Playback interface Callbacks */
    private Callback mCallback;
    private long mCurrentPosition;
    private int mCurrentMusicId;

    public CastPlayback(MusicProvider musicProvider, Context context) {
        mMusicProvider = musicProvider;
        mAppContext = context.getApplicationContext();

        CastSession castSession = CastContext.getSharedInstance(mAppContext).getSessionManager()
                .getCurrentCastSession();
        mRemoteMediaClient = castSession.getRemoteMediaClient();
        mRemoteMediaClientListener = new CastMediaClientListener();
    }

    @Override
    public void start() {
        mRemoteMediaClient.addListener(mRemoteMediaClientListener);
    }

    @Override
    public void stop(boolean notifyListeners) {
        mRemoteMediaClient.removeListener(mRemoteMediaClientListener);
        mPlaybackState = PlaybackStateCompat.STATE_STOPPED;
        if (notifyListeners && mCallback != null) {
            mCallback.onPlaybackStatusChanged(mPlaybackState);
        }
    }

    @Override
    public void setState(int state) {
        this.mPlaybackState = state;
    }

    @Override
    public long getCurrentStreamPosition() {
        if (!isConnected()) {
            return mCurrentPosition;
        }
        return (int) mRemoteMediaClient.getApproximateStreamPosition();
    }

    @Override
    public void updateLastKnownStreamPosition() {
        mCurrentPosition = getCurrentStreamPosition();
    }

    @Override
    public void play(QueueItem item) {
        try {
            loadMedia(Integer.decode(item.getDescription().getMediaId()), true);
            mPlaybackState = PlaybackStateCompat.STATE_BUFFERING;
            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mPlaybackState);
            }
        } catch (JSONException e) {
            LogHelper.e(TAG, "Exception loading media ", e, null);
            if (mCallback != null) {
                mCallback.onError(e.getMessage());
            }
        }
    }

    @Override
    public void pause() {
        try {
            if (mRemoteMediaClient.hasMediaSession()) {
                mRemoteMediaClient.pause();
                mCurrentPosition = (int) mRemoteMediaClient.getApproximateStreamPosition();
            } else {
                loadMedia(mCurrentMusicId, false);
            }
        } catch (JSONException e) {
            LogHelper.e(TAG, e, "Exception pausing cast playback");
            if (mCallback != null) {
                mCallback.onError(e.getMessage());
            }
        }
    }

    @Override
    public void seekTo(long position) {
        try {
            if (mRemoteMediaClient.hasMediaSession()) {
                mRemoteMediaClient.seek(position);
                mCurrentPosition = position;
            } else {
                mCurrentPosition = position;
                loadMedia(mCurrentMusicId, false);
            }
        } catch (JSONException e) {
            LogHelper.e(TAG, e, "Exception pausing cast playback");
            if (mCallback != null) {
                mCallback.onError(e.getMessage());
            }
        }
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        this.mCurrentMusicId = Integer.decode(mediaId);
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMusicId+"";
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public boolean isConnected() {
        CastSession castSession = CastContext.getSharedInstance(mAppContext).getSessionManager()
                .getCurrentCastSession();
        return (castSession != null && castSession.isConnected());
    }

    @Override
    public boolean isPlaying() {
        return isConnected() && mRemoteMediaClient.isPlaying();
    }

    @Override
    public int getState() {
        return mPlaybackState;
    }

    // TODO: This assumes the mediaId is the musicID. Is that right?
    private void loadMedia(int musicId, boolean autoPlay) throws JSONException {
        Song song = mMusicProvider.getSong(musicId);
        if (song == null) {
            throw new IllegalArgumentException("Invalid mediaId " + musicId);
        }
        if (musicId == mCurrentMusicId) {
            mCurrentMusicId = musicId;
            mCurrentPosition = 0;
        }
        JSONObject customData = new JSONObject();
        customData.put(ITEM_ID, musicId);
        MediaInfo media = toCastMediaMetadata(song, customData);
        mRemoteMediaClient.load(media, autoPlay, mCurrentPosition, customData);
    }

    /**
     * Helper method to convert a {@link android.media.MediaMetadata} to a
     * {@link com.google.android.gms.cast.MediaInfo} used for sending media to the receiver app.
     *
     * @param song {@link com.turboturnip.turnipmusic.model.Song}
     * @param customData custom data specifies the local mediaId used by the player.
     * @return mediaInfo {@link com.google.android.gms.cast.MediaInfo}
     */
    private static MediaInfo toCastMediaMetadata(Song song,
                                                 JSONObject customData) {
        MediaMetadataCompat originalMetadata = song.getMetadata();
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE,
                originalMetadata.getDescription().getTitle() == null ? "" :
                        originalMetadata.getDescription().getTitle().toString());
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE,
                originalMetadata.getDescription().getSubtitle() == null ? "" :
                        originalMetadata.getDescription().getSubtitle().toString());
        mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST,
                originalMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST));
        mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE,
                originalMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        WebImage image = new WebImage(
                new Uri.Builder().encodedPath(
                        originalMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                        .build());
        // First image is used by the receiver for showing the audio album art.
        mediaMetadata.addImage(image);
        // Second image is used by Cast Companion Library on the full screen activity that is shown
        // when the cast dialog is clicked.
        mediaMetadata.addImage(image);

        //noinspection ResourceType
        // TODO: Does sending the file path make things not work?
        return new MediaInfo.Builder(song.getFilePath())
                .setContentType(MIME_TYPE_AUDIO_MPEG)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .setCustomData(customData)
                .build();
    }

    private void setMetadataFromRemote() {
        // Sync: We get the customData from the remote media information and update the local
        // metadata if it happens to be different from the one we are currently using.
        // This can happen when the app was either restarted/disconnected + connected, or if the
        // app joins an existing session while the Chromecast was playing a queue.
        try {
            MediaInfo mediaInfo = mRemoteMediaClient.getMediaInfo();
            if (mediaInfo == null) {
                return;
            }
            JSONObject customData = mediaInfo.getCustomData();

            if (customData != null && customData.has(ITEM_ID)) {
                int remoteMediaId = Integer.decode(customData.getString(ITEM_ID));
                if (mCurrentMusicId != remoteMediaId) {
                    mCurrentMusicId = remoteMediaId;
                    if (mCallback != null) {
                        mCallback.setCurrentMediaId(""+remoteMediaId);
                    }
                    updateLastKnownStreamPosition();
                }
            }
        } catch (JSONException e) {
            LogHelper.e(TAG, e, "Exception processing update metadata");
        }

    }

    private void updatePlaybackState() {
        int status = mRemoteMediaClient.getPlayerState();
        int idleReason = mRemoteMediaClient.getIdleReason();

        LogHelper.d(TAG, "onRemoteMediaPlayerStatusUpdated ", status);

        // Convert the remote playback states to media playback states.
        switch (status) {
            case MediaStatus.PLAYER_STATE_IDLE:
                if (idleReason == MediaStatus.IDLE_REASON_FINISHED) {
                    if (mCallback != null) {
                        mCallback.onCompletion();
                    }
                }
                break;
            case MediaStatus.PLAYER_STATE_BUFFERING:
                mPlaybackState = PlaybackStateCompat.STATE_BUFFERING;
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mPlaybackState);
                }
                break;
            case MediaStatus.PLAYER_STATE_PLAYING:
                mPlaybackState = PlaybackStateCompat.STATE_PLAYING;
                setMetadataFromRemote();
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mPlaybackState);
                }
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                mPlaybackState = PlaybackStateCompat.STATE_PAUSED;
                setMetadataFromRemote();
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mPlaybackState);
                }
                break;
            default: // case unknown
                LogHelper.d(TAG, "State default : ", status);
                break;
        }
    }

    private class CastMediaClientListener implements RemoteMediaClient.Listener {

        @Override
        public void onMetadataUpdated() {
            LogHelper.d(TAG, "RemoteMediaClient.onMetadataUpdated");
            setMetadataFromRemote();
        }

        @Override
        public void onStatusUpdated() {
            LogHelper.d(TAG, "RemoteMediaClient.onStatusUpdated");
            updatePlaybackState();
        }

        @Override
        public void onSendingRemoteMediaRequest() {
        }

        @Override
        public void onAdBreakStatusUpdated() {
        }

        @Override
        public void onQueueStatusUpdated() {
        }

        @Override
        public void onPreloadStatusUpdated() {
        }
    }
}
