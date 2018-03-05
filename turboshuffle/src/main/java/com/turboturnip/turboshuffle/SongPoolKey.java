package com.turboturnip.turboshuffle;

import java.util.Objects;

public class SongPoolKey {
	public int poolIndex;
	public int songIndexInPool;

	public SongPoolKey(int poolIndex, int songIndexInPool){
		this.poolIndex = poolIndex;
		this.songIndexInPool = songIndexInPool;
	}

	@Override
	public boolean equals(Object obj){
		if (obj == null) {
			return false;
		}
		if (!SongPoolKey.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final SongPoolKey other = (SongPoolKey) obj;
		if (this.poolIndex != other.poolIndex) {
			return false;
		}
		if (this.songIndexInPool != other.songIndexInPool) {
			return false;
		}
		return true;
	}
	@Override
	public int hashCode(){
		return Objects.hash(poolIndex, songIndexInPool);
	}
}