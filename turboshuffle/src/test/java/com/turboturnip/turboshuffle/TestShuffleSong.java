package com.turboturnip.turboshuffle;

public class TestShuffleSong implements TurboShuffleSong<Integer> {

	private final int id;
	private final int lengthInSeconds;

	public TestShuffleSong(int id, int lengthInSeconds){
		this.id = id;
		this.lengthInSeconds = lengthInSeconds;
	}

	@Override
	public Integer getId(){
		return id;
	}
	@Override
	public int getLengthInSeconds(){
		return lengthInSeconds;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!TestShuffleSong.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final TestShuffleSong other = (TestShuffleSong) obj;
		if (this.id != other.getId()) {
			return false;
		}
		return true;
	}
	@Override
	public int hashCode(){
		return id;
	}

}
