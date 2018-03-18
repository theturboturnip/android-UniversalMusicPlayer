package com.turboturnip.turnipmusic.ui.roots;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.turboturnip.turnipmusic.utils.LogHelper;

import org.json.JSONException;

import java.util.Arrays;

public class JourneyEditFragment extends EditorCommandFragment {

	private static final String TAG = LogHelper.makeLogTag(JourneyEditFragment.class);

	public static final String IS_NEW_JOURNEY_KEY = "NEW";
	public static final String JOURNEY_TO_EDIT_KEY = "TO_EDIT";
	public static final String NEW_STAGE_KEY = "NEWSTAGE";
	public static final String NEW_STAGE_INDEX_KEY = "NEWSTAGEINDEX";

	private Journey currentlyEditing;

	private EditText nameEditor;
	private ListView stageList;

	private StageAdapter stageAdapter;

	@Override
	public void findEditingObject(){
		Bundle args = getArguments();
		if (args.getBoolean(IS_NEW_JOURNEY_KEY, false)) {
			currentlyEditing = new Journey(0, "", defaultStage());
		}else {
			try {
				currentlyEditing = new Journey(args.getString(JOURNEY_TO_EDIT_KEY));
			} catch (JSONException e) {
				e.printStackTrace();
				mCommandListener.navigateBack();
			}
		}
	}

	/*@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_journey_editor, container, false);

		nameEditor = rootView.findViewById(R.id.name_field);
		nameEditor.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void afterTextChanged(Editable editable) {
				updateJourneyFromUI();
			}
		});
		stageList = rootView.findViewById(R.id.stage_list);
		stageAdapter = new StageAdapter();
		stageList.setAdapter(stageAdapter);
		cancelButton = rootView.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCommandListener.navigateBack();
			}
		});
		applyButton = rootView.findViewById(R.id.apply_button);
		applyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
		updateUIFromJourney();

		currentInstance = new WeakReference<>(this);

		return rootView;
	}*/
	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle("Editing Journey");
	}

	private Journey.Stage defaultStage(){
		return new Journey.Stage("Stage #" + (currentlyEditing == null ? 1 : (currentlyEditing.stages.length + 1)), Journey.Stage.PlayType.Repeat, 0, null, new MusicFilter());
	}

	@Override
	protected void createEditUI() {
		nameEditor = createTextField("Name");
		stageAdapter = new StageAdapter();
		stageList = createList("Stages", stageAdapter);
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
		if (currentlyEditing.stages.length < index) return;
		else if (currentlyEditing.stages.length == index){
			currentlyEditing.stages = Arrays.copyOf(currentlyEditing.stages, index + 1);
		}
		currentlyEditing.stages[index] = stage;
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
			data.putString(JourneyStageEditFragment.STAGE_TO_EDIT_KEY, currentlyEditing.stages[indexToChange].toString());
			data.putInt(JourneyStageEditFragment.STAGE_INDEX_KEY, indexToChange);
			mCommandListener.navigateToNewFragment(JourneyStageEditFragment.class, data);
		}
	}

	private class StageCachedViews{
		TextView nameTextView, subtitleTextView;
		StageCachedViews(View baseView){
			nameTextView = baseView.findViewById(R.id.stage_name);
			subtitleTextView = baseView.findViewById(R.id.stage_desc);
		}
	}
	private class StageAdapter extends BaseAdapter {
		private static final int STAGE_TYPE = 0, ADD_STAGE_BUTTON_TYPE = 1;

		@Override
		public int getItemViewType(int position) {
			return position < currentlyEditing.stages.length ? STAGE_TYPE : ADD_STAGE_BUTTON_TYPE;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getCount() {
			return currentlyEditing.stages.length + 1;
		}

		@Override
		public Journey.Stage getItem(int position) {
			return position < currentlyEditing.stages.length ? currentlyEditing.stages[position] : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position){
			return false;
		}

		@NonNull
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
						updateStage(defaultStage(), currentlyEditing.stages.length);
					}
				});
			}
			return convertView;
		}
	}
}
