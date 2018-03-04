package com.turboturnip.turboshuffle;


public interface TurboShuffleSong<SongIDType> {
	SongIDType getId();
	int getLengthInSeconds();
}
