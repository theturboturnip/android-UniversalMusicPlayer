package com.turboturnip.turnipmusic.model;

public class Album {
    public String libraryId;
    public String name;
    public String artist;
    public String artPath;
    public long trackCount;

    public Album(String libraryId, String name, String artist, String artPath, long trackCount){
        this.libraryId = libraryId;
        this.name = name;
        this.artist = artist;
        this.artPath = artPath;
        this.trackCount = trackCount;
    }
}
