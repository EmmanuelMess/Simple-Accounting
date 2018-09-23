package com.emmanuelmess.simpleaccounting.activities.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.utils.TinyDB;

import java.util.ArrayList;

public class CurrencyPicker extends DialogPreference {
	public static final String KEY = "currency_picker";
	public static final String DFLT = "DFLT";

	private final ArrayList<String> DEFAULT_VALUE = new ArrayList<>();

	private String summary = null;
	private TinyDB tinyDB;

	public CurrencyPicker(Context context, AttributeSet attrs) {
		super(context, attrs);

		tinyDB = new TinyDB(context);

		setDialogLayoutResource(R.layout.dialog_currencypicker);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);
		setTitle(R.string.costumize_currencies);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	public CharSequence getSummary() {
		if(summary == null) {
			summary = createSummary(getPersistedStringList(DEFAULT_VALUE));
		}

		return summary;
	}

	public void setCurrencyList(ArrayList<String> list) {
		persistStringList(list);
		notifyChanged();
	}

	@Override
	protected void notifyChanged() {
		summary = createSummary(getPersistedStringList(DEFAULT_VALUE));
		super.notifyChanged();
	}

	public ArrayList<String> getPersistedStringList(ArrayList<String> defaultValue) {
		return tinyDB.getListString(KEY);
	}

	private boolean persistStringList(ArrayList<String> value) {
		if (shouldPersist()) {
			if (value == getPersistedStringList(value)) {
				// It's already there, so the same as persisting
				return true;
			}

			tinyDB.putListString(KEY, value);
			return true;
		}
		return false;
	}

	private String createSummary(ArrayList<String> currentValue) {
		if (currentValue.size() != 0) {
			String[] myStringList = currentValue.toArray(new String[currentValue.size()]);
			return TextUtils.join(", ", myStringList);
		} else return getContext().getString(R.string.with_no_items_deactivated);
	}

}
