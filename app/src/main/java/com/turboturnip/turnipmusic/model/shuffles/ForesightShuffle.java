package com.turboturnip.turnipmusic.model.shuffles;

import com.turboturnip.turnipmusic.model.Shuffle;
import com.turboturnip.turnipmusic.model.Song;

import java.util.List;

// This subclass of Shuffle allows Shuffles without randomness to tell the user
// exactly what's going to play moveToNext.
public interface ForesightShuffle extends Shuffle {
    List<Song> guaranteedSongs();
}
