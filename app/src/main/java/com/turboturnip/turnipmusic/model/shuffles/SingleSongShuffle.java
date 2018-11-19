package com.turboturnip.turnipmusic.model.shuffles;

import com.turboturnip.turnipmusic.model.Song;

public class SingleSongShuffle extends OrderedGroupShuffle {
    public SingleSongShuffle(Song song, boolean playOnce){
        super(new Song[]{song}, playOnce);
    }
}
