package com.turboturnip.turnipmusic.ui.ext;

public interface ItemTouchHelperAdapter {
	boolean onItemMove(int fromPosition, int toPosition);
	void onItemDismiss(int position);
}
