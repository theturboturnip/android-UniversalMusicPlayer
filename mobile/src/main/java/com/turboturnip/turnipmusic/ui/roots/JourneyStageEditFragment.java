package com.turboturnip.turnipmusic.ui.roots;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.CompositeMusicFilter;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.model.MusicFilterType;
import com.turboturnip.turnipmusic.ui.base.CommandFragment;
import com.turboturnip.turnipmusic.ui.base.EditorCommandFragment;
import com.turboturnip.turnipmusic.ui.ext.BaseItemTouchHelperCallback;
import com.turboturnip.turnipmusic.ui.ext.EnumSpinner;
import com.turboturnip.turnipmusic.ui.ext.ItemTouchHelperAdapter;

import org.json.JSONException;

import java.util.Collections;

public class JourneyStageEditFragment extends EditorCommandFragment {

	public static final String STAGE_TO_EDIT_KEY = "STAGE_TO_EDIT";
	public static final String STAGE_INDEX_KEY = "STAGE_INDEX";

	private Journey.Stage currentlyEditing;
	private int indexOfCurrentlyEditing;

	private EditText nameEditor, playCountEditor;
	private EnumSpinner<Journey.Stage.PlayType> playTypeSpinner;
	private PoolAdapter poolAdapter;
	private RecyclerView filterRecycler;

	@Override
	protected void findEditingObject() {
		Bundle args = getArguments();
		try {
			currentlyEditing = new Journey.Stage(args.getString(STAGE_TO_EDIT_KEY));
			indexOfCurrentlyEditing = args.getInt(STAGE_INDEX_KEY);
		} catch (JSONException e) {
			e.printStackTrace();
			mCommandListener.navigateBack();
		}
	}

	@Override
	protected void onApply() {
		Bundle data = new Bundle();
		data.putBoolean(CommandFragment.PASS_BACK_TAG, true);
		data.putString(JourneyEditFragment.NEW_STAGE_KEY, currentlyEditing.toString());
		data.putInt(JourneyEditFragment.NEW_STAGE_INDEX_KEY, indexOfCurrentlyEditing);
		mCommandListener.getDataFromFragment(data);
	}

	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle("Editing Stage");
	}

	@Override
	protected void createEditUI() {
		nameEditor = createTextField("Name");
		playTypeSpinner = createEnumSpinner("Play Mode", Journey.Stage.PlayType.values());
		playCountEditor = createNumberField("Play Count");
				poolAdapter = new PoolAdapter();
		ItemTouchHelper.Callback callback =
				new BaseItemTouchHelperCallback(poolAdapter, true, true);
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		filterRecycler = createLinearRecycler("Pools", poolAdapter);
		touchHelper.attachToRecyclerView(filterRecycler);
	}

	@Override
	protected boolean canApply() {
		if (currentlyEditing.name.length() == 0) return false;
		if (currentlyEditing.playType == null) return false;
		if (currentlyEditing.playCount < 0) return false;
		if (currentlyEditing.pools.size() == 0) return false;
		return true;
	}

	@Override
	protected void applyObjectValuesToUI() {
		nameEditor.setText(currentlyEditing.name);
		playTypeSpinner.setSelection(currentlyEditing.playType);
		playCountEditor.setText("" + currentlyEditing.playCount);
	}

	@Override
	protected void applyUIValuesToObject() {
		currentlyEditing.name = nameEditor.getText().toString();
		currentlyEditing.playType = playTypeSpinner.getSelectedValue();
		String playCount = playCountEditor.getText().toString();
		currentlyEditing.playCount = playCount.isEmpty() ? -1 : Integer.decode(playCount);
	}

	private class PoolListEntryViewHolder extends RecyclerView.ViewHolder{
		public View rootView;

		public View addButtonView;

		public View poolRootParent;
		public EditText nameView;
		public EditText weightView;

		public View filterParent;
		public EnumSpinner<MusicFilterType> filterTypeSpinner;
		public Spinner filterValueSpinner;

		public PoolListEntryViewHolder(View rootView){
			super(rootView);

			this.rootView = rootView;

			this.addButtonView = rootView.findViewById(R.id.add_button);

			this.poolRootParent = rootView.findViewById(R.id.pool_root_parent);
			this.nameView = this.poolRootParent.findViewById(R.id.name_field);
			this.weightView = this.poolRootParent.findViewById(R.id.weight_field);

			this.filterParent = rootView.findViewById(R.id.filter_parent);
			LinearLayout.LayoutParams expandingParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			LinearLayout filterTypeSpinnerParent = this.filterParent.findViewById(R.id.type_spinner_parent);
			this.filterTypeSpinner = new EnumSpinner<MusicFilterType>(getContext(), R.layout.editor_dropdown_text, R.id.dropdown_text,
					MusicFilterType.explorableTypes.toArray(new MusicFilterType[MusicFilterType.explorableTypes.size()]));
			this.filterTypeSpinner.setLayoutParams(expandingParams);
			filterTypeSpinnerParent.addView(this.filterTypeSpinner);

			LinearLayout filterValueSpinnerParent = this.filterParent.findViewById(R.id.value_spinner_parent);
			this.filterValueSpinner = new Spinner(getContext());
			this.filterValueSpinner.setLayoutParams(expandingParams);
			filterValueSpinnerParent.addView(this.filterValueSpinner);
		}
	}
	private class PoolAdapter extends RecyclerView.Adapter<JourneyStageEditFragment.PoolListEntryViewHolder> implements ItemTouchHelperAdapter {
		private int POOL_ROOT_TYPE = 0, FILTER_TYPE = 1;

		@Override
		public boolean onItemMove(int fromPosition, int toPosition) {
			int fromParentPool = getParentPoolIndex(fromPosition), toParentPool = getParentPoolIndex(toPosition);
			if (fromParentPool != toParentPool) return false;

			// TODO: Movement logic between pools
			if (fromParentPool == -1){
				// We're moving a pool, CHILDREN FILTERS MUST MOVE WITH US
			}else {
				// We're moving a filter
				int fromIndexInPool = getIndexInPool(fromPosition), toIndexInPool = getIndexInPool(toPosition);
				CompositeMusicFilter parentPool = currentlyEditing.pools.get(fromParentPool);
				if (fromIndexInPool < toIndexInPool) {
					for (int i = fromIndexInPool; i < toIndexInPool; i++) {
						Collections.swap(parentPool.filters, i, i + 1);
					}
				} else {
					for (int i = fromIndexInPool; i > toIndexInPool; i--) {
						Collections.swap(parentPool.filters, i, i - 1);
					}
				}
			}
			notifyItemMoved(fromPosition, toPosition);
			return true;
		}
		@Override
		public void onItemDismiss(int position) {
			int parentPool = getParentPoolIndex(position);
			if (parentPool != -1)
				currentlyEditing.pools.get(parentPool).filters.remove(getIndexInPool(position));
			else {
				currentlyEditing.pools.remove(getIndexAsPool(position));
			}
			notifyItemRemoved(position);
		}

		@Override
		public int getItemViewType(int position) {
			if (getParentPoolIndex(position) >= 0) return FILTER_TYPE;
			return POOL_ROOT_TYPE;
		}
		private int getIndexAsPool(int position){
			for (int i = 0; i < currentlyEditing.pools.size(); i++){
				position--;
				if (position < 0) return i;
				for (MusicFilter f : currentlyEditing.pools.get(i).filters){
					position--;
					if (position < 0) return -1;
				}
			}
			return -1;
		}
		private int getParentPoolIndex(int position){
			for (int i = 0; i < currentlyEditing.pools.size(); i++){
				position--;
				if (position < 0) return -1;
				for (MusicFilter f : currentlyEditing.pools.get(i).filters){
					position--;
					if (position < 0) return i;
				}
			}
			return -1;
		}
		private int getIndexInPool(int position){
			for (int i = 0; i < currentlyEditing.pools.size(); i++){
				position--;
				if (position < 0) return -1;
				for (int j = 0; j < currentlyEditing.pools.get(i).filters.size(); j++){
					position--;
					if (position < 0) return j;
				}
			}
			return -1;
		}

		// Create new views (invoked by the layout manager)
		@Override
		public JourneyStageEditFragment.PoolListEntryViewHolder onCreateViewHolder(ViewGroup parent,
		                                                              int viewType) {
			// create a new view
			View rootView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.pool_list_item, parent, false);
			JourneyStageEditFragment.PoolListEntryViewHolder vh = new JourneyStageEditFragment.PoolListEntryViewHolder(rootView);
			return vh;
		}

		// Replace the contents of a view (invoked by the layout manager)
		@Override
		public void onBindViewHolder(JourneyStageEditFragment.PoolListEntryViewHolder holder, int position) {
			int viewType = getItemViewType(position);
			if (viewType == POOL_ROOT_TYPE) {
				// TODO: Use listeners to apply values to the data
				int poolIndex = getIndexAsPool(position);
				holder.poolRootParent.setVisibility(View.VISIBLE);
				holder.nameView.setText("Pool #" + (poolIndex + 1));
				holder.weightView.setText("0.0f");
				holder.filterParent.setVisibility(View.INVISIBLE);
				// TODO: Set up the add button
			}else{
				// TODO: Indent
				// Use listeners to make sure the value spinner has the correct possibilities
				int poolIndex = getParentPoolIndex(position);
				int filterIndex = getIndexInPool(position);
				holder.poolRootParent.setVisibility(View.INVISIBLE);
				holder.filterParent.setVisibility(View.VISIBLE);
				holder.filterTypeSpinner.setSelection(currentlyEditing.pools.get(poolIndex).filters.get(filterIndex).filterType);
				// TODO: Set up the add button
			}
		}

		// Return the size of your dataset (invoked by the layout manager)
		@Override
		public int getItemCount() {
			int size = 0;
			for(CompositeMusicFilter cf : currentlyEditing.pools){
				size += 1 + cf.filters.size();
			}
			return size;
		}
	}
}
