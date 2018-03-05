package com.turboturnip.turnipmusic.playback;

import com.turboturnip.turboshuffle.SongPool;
import com.turboturnip.turnipmusic.model.Song;

public interface ImplicitQueue {
	void initialize(SongPool[] pools);
	void onSongPlayed(Song song);
	Song nextSong();
}
