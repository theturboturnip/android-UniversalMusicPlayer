package com.turboturnip.turnipmusic.ui.roots;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.ui.base.CommandFragment;
import com.turboturnip.turnipmusic.utils.LogHelper;

import org.json.JSONException;

public class JourneyStageEditFragment extends CommandFragment {

	public static final String STAGE_TO_EDIT_KEY = "STAGE_TO_EDIT";
	public static final String STAGE_INDEX_KEY = "STAGE_INDEX";

	private Journey.Stage currentlyEditing;
	private int indexOfCurrentlyEditing;

	private EditText nameEditor;
	private Button cancelButton, applyButton;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

		Bundle args = getArguments();
		try {
			currentlyEditing = new Journey.Stage(args.getString(STAGE_TO_EDIT_KEY));
			indexOfCurrentlyEditing = args.getInt(STAGE_INDEX_KEY);
		}catch(JSONException e){
			e.printStackTrace();
			mCommandListener.navigateBack();
		}

		View rootView = inflater.inflate(R.layout.fragment_stage_editor, container, false);

		nameEditor = rootView.findViewById(R.id.name_field);
		nameEditor.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void afterTextChanged(Editable editable) {
				updateStageFromUI();
			}
		});
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
				Bundle data = new Bundle();
				data.putInt(JourneyActivity.DATA_TYPE_KEY, JourneyActivity.SAVE_STAGE_REQUEST_TYPE);
				data.putString(JourneyActivity.NEW_STAGE_KEY, currentlyEditing.toString());
				data.putInt(JourneyActivity.NEW_STAGE_INDEX_KEY, indexOfCurrentlyEditing);
				mCommandListener.getDataFromFragment(data);
			}
		});
		updateUIFromStage();

		return rootView;
	}
	@Override
	protected void updateTitle(){
		mCommandListener.setToolbarTitle("Editing Stage");
	}

	void updateUIFromStage(){
		nameEditor.setText(currentlyEditing.name);
		updateApplyButtonVisibility();
	}
	void updateStageFromUI(){
		currentlyEditing.name = nameEditor.getText().toString();
		updateApplyButtonVisibility();
	}
	void updateApplyButtonVisibility(){
		if (currentlyEditing.name.length() == 0){
			applyButton.setVisibility(View.GONE);
		}else
			applyButton.setVisibility(View.VISIBLE);
	}
}
