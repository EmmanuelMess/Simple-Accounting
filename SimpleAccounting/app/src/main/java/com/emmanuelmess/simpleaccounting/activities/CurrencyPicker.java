package com.emmanuelmess.simpleaccounting.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.activities.views.ScrollViewWithMaxHeight;
import com.emmanuelmess.simpleaccounting.utils.RangedStructure;
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
	private boolean firstTimeItemHeighted = true;

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
		super.onPrepareDialogBuilder(builder);
	}

	@Override
	public void onBindDialogView(View view) {
		SparseIntArray itemPos = new SparseIntArray(1);
		RangedStructure itemPosRanges = new RangedStructure();

		view.findViewById(R.id.add).setOnClickListener(v->{
			ScrollViewWithMaxHeight scrollView = ((ScrollViewWithMaxHeight) view.findViewById(R.id.scrollerView));
			LinearLayout linearLayout = ((LinearLayout) view.findViewById(R.id.scrollView));
			inflater.inflate(R.layout.currencypicker_dialog_item, linearLayout);

			int childIndex = linearLayout.getChildCount()-1;
			View item = linearLayout.getChildAt(childIndex);

			item.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				boolean alreadyLoaded = false;
				@Override
				public void onGlobalLayout() {
					if(!alreadyLoaded) {
						itemPos.append(childIndex, item.getTop());
						itemPosRanges.add(item.getTop(), item.getBottom());
						alreadyLoaded = true;
					}
				}
			});

			item.getLayoutParams().width = linearLayout.getWidth();

			if(linearLayout.getChildCount() == 2 && firstTimeItemHeighted) {
				firstTimeItemHeighted = false;
				scrollView.setMaxHeight(linearLayout.getChildAt(0).getHeight()*4);
			}

			item.findViewById(R.id.move).setOnTouchListener(new View.OnTouchListener() {
				float dY;

				private void move(int getToPos, int itemIndex) {
					if(getToPos == itemIndex) return;

					boolean direction = getToPos > itemIndex;
					int toBeMovedIndex = itemIndex + (direction? +1:-1);
					if(toBeMovedIndex != getToPos)
						move(getToPos, toBeMovedIndex);

					//swap
					Integer m = itemPos.get(itemIndex);
					itemPos.removeAt(itemIndex);
					itemPos.append(itemIndex, itemPos.get(toBeMovedIndex));
					itemPos.removeAt(toBeMovedIndex);
					itemPos.append(toBeMovedIndex, m);

					itemPosRanges.swap(itemIndex, toBeMovedIndex);
				}

				@Override
				public boolean onTouch(View v1, MotionEvent event) {
					switch (event.getAction()) {

						case MotionEvent.ACTION_DOWN:
							ViewCompat.animate(item).z(5).setDuration(0).start();
							dY = ViewCompat.getY(item) - event.getRawY();
							break;
						case MotionEvent.ACTION_MOVE:
							float moveTo = event.getRawY() + dY;

							if (moveTo < ViewCompat.getY(linearLayout))
								moveTo = ViewCompat.getY(linearLayout);
							else if (moveTo > ViewCompat.getTranslationY(scrollView)
									+ scrollView.getBottom() - item.getHeight())
								moveTo = ViewCompat.getTranslationY(scrollView)
										+ scrollView.getBottom() - item.getHeight();

							ViewCompat.animate(item).y(moveTo).setDuration(0).start();

							int overItemIndex = itemPosRanges.get((int) (ViewCompat.getY(item) + item.getHeight()/2f));

							move(childIndex, overItemIndex);

							//reposition
							for (int i = 0; i < linearLayout.getChildCount(); i++) {
								if (childIndex == i) continue;
								ViewCompat.animate(linearLayout.getChildAt(i)).z(2.5f)
										.y(itemPos.get(i)).z(0).setDuration(0).start();
							}
							break;
						case MotionEvent.ACTION_UP:
							moveTo = itemPos.get(itemPosRanges.get((int) (ViewCompat.getY(item) + item.getHeight()/2f)));
							ViewCompat.animate(item).y(moveTo).z(0).setDuration(0).start();
							break;
						default:
							return false;
					}
					return true;
				}
			});

			EditText text = ((EditText) item.findViewById(R.id.text));
			text.setOnFocusChangeListener((v1, hasFocus)->{
				item.findViewById(R.id.delete).setVisibility(hasFocus? View.VISIBLE:View.INVISIBLE);

				InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(text, hasFocus? WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE:
						InputMethodManager.RESULT_UNCHANGED_SHOWN);
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
				if(childIndex > 0 && linearLayout.getChildAt(childIndex-1) != null)
					linearLayout.getChildAt(childIndex-1).findViewById(R.id.text).requestFocus();
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

	private int getAbsoluteYInScreen(View v) {
		int[] m = new int[] {0, 0};
		v.getLocationOnScreen(m);
		return m[1];
	}

}
