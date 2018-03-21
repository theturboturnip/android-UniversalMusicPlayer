package com.turboturnip.turboui.ext;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class BaseItemTouchHelperCallback extends ItemTouchHelper.Callback {
	private final ItemTouchHelperAdapter mAdapter;
	private boolean allowRearrange, allowRemove;

	public BaseItemTouchHelperCallback(ItemTouchHelperAdapter adapter, boolean allowRearrange, boolean allowRemove) {
		this.mAdapter = adapter;
		this.allowRearrange = allowRearrange;
		this.allowRemove = allowRemove;
	}

	@Override
	public boolean isLongPressDragEnabled() {
		return true;
	}

	@Override
	public boolean isItemViewSwipeEnabled() {
		return true;
	}

	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		int dragFlags = allowRearrange ? (ItemTouchHelper.UP | ItemTouchHelper.DOWN) : 0;
		int swipeFlags = allowRemove ? (ItemTouchHelper.START | ItemTouchHelper.END) : 0;
		return makeMovementFlags(dragFlags, swipeFlags);
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
	                      RecyclerView.ViewHolder target) {
		return mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
	}
}