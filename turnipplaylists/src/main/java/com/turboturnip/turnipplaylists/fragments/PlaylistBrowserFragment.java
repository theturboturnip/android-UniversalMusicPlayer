package com.turboturnip.turnipplaylists.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.turboturnip.turboui.ext.BaseRecyclerViewItemTouchHelperCallback;
import com.turboturnip.turboui.ext.RecyclerViewItemTouchHelperAdapter;
import com.turboturnip.turboui.fragment.CommandFragment;
import com.turboturnip.turnipplaylists.R;

import java.util.ArrayList;
import java.util.List;

public class PlaylistBrowserFragment extends CommandFragment {

	static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

	private List<String> playlists;
	private PlaylistAdapter playlistAdapter;

	protected void updateTitle() {
		mCommandListener.setToolbarTitle("Playlists");
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_recyclerlist, container, false);

		playlistAdapter = new PlaylistAdapter();
		ItemTouchHelper.Callback callback =
				new BaseRecyclerViewItemTouchHelperCallback(playlistAdapter, false, true);
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		RecyclerView result = rootView.findViewById(R.id.recycler_view);
		LinearLayoutManager manager = new LinearLayoutManager(getContext());
		result.setLayoutManager(manager);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
				manager.getOrientation());
		result.addItemDecoration(dividerItemDecoration);
		result.setAdapter(playlistAdapter);
		touchHelper.attachToRecyclerView(result);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		String playlist_id = "PLpc_f2Kxcy9UmEPPAJc43jIDe5jaFyGVL";
		playlists = new ArrayList<>();
		playlists.add(playlist_id);
		playlistAdapter.notifyDataSetChanged();
	}

	private static class PlaylistViewHolder extends RecyclerView.ViewHolder{
		View rootView;

		View actionButton;
		ImageView actionDrawable;
		TextView actionText;

		View selectButton;
		ImageView selectDrawable;

		TextView title;
		TextView subtitle;

		PlaylistViewHolder(View rootView){
			super(rootView);
			this.rootView = rootView;

			this.actionButton = this.rootView.findViewById(R.id.action_button);
			this.actionDrawable = this.actionButton.findViewById(R.id.action_drawable);
			this.actionText = this.actionButton.findViewById(R.id.action_text);

			this.selectButton = this.rootView.findViewById(R.id.into_button);
			this.selectDrawable = this.selectButton.findViewById(R.id.into_drawable);

			this.title = this.rootView.findViewById(R.id.title);
			this.subtitle = this.rootView.findViewById(R.id.subtitle);
		}
	}
	private class PlaylistAdapter extends RecyclerView.Adapter<PlaylistViewHolder> implements RecyclerViewItemTouchHelperAdapter {
		@Override
		public boolean canMoveItem(int position) {
			return false;
		}
		@Override
		public boolean onItemMove(int fromPosition, int toPosition) {
			return false;
		 }

		@Override
		public boolean canDismissItem(int position) {
			if (position >= playlists.size()) return false;
			return true;
		}
		@Override
		public void onItemDismiss(int position) {
			playlists.remove(position);
			notifyItemRemoved(position);
		}

		// Create new views (invoked by the layout manager)
		@Override
		public PlaylistViewHolder onCreateViewHolder(ViewGroup parent,
		                                          int viewType) {
			// create a new view
			View rootView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.generic_list_item, parent, false);
			PlaylistViewHolder vh = new PlaylistViewHolder(rootView);
			return vh;
		}

		// Replace the contents of a view (invoked by the layout manager)
		@Override
		public void onBindViewHolder(PlaylistViewHolder holder, int position) {
			if (position == playlists.size()){
				holder.actionButton.setVisibility(View.VISIBLE);
				holder.actionButton.setOnClickListener(null);
				holder.actionDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
						R.drawable.ic_add_black));
				holder.actionDrawable.setVisibility(View.VISIBLE);
				holder.actionText.setVisibility(View.GONE);

				holder.selectButton.setVisibility(View.GONE);
				holder.selectButton.setOnClickListener(null);
				holder.title.setText("Add Playlist");
				holder.subtitle.setText("");
			}else {
				final String item = playlists.get(position);

				holder.actionButton.setVisibility(View.GONE);
				holder.actionButton.setOnClickListener(null);

				holder.selectButton.setVisibility(View.VISIBLE);
				holder.selectButton.setOnClickListener(null);
				holder.title.setText("Playlist");
				holder.subtitle.setText(item);
			}
		}

		// Return the size of your dataset (invoked by the layout manager)
		@Override
		public int getItemCount() {
			return playlists.size() + 1;
		}
	}
}
