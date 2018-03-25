package com.turboturnip.turboui.ext;

public interface RecyclerViewItemTouchHelperAdapter {
	boolean canMoveItem(int position);
	boolean canDismissItem(int position);

	boolean onItemMove(int fromPosition, int toPosition);
	void onItemDismiss(int position);
}
