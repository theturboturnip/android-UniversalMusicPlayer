package com.turboturnip.turnipmusic.frontend.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.turboturnip.turnipmusic.R;

public class LinearItemListAdapter extends ItemListCommandFragment.RecyclerAdapter<LinearItemListAdapter.ViewHolder, LinearLayoutManager> {
    @Override
    public LinearLayoutManager makeNewLayoutManager(Context context) {
        return new LinearLayoutManager(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView titleView;
        final TextView subtitleView;

        final View playButton;
        final View browseButton;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            if (viewType == DATA_VIEW){
                titleView = itemView.findViewById(R.id.header_text);
                subtitleView = null;
                playButton = browseButton = null;
            }else{
                titleView = itemView.findViewById(R.id.title);
                subtitleView = itemView.findViewById(R.id.subtitle);
                playButton = itemView.findViewById(R.id.action_button);
                browseButton = itemView.findViewById(R.id.into_button);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == DATA_VIEW){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.media_list_header, viewGroup, false);
        }else{
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.generic_list_item, viewGroup, false);
        }
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final ListableItem item = items.get(position);

        viewHolder.titleView.setText(item.title);
        if (viewHolder.subtitleView != null) viewHolder.subtitleView.setText(item.subtitle);
        if (viewHolder.playButton != null && item.playable) viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.onPlay();
            }
        });
        if (viewHolder.browseButton != null && item.browsable) viewHolder.browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.onBrowse();
            }
        });
    }
}
