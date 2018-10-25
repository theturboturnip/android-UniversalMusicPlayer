package com.turboturnip.turnipmusic.model;

public class Album {
    public String libraryId;
    public String name;
    public String artPath;
    public long trackCount;

    public Album(String libraryId, String name, String artPath, long trackCount){
        this.libraryId = libraryId;
        this.name = name;
        this.artPath = artPath;
        this.trackCount = trackCount;
    }
}
