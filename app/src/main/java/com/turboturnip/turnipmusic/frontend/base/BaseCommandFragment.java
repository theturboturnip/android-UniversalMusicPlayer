package com.turboturnip.turnipmusic.frontend.base;

import android.support.v4.media.MediaBrowserCompat;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turboui.fragment.CommandFragment;

// TODO: Rename to BaseCommandFragment?
public abstract class BaseCommandFragment extends CommandFragment  {
    private static final String TAG = LogHelper.makeLogTag(BaseCommandFragment.class);

    private MediaBrowserProvider browserProvider;
    protected MediaBrowserCompat mediaBrowser;

    @Override
    public void onStart() {
        super.onStart();
        browserProvider = (MediaBrowserProvider)mCommandListener;
        mediaBrowser = browserProvider.getMediaBrowser();

        LogHelper.d(TAG, "fragment.onStart onConnected=" + mediaBrowser.isConnected());

        if (mediaBrowser.isConnected() && !isDetached()) {
            connectToMediaBrowser();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            disconnectFromMediaBrowser();
        }
    }

    // Called when the MediaBrowser is connected. This method is either called by the
    // fragment.onStart() or explicitly by the activity in the case where the connection
    // completes after the onStart()
    public abstract void connectToMediaBrowser();
    // Called when the MediaBrowser is disconnected.
    public abstract void disconnectFromMediaBrowser();
}
