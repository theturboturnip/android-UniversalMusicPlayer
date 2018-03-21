package com.turboturnip.turboui.ext;

public interface ItemTouchHelperAdapter {
	boolean onItemMove(int fromPosition, int toPosition);
	void onItemDismiss(int position);
}
