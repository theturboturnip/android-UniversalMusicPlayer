package com.turboturnip.turnipmusic.frontend.roots.library;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.widget.Toast;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.frontend.base.LinearItemListCommandFragment;
import com.turboturnip.turnipmusic.frontend.base.ListableAlbum;
import com.turboturnip.turnipmusic.frontend.base.MediaBrowserProvider;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;

import java.util.List;

public class AlbumListFragment extends LinearItemListCommandFragment {
    private static final String TAG = LogHelper.makeLogTag(AlbumListFragment.class);

    private class MediaBrowserSubscriber {
        MediaBrowserProvider mBrowserProvider;
        String mSubscribeTo;
        MediaBrowserCompat.SubscriptionCallback mCallback;

        MediaBrowserSubscriber(MediaBrowserProvider provider, String subscribeTo, MediaBrowserCompat.SubscriptionCallback callback){
            this.mBrowserProvider = provider;
            this.mSubscribeTo = subscribeTo;
            this.mCallback = callback;
        }

        void subscribe(){
            mBrowserProvider.getMediaBrowser().subscribe(mSubscribeTo, mCallback);
        }
        void unsubscribe(){
            mBrowserProvider.getMediaBrowser().unsubscribe(mSubscribeTo);
        }
    }
    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                                "  count=" + children.size());
                        mAdapter.items.clear();
                        for (MediaBrowserCompat.MediaItem item : children) {
                            mAdapter.items.add(new ListableAlbum(AlbumListFragment.this, item));
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (Throwable t) {
                        LogHelper.e(TAG, "Error on childrenloaded", t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    LogHelper.e(TAG, "browse fragment subscription onError, id=" + id);
                    Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
                    //checkForUserVisibleErrors(true);
                }
            };
    private MediaBrowserSubscriber mSubscriber;

    @Override
    public void connectToMediaBrowser(){
        if (mSubscriber == null)
            mSubscriber = new MediaBrowserSubscriber(
                    (MediaBrowserProvider)mCommandListener,
                    new MusicFilter(MusicFilterType.Explore, MusicFilterType.ByAlbum.toString()).toString(),
                    mSubscriptionCallback);

        mSubscriber.subscribe();
    }

    @Override
    public void disconnectFromMediaBrowser() {
        if (mSubscriber != null) mSubscriber.unsubscribe();
    }

    @Override
    protected void updateTitle() {

    }
}
