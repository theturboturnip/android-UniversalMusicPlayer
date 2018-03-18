package com.turboturnip.turnipmusic.ui.roots;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.turboturnip.turnipmusic.model.Journey;
import com.turboturnip.turnipmusic.ui.base.CommandFragment;
import com.turboturnip.turnipmusic.ui.base.EditorCommandFragment;
import com.turboturnip.turnipmusic.ui.ext.EnumSpinner;

import org.json.JSONException;

public class JourneyStageEditFragment extends EditorCommandFragment {

	public static final String STAGE_TO_EDIT_KEY = "STAGE_TO_EDIT";
	public static final String STAGE_INDEX_KEY = "STAGE_INDEX";

	private Journey.Stage currentlyEditing;
	private int indexOfCurrentlyEditing;

	private EditText nameEditor, playCountEditor;
	private EnumSpinner<Journey.Stage.PlayType> playTypeSpinner;

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
	}

	@Override
	protected boolean canApply() {
		if (currentlyEditing.name.length() == 0) return false;
		if (currentlyEditing.playType == null) return false;
		if (currentlyEditing.playCount < 0) return false;
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
}
