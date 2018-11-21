package com.turboturnip.turnipmusic.frontend.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.turboturnip.common.utils.LogHelper;
import com.turboturnip.turnipmusic.R;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemListCommandFragment<
        ItemBrowseAdapter extends ItemListCommandFragment.RecyclerAdapter> extends BaseCommandFragment {
    private static final String TAG = LogHelper.makeLogTag(ItemListCommandFragment.class);

    public static abstract class RecyclerAdapter<
            ViewHolder extends RecyclerView.ViewHolder,
            LayoutManager extends RecyclerView.LayoutManager
            > extends RecyclerView.Adapter<ViewHolder> {
        public List<ListableItem> items;

        protected static final int DATA_VIEW = 0; // Not browsable or playable
        protected static final int PLAYABLE_VIEW = 1; // Only playable
        protected static final int BROWSABLE_VIEW = 2; // Only browsable
        protected static final int BROWSABLE_PLAYABLE_VIEW = 3; // Both

        public RecyclerAdapter(){
            items = new ArrayList<>();
        }

        @Override
        public final int getItemCount() {
            return items.size();
        }
        @Override
        public final int getItemViewType(int position) {
            ListableItem item = items.get(position);
            int itemType = 0;
            if (item.playable) itemType += 1;
            if (item.browsable) itemType += 2;
            return itemType;
        }

        public abstract LayoutManager makeNewLayoutManager(Context context);
    }

    private RecyclerView mRecyclerView;
    protected ItemBrowseAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View mErrorView;
    private View mIndeterminateProgressView;
    private TextView mErrorMessage;
    private TextView mNoItemsText;

    public abstract ItemBrowseAdapter makeNewItemBrowseAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.d(TAG, "fragment.onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        mErrorView = rootView.findViewById(R.id.playback_error);
        mErrorMessage = mErrorView.findViewById(R.id.error_message);


        mAdapter = makeNewItemBrowseAdapter();

        mRecyclerView = rootView.findViewById(R.id.list_view);
        mRecyclerView.setAdapter(mAdapter);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = mAdapter.makeNewLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        mIndeterminateProgressView = rootView.findViewById(R.id.progress_bar);
        mNoItemsText = rootView.findViewById(R.id.no_data_text);
        updateLoadedState();

        return rootView;
    }

    protected void updateLoadedState(){
        mIndeterminateProgressView.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        mNoItemsText.setVisibility((mAdapter.getItemCount() > 0) ? View.GONE : View.VISIBLE);
    }
}
