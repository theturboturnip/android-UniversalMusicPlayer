package com.turboturnip.turnipmusic.frontend.base;

import android.graphics.Bitmap;

public class ListableItem {
    public final ItemListCommandFragment owner;
    public final CharSequence title, subtitle;
    public final Bitmap thumbnail;
    public final boolean browsable, playable;

    protected ListableItem(ItemListCommandFragment owner, CharSequence title, CharSequence subtitle, Bitmap thumbnail, boolean browsable, boolean playable){
        this.owner = owner;
        this.title = title;
        this.subtitle = subtitle;
        this.thumbnail = thumbnail;
        this.browsable = browsable;
        this.playable = playable;
    }

    protected void onPlay(){
        throw new RuntimeException("onPlay() not overriden");
    }
    protected void onBrowse(){
        throw new RuntimeException("onBrowse() not overriden");
    }
}
