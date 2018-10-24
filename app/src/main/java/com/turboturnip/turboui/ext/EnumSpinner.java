package com.turboturnip.turboui.ext;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

public class EnumSpinner<T extends Enum<T>> extends AppCompatSpinner {
	private List<T> values;

	public EnumSpinner(Context context, int itemLayout, int textIdInItemLayout, T... values){
		super(context);
		this.values = Arrays.asList(values);

		String[] valuesAsStrings = new String[values.length];
		for (int i = 0; i < values.length; i++) valuesAsStrings[i] = values[i].toString();
		this.setAdapter(new ArrayAdapter<>(context, itemLayout, textIdInItemLayout, valuesAsStrings));
	}

	public T getSelectedValue(){
		if (getSelectedItemPosition() == AdapterView.INVALID_POSITION) return null;
		return values.get(getSelectedItemPosition());
	}
	public void setSelection(T value){
		setSelection(values.indexOf(value));
	}
	public void setSelection(T value, boolean animate){
		setSelection(values.indexOf(value), animate);
	}
}
