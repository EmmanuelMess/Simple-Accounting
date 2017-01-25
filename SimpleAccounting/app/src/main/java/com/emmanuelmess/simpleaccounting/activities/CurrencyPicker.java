package com.emmanuelmess.simpleaccounting.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.utils.TinyDB;
import com.emmanuelmess.simpleaccounting.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Emmanuel
 *         on 24/1/2017, at 22:55.
 */

public class CurrencyPicker extends DialogPreference {
	private static final String KEY = "currency_picker";
	private static final ArrayList<String> DEFAULT_VALUE = new ArrayList<>();

	private TinyDB tinyDB;
	private ArrayList<String> currentValue = new ArrayList<>();
	private LayoutInflater inflater;

	public CurrencyPicker(Context context, AttributeSet attrs) {
		super(context, attrs);

		tinyDB = new TinyDB(context);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		setDialogLayoutResource(R.layout.currencypicker_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		setDialogIcon(null);
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		builder.setTitle(R.string.costumize_currencies);
		Dialog d = builder.create();
		d.show();
		d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		super.onPrepareDialogBuilder(builder);
	}

	@Override
	public void onBindDialogView(View view) {

		view.findViewById(R.id.add).setOnClickListener(v->{
			LinearLayout linearLayout = ((LinearLayout) view.findViewById(R.id.scrollView));
			inflater.inflate(R.layout.currencypicker_dialog_item, linearLayout);

			int childIndex = linearLayout.getChildCount()-1;
			View item = linearLayout.getChildAt(childIndex);

			item.findViewById(R.id.move).setOnTouchListener((v1, event)->{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
					item.setMinimumHeight(5);
				else if(event.getAction() == MotionEvent.ACTION_UP)
					item.setMinimumHeight(0);

				return false;
			});

			EditText text = ((EditText) item.findViewById(R.id.text));
			text.setOnFocusChangeListener((v1, hasFocus)->{
				item.findViewById(R.id.delete)
						.setVisibility(hasFocus? View.VISIBLE:View.INVISIBLE);
			});
			text.addTextChangedListener(new Utils.SimpleTextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if(currentValue.size() > childIndex)
						currentValue.set(childIndex, s.toString());
					else
						currentValue.add(childIndex, s.toString());
				}
			});

			item.findViewById(R.id.delete).setOnClickListener((v1)->{
				linearLayout.removeView(item);
			});
		});

		super.onBindDialogView(view);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			currentValue = this.getPersistedStringList(DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			currentValue = new ArrayList<>(Arrays.asList(TextUtils.split((String) defaultValue, "‚‗‚")));
			persistStringList(currentValue);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// When the user selects "OK", persist the new value
		if (positiveResult)
			persistStringList(currentValue);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	public CharSequence getSummary() {
		String[] myStringList = currentValue.toArray(new String[currentValue.size()]);
		return TextUtils.join(", ", myStringList);
	}

	/**
	 * Attempts to persist an ArrayList&lt;String&gt; to the {@link android.content.SharedPreferences}.
	 *
	 * @param value The value to persist.
	 * @return True if the Preference is persistent. (This is not whether the
	 *         value was persisted, since we may not necessarily commit if there
	 *         will be a batch commit later.)
	 * @see #persistString(String)
	 * @see #getPersistedInt(int)
	 */
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

	/**
	 * Attempts to get a persisted ArrayList&lt;String&gt; from the {@link android.content.SharedPreferences}.
	 *
	 * @param defaultValue The default value to return if either this
	 *            Preference is not persistent or this Preference is not in the
	 *            SharedPreferences.
	 * @return The value from the SharedPreferences or the default return
	 *         value.
	 * @see #getPersistedString(String)
	 * @see #persistInt(int)
	 */
	private ArrayList<String> getPersistedStringList(ArrayList<String> defaultValue) {
		if (!shouldPersist()) {
			return defaultValue;
		}

		return tinyDB.getListString(KEY);
	}

}
