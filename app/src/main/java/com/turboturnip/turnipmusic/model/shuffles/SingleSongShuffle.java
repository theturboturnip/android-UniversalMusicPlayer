package com.turboturnip.turnipmusic.model.shuffles;

import com.turboturnip.turnipmusic.model.Shuffle;
import com.turboturnip.turnipmusic.model.Song;

public class SingleSongShuffle implements Shuffle {
    private final Song song;
    private final boolean playOnce;
    private boolean finished = false;

    public SingleSongShuffle(Song song, boolean playOnce){
        this.song = song;
        this.playOnce = playOnce;
    }

    @Override
    public int getTotalLength() {
        return playOnce ? 1 : 0;
    }
    @Override
    public int getLengthRemaining() {
        if (finished) return 0;
        if (playOnce) return 1;
        return -1;
    }

    @Override
    public void advance() {
        if (playOnce) finished = true;
    }
    @Override
    public Song nextSong() {
        return finished ? null : song;
    }

}
