package com.turboturnip.turnipmusic.frontend.base;

public class ListableSong extends ListableItem {
    public ListableSong(ItemListCommandFragment owner, String name){
        super(owner, name, null, null, false, true);
    }
}
