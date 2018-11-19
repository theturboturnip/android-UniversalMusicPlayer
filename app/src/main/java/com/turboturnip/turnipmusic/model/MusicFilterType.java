package com.turboturnip.turnipmusic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MusicFilterType {
	Empty("Empty"),
	Root("Root"),
	Search("Search"),
	Explore("Explore"),
	ByAlbum("Album"),
	ByArtist("Artist"),
	ByTag("Tag"),
	Song("Song");

	private final String name;

	private static final Map<String, MusicFilterType> map = new HashMap<>();
	static {
		for (MusicFilterType en : values()) {
			map.put(en.name, en);
		}
	}
	public static final List<MusicFilterType> explorableTypes = new ArrayList<>();
	static{
		explorableTypes.add(ByAlbum);
		explorableTypes.add(ByArtist);
		explorableTypes.add(ByTag);
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