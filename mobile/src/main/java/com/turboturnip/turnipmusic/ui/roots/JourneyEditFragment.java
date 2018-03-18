package com.turboturnip.turnipmusic.ui.roots;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.model.MusicFilter;
import com.turboturnip.turnipmusic.ui.base.EditorCommandFragment;
import com.turboturnip.turnipmusic.ui.ext.BaseItemTouchHelperCallback;
import com.turboturnip.turnipmusic.ui.ext.ItemTouchHelperAdapter;
import com.turboturnip.turnipmusic.utils.LogHelper;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Collections;

public class JourneyEditFragment extends EditorCommandFragment{

	private static final String TAG = LogHelper.makeLogTag(JourneyEditFragment.class);

	public static final String IS_NEW_JOURNEY_KEY = "NEW";
	public static final String JOURNEY_TO_EDIT_KEY = "TO_EDIT";
	public static final String NEW_STAGE_KEY = "NEWSTAGE";
	public static final String NEW_STAGE_INDEX_KEY = "NEWSTAGEINDEX";

	private Journey currentlyEditing;

	private EditText nameEditor;
	private RecyclerView stageList;

	private StageAdapter stageAdapter;

	@Override
	public void findEditingObject(){
		Bundle args = getArguments();
		if (args.getBoolean(IS_NEW_JOURNEY_KEY, false)) {
			currentlyEditing = new Journey(0, "", defaultStage(), defaultStage(), defaultStage());
		}else {
			try {
				currentlyEditing = new Journey(args.getString(JOURNEY_TO_EDIT_KEY));
			} catch (JSONException e) {
				e.printStackTrace();
				mCommandListener.navigateBack();
			}
		}
	}

	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle("Editing Journey");
	}

	private Journey.Stage defaultStage(){
		return new Journey.Stage("Stage #" + (currentlyEditing == null ? 1 : (currentlyEditing.stages.size() + 1)), Journey.Stage.PlayType.Repeat, 0, null, new MusicFilter());
	}

	@Override
	protected void createEditUI() {
		nameEditor = createTextField("Name");
		stageAdapter = new StageAdapter();
		ItemTouchHelper.Callback callback =
				new BaseItemTouchHelperCallback(stageAdapter);
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		stageList = createRecycler("Stages", stageAdapter);
		touchHelper.attachToRecyclerView(stageList);
	}

	@Override
	protected void applyObjectValuesToUI() {
		nameEditor.setText(currentlyEditing.name);
		stageAdapter.notifyDataSetChanged();
	}
	@Override
	protected void applyUIValuesToObject(){
		currentlyEditing.name = nameEditor.getText().toString();
	}
	@Override
	protected boolean canApply(){
		if (currentlyEditing.name.length() == 0) return false;
		return true;
	}

	@Override
	protected void onApply() {
		Bundle data = new Bundle();
		data.putInt(JourneyActivity.DATA_TYPE_KEY, JourneyActivity.SAVE_JOURNEY_REQUEST_TYPE);
		data.putString(JourneyActivity.NEW_JOURNEY_KEY, currentlyEditing.toString());
		mCommandListener.getDataFromFragment(data);
	}

	@Override
	public void getDataFromChildFragment(Bundle data) {
		super.getDataFromChildFragment(data);

		if (!data.containsKey(NEW_STAGE_KEY)) return;
		try{
			updateStage(new Journey.Stage(data.getString(NEW_STAGE_KEY)), data.getInt(NEW_STAGE_INDEX_KEY));
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	public void updateStage(Journey.Stage stage, int index){
		/*if (currentlyEditing.stages.size() < index) return;
		else if (currentlyEditing.stages.size() == index){
			currentlyEditing.stages = Arrays.copyOf(currentlyEditing.stages, index + 1);
		}*/
		currentlyEditing.stages.set(index, stage);
		LogHelper.e(TAG, stage);
		applyObjectValuesToUI();
	}

	private class EditStageOnClickListener implements View.OnClickListener{
		private final int indexToChange;

		EditStageOnClickListener(int indexToChange){
			this.indexToChange = indexToChange;
		}

		@Override
		public void onClick(View view) {
			Bundle data = new Bundle();
			data.putString(JourneyStageEditFragment.STAGE_TO_EDIT_KEY, currentlyEditing.stages.get(indexToChange).toString());
			data.putInt(JourneyStageEditFragment.STAGE_INDEX_KEY, indexToChange);
			mCommandListener.navigateToNewFragment(JourneyStageEditFragment.class, data);
		}
	}

	private static class StageCachedViews{
		TextView nameTextView, subtitleTextView;
		StageCachedViews(View baseView){
			nameTextView = baseView.findViewById(R.id.stage_name);
			subtitleTextView = baseView.findViewById(R.id.stage_desc);
		}
	}
	public static class StageViewHolder extends RecyclerView.ViewHolder{
		public View parentView;
		public StageCachedViews cachedViews;

		public StageViewHolder(View parentView){
			super(parentView);
			this.parentView = parentView;
			cachedViews = new StageCachedViews(parentView);
		}
	}
	private class StageAdapter extends RecyclerView.Adapter<StageViewHolder> implements ItemTouchHelperAdapter {
		@Override
		public void onItemMove(int fromPosition, int toPosition) {
			if (fromPosition < toPosition) {
				for (int i = fromPosition; i < toPosition; i++) {
					Collections.swap(currentlyEditing.stages, i, i + 1);
				}
			} else {
				for (int i = fromPosition; i > toPosition; i--) {
					Collections.swap(currentlyEditing.stages, i, i - 1);
				}
			}
			notifyItemMoved(fromPosition, toPosition);
		}
		@Override
		public void onItemDismiss(int position) {
			currentlyEditing.stages.remove(position);
			notifyItemRemoved(position);
		}


		// Create new views (invoked by the layout manager)
		@Override
		public StageViewHolder onCreateViewHolder(ViewGroup parent,
		                                               int viewType) {
			// create a new view
			View rootView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.stage_list_item, parent, false);
			StageViewHolder vh = new StageViewHolder(rootView);
			return vh;
		}

		// Replace the contents of a view (invoked by the layout manager)
		@Override
		public void onBindViewHolder(StageViewHolder holder, int position) {
			final Journey.Stage item = currentlyEditing.stages.get(position);

			// - get element from your dataset at this position
			// - replace the contents of the view with that element
			holder.cachedViews.nameTextView.setText(item.name);
			holder.cachedViews.subtitleTextView.setText(item.playType.toString() + " " + (item.playCount == 0 ? "forever" : (item.playCount + " songs")));
			holder.parentView.setOnClickListener(new EditStageOnClickListener(position));
		}

		// Return the size of your dataset (invoked by the layout manager)
		@Override
		public int getItemCount() {
			return currentlyEditing.stages.size();
		}


		/*@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			int type = getItemViewType(position);
			if (type == STAGE_TYPE) {
				final Journey.Stage item = getItem(position);

				StageCachedViews cachedViews;

				if (convertView == null) {
					convertView = LayoutInflater.from(getActivity())
							.inflate(R.layout.stage_list_item, parent, false);
					cachedViews = new StageCachedViews(convertView);
					convertView.setTag(cachedViews);
				} else {
					// If it isn't null, it has itemData already
					cachedViews = (StageCachedViews) convertView.getTag();
				}

				cachedViews.nameTextView.setText(item.name);
				cachedViews.subtitleTextView.setText(item.playType.toString() + " " + (item.playCount == 0 ? "forever" : (item.playCount + " songs")));
				convertView.setOnClickListener(new EditStageOnClickListener(position));
			}else if (type == ADD_STAGE_BUTTON_TYPE){
				if (convertView == null)
					convertView = LayoutInflater.from(getActivity()).inflate(R.layout.stage_add_item, parent, false);
				convertView.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						updateStage(defaultStage(), currentlyEditing.stages.size());
					}
				});
			}
			return convertView;
		}*/
	}
}
