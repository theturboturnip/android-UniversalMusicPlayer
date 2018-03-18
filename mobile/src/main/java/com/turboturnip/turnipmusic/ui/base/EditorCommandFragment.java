package com.turboturnip.turnipmusic.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.ui.ext.EnumSpinner;

public abstract class EditorCommandFragment extends CommandFragment {

	private LinearLayout optionsParent;
	protected Button cancelButton, applyButton;

	private LayoutInflater inflater;

	private boolean isCreatingUI = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findEditingObject();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;

		View rootView = inflater.inflate(R.layout.fragment_editor, container, false);

		optionsParent = rootView.findViewById(R.id.options);

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
				onApply();
				mCommandListener.navigateBack();
			}
		});

		isCreatingUI = true;
		createEditUI();
		applyObjectValuesToUI();
		updateApplyButtonVisibility();
		isCreatingUI = false;

		return rootView;
	}

	protected abstract void findEditingObject();
	protected abstract void createEditUI();

	protected abstract boolean canApply();
	protected abstract void onApply();

	protected abstract void applyObjectValuesToUI();
	protected abstract void applyUIValuesToObject();

	private void updateApplyButtonVisibility(){
		applyButton.setVisibility(canApply() ? View.VISIBLE : View.GONE);
	}
	private void onUIUpdate(){
		if (isCreatingUI) return;
		applyUIValuesToObject();
		updateApplyButtonVisibility();
	}

	protected void addCustomOption(View optionView){
		optionsParent.addView(optionView);
	}
	private View inflateLayout(int layout){
		View currentView = getView();
		return getLayoutInflater().inflate(layout, (currentView == null ? null : (ViewGroup)currentView.getParent()), false);
	}
	private void createGenericOption(String name, View optionView){
		createGenericOption(name, optionView, R.layout.editor_option);
	}
	private void createGenericOption(String name, View optionView, int optionLayout){
		View optionHolderView = inflateLayout(optionLayout);
		((TextView)optionHolderView.findViewById(R.id.name)).setText(name);
		((LinearLayout)optionHolderView.findViewById(R.id.option)).addView(optionView);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		optionView.setLayoutParams(params);
		optionsParent.addView(optionHolderView);
	}

	protected void startWatchingField(AdapterView view){
		view.setOnItemSelectedListener(new AdapterWatcher());
	}
	protected void startWatchingField(EditText view){
		view.addTextChangedListener(new EditTextWatcher());
	}
	protected View createCustomField(String name, int layout){
		View result = inflateLayout(layout);
		createGenericOption(name, result);
		return result;
	}
	protected EditText createTextField(String name){
		EditText result = new EditText(getContext());
		result.setMaxLines(1);
		result.setInputType(InputType.TYPE_CLASS_TEXT);
		startWatchingField(result);
		createGenericOption(name, result);
		return result;
	}
	protected EditText createNumberField(String name){
		EditText result = new EditText(getContext());
		result.setMaxLines(1);
		result.setInputType(InputType.TYPE_CLASS_NUMBER);
		startWatchingField(result);
		createGenericOption(name, result);
		return result;
	}
	protected <E extends Enum<E>> EnumSpinner<E> createEnumSpinner(String name, E... values){
		EnumSpinner<E> result = new EnumSpinner<>(getContext(), R.layout.edit_dropdown_text, R.id.dropdown_text, values);
		startWatchingField(result);

		createGenericOption(name, result);
		return result;
	}
	protected ListView createList(String name, ListAdapter adapter){
		ListView result = new ListView(getContext());
		result.setDivider(null);
		result.setDividerHeight(0);
		result.setAdapter(adapter);
		startWatchingField(result);

		createGenericOption(name, result, R.layout.editor_list_option);
		return result;
	}

	protected class EditTextWatcher implements TextWatcher{
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void afterTextChanged(Editable editable) {
			onUIUpdate();
		}
	}
	protected class AdapterWatcher implements AdapterView.OnItemSelectedListener{
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			onUIUpdate();
		}

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
			onUIUpdate();
		}
	}
}
