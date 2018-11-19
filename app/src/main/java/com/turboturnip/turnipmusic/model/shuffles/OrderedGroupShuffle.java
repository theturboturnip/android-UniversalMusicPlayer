package com.turboturnip.turnipmusic.model.shuffles;

import com.turboturnip.turnipmusic.model.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderedGroupShuffle implements ForesightShuffle {
    private final Song[] songs;
    private int songIndex;
    private final boolean playOnce;

    public OrderedGroupShuffle(Song[] songs, boolean playOnce){
        this.songs = songs;
        this.songIndex = 0;
        this.playOnce = playOnce;
    }

    @Override
    public int getTotalLength() {
        return playOnce ? songs.length : 0;
    }
    @Override
    public int getLengthRemaining() {
        if (songIndex == songs.length) return 0;
        if (playOnce) return songs.length - songIndex;
        return -1;
    }

    @Override
    public void advance() {
        songIndex++;
        if (!playOnce && songIndex == songs.length) songIndex = 0;
    }
    @Override
    public Song nextSong() {
        return (songs.length == songIndex) ? null : songs[songIndex];
    }

    @Override
    public List<Song> guaranteedSongs() {
        return (songs.length == songIndex) ? new ArrayList<Song>() : Arrays.asList(songs).subList(songIndex, songs.length);
    }
}
