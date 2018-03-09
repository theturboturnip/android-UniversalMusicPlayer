package com.turboturnip.turnipmusic;

import java.util.HashMap;
import java.util.Map;

public enum MusicFilterType {
	Empty ("__EMPTY__"),
	Root ("__ROOT__"),
	Search ("__SEARCH__"),
	Explore ("__EXPLORE__"),
	ByAlbum ("__ALBUM__"),
	ByArtist ("__ARTIST__"),
	ByTag("__TAG__");

	private final String name;

	private static final Map<String, MusicFilterType> map = new HashMap<>();
	static {
		for (MusicFilterType en : values()) {
			map.put(en.name, en);
		}
	}

	public static MusicFilterType valueFor(String name) {
		return map.get(name);
	}

	MusicFilterType(String s) {
		name = s;
	}

	public boolean equals(String otherName) {
		// (otherName == null) check is not needed because name.equals(null) returns false
		return name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}
}