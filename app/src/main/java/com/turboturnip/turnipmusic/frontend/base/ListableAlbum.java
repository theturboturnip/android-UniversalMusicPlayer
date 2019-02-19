package com.turboturnip.turnipmusic.frontend.base;

import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;

public class ListableAlbum extends ListableItem {
    public ListableAlbum(ItemListCommandFragment owner, MediaBrowserCompat.MediaItem srcItem ){ //CharSequence title, CharSequence subtitle, Bitmap thumbnail) {
        super(owner, srcItem.getDescription().getTitle(), srcItem.getDescription().getSubtitle(), srcItem.getDescription().getIconBitmap(), true, false);
    }
}
