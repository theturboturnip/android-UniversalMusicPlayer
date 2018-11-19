package com.turboturnip.turnipmusic.model;

/**
 * A Shuffle is the base unit of play in the app. Contrary to the name, it is not necessarily a shuffled set of songs,
 * it could be a repeated set of songs, one song played once, etc.
 */
public interface Shuffle {
    int getTotalLength(); // Length of -1 (or 0) implies the Shuffle can continue forever.
    int getLengthRemaining(); // Returns the amount of songs left, if the Shuffle is bounded. Returns -1 otherwise

    void advance(); // Move to the moveToNext Song.
    Song nextSong(); // Return the Song to play moveToNext.
}
