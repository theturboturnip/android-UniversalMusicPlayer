package com.turboturnip.turnipmusic.frontend.roots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turboturnip.turnipmusic.frontend.base.ItemListCommandFragment;
import com.turboturnip.turnipmusic.frontend.base.LinearItemListAdapter;
import com.turboturnip.turnipmusic.frontend.base.ListableHeader;
import com.turboturnip.turnipmusic.frontend.base.ListableSong;

public class TestItemLinearListFragment extends ItemListCommandFragment<LinearItemListAdapter> {
    @Override
    public LinearItemListAdapter makeNewItemBrowseAdapter() {
        return new LinearItemListAdapter();
    }

    @Override
    public void connectToMediaBrowser() {}

    @Override
    public void disconnectFromMediaBrowser() {}

    @Override
    protected void updateTitle() {}

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.items.add(new ListableHeader(this, "First Set"));
        mAdapter.items.add(new ListableSong(this, "Song #1"));
        mAdapter.items.add(new ListableSong(this, "Song #2"));
        mAdapter.items.add(new ListableSong(this, "Song #3"));
        mAdapter.items.add(new ListableHeader(this, "Second Set"));
        mAdapter.items.add(new ListableSong(this, "Song #4"));
        mAdapter.items.add(new ListableSong(this, "Song #5"));

        mAdapter.notifyDataSetChanged();
    }
}
