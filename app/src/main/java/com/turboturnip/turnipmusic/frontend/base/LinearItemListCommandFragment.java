package com.turboturnip.turnipmusic.frontend.base;

public abstract class LinearItemListCommandFragment extends ItemListCommandFragment<LinearItemListAdapter> {
    @Override
    public LinearItemListAdapter makeNewItemBrowseAdapter() {
        return new LinearItemListAdapter();
    }
}
