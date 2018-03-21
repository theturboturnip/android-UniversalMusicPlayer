package com.turboturnip.turboui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.turboturnip.turboui.R;
import com.turboturnip.turboui.ext.EnumSpinner;

public abstract class EditorCommandFragment extends CommandFragment {

	private LinearLayout optionsParent;
	protected Button cancelButton, applyButton;

	private LayoutInflater inflater;

	private boolean isCreatingUI = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findEditingObject();
		setRetainInstance(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
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
		return inflater.inflate(layout, (currentView == null ? null : (ViewGroup)currentView.getParent()), false);
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
		view.setOnItemSelectedListener(new AdapterViewWatcher());
	}
	protected void startWatchingField(EditText view){
		view.addTextChangedListener(new EditTextWatcher());
	}
	protected final View createCustomField(String name, int layout){
		if (getContext() == null) return null;

		View result = inflateLayout(layout);
		createGenericOption(name, result);
		return result;
	}
	protected final EditText createTextField(String name){
		if (getContext() == null) return null;

		EditText result = new EditText(getContext());
		result.setMaxLines(1);
		result.setInputType(InputType.TYPE_CLASS_TEXT);
		startWatchingField(result);
		createGenericOption(name, result);
		return result;
	}
	protected final EditText createNumberField(String name){
		if (getContext() == null) return null;

		EditText result = new EditText(getContext());
		result.setMaxLines(1);
		result.setInputType(InputType.TYPE_CLASS_NUMBER);
		startWatchingField(result);
		createGenericOption(name, result);
		return result;
	}
	@SafeVarargs
	protected final <E extends Enum<E>> EnumSpinner<E> createEnumSpinner(String name, E... values){
		if (getContext() == null) return null;

		EnumSpinner<E> result = new EnumSpinner<>(getContext(), R.layout.editor_dropdown_text, R.id.dropdown_text, values);
		startWatchingField(result);

		createGenericOption(name, result);
		return result;
	}
	protected final ListView createList(String name, ListAdapter adapter){
		if (getContext() == null) return null;

		ListView result = new ListView(getContext());
		result.setDivider(null);
		result.setDividerHeight(0);
		result.setAdapter(adapter);
		startWatchingField(result);

		createGenericOption(name, result, R.layout.editor_list_option);
		return result;
	}
	protected final RecyclerView createLinearRecycler(String name, RecyclerView.Adapter adapter){
		if (getContext() == null) return null;

		RecyclerView result = new RecyclerView(getContext());
		LinearLayoutManager manager = new LinearLayoutManager(getContext());
		result.setLayoutManager(manager);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
				manager.getOrientation());
		result.addItemDecoration(dividerItemDecoration);
		result.setAdapter(adapter);
		adapter.registerAdapterDataObserver(new AdapterWatcher());

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
	protected class AdapterViewWatcher implements AdapterView.OnItemSelectedListener{
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			onUIUpdate();
		}

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
			onUIUpdate();
		}
	}
	protected class AdapterWatcher extends RecyclerView.AdapterDataObserver{
		@Override
		public void onChanged() {
			super.onChanged();
			onUIUpdate();
		}
	}
}
