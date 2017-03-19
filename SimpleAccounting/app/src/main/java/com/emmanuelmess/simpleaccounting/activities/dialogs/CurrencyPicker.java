package com.emmanuelmess.simpleaccounting.activities.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.MainActivity;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.activities.views.LockableScrollView;
import com.emmanuelmess.simpleaccounting.utils.RangedStructure;
import com.emmanuelmess.simpleaccounting.utils.TinyDB;
import com.emmanuelmess.simpleaccounting.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * @author Emmanuel
 *         on 24/1/2017, at 22:55.
 */

public class CurrencyPicker extends DialogPreferenceWithKeyboard implements View.OnClickListener {
	public static final String KEY = "currency_picker";
	private static final ArrayList<String> DEFAULT_VALUE = new ArrayList<>();

	private TinyDB tinyDB;
	private ArrayList<String> currentValue = new ArrayList<>();
	private boolean hasValueChanged = true;
	private LayoutInflater inflater;
	private boolean firstTimeItemHeighted = true;
	private SparseIntArray itemPos = new SparseIntArray(1);
	private RangedStructure itemPosRanges = new RangedStructure();
	private ArrayList<Boolean> isItemNew = new ArrayList<>();
	private LinearLayout linearLayout;
	private View deleteConfirmation;
	private View add;
	private EditText textDefault;
	private TextView textItemToDelete;
	private LockableScrollView scrollView;

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
		textDefault = ((EditText) view.findViewById(R.id.textDefault));

		if(currentValue.size() > 0)
			textDefault.setText(Utils.equal(currentValue.get(0), getContext().getString(R.string.default_short))? "":currentValue.get(0));
		else currentValue.add("");
		textDefault.addTextChangedListener(new Utils.SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				currentValue.set(0, s.toString());
			}
		});

		textItemToDelete = (TextView) view.findViewById(R.id.textItemToDelete);
		linearLayout = ((LinearLayout) view.findViewById(R.id.scrollView));
		scrollView = ((LockableScrollView) view.findViewById(R.id.scrollerView));
		add = view.findViewById(R.id.add);
		deleteConfirmation = view.findViewById(R.id.deleteConfirmation);
		deleteConfirmation.findViewById(R.id.cancel).setOnClickListener(v->{
			deleteConfirmation.setVisibility(GONE);
			scrollView.setVisibility(VISIBLE);
			add.setVisibility(VISIBLE);
		});
		add.setOnClickListener(this);
		super.onBindDialogView(view);
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		for(String s : currentValue)
			isItemNew.add(false);

		scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			boolean alreadyLoaded = false;
			@Override
			public void onGlobalLayout() {
				if(!alreadyLoaded) {
					deleteConfirmation.getLayoutParams().width = scrollView.getWidth();
					deleteConfirmation.setVisibility(GONE);
					if(currentValue.size() > 1)
						load(1);
					alreadyLoaded = true;
				}
			}
		});
	}

	private void load(int i) {
		((EditText) createItem(()->{
			if(i+1 < currentValue.size())
				load(i+1);
		}).findViewById(R.id.text)).setText(currentValue.get(i));
	}

	@Override
	public boolean needInputMethod() {
		return true;
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			currentValue = getPersistedStringList(DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			currentValue = new ArrayList<>(Arrays.asList(TextUtils.split((String) defaultValue, "‚‗‚")));
			hasValueChanged = true;
			persistStringList(currentValue);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			for (int i = 1; i < currentValue.size(); i++) // STARTS on 1 to save default
				if (Utils.equal(currentValue.get(i).replace(" ", ""), ""))
					currentValue.remove(i);

			if(Utils.equal(currentValue.get(0).replace(" ", ""), "")) {
				if(currentValue.size() == 1)
					currentValue.remove(0);
				else
					currentValue.set(0, getContext().getString(R.string.default_short));
			}

			persistStringList(currentValue);
			hasValueChanged = true;
			notifyChanged();
			MainActivity.invalidateToolbar();
		} else currentValue = getPersistedStringList(DEFAULT_VALUE);

		itemPos.clear();
		itemPosRanges.clear();
		isItemNew.clear();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	public CharSequence getSummary() {
		currentValue = getPersistedStringList(DEFAULT_VALUE);
		if(currentValue.size() != 0) {
			String[] myStringList = currentValue.toArray(new String[currentValue.size()]);
			return TextUtils.join(", ", myStringList);
		} else return getContext().getString(R.string.with_no_items_deactivated);
	}

	/**
	 * Attempts to persist an ArrayList&lt;String&gt; to the {@link android.content.SharedPreferences}.
	 *
	 * @param value The value to persist.
	 * @return True if the Preference is persistent. (This is not whether the
	 * value was persisted, since we may not necessarily commit if there
	 * will be a batch commit later.)
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
	 *                     Preference is not persistent or this Preference is not in the
	 *                     SharedPreferences.
	 * @return The value from the SharedPreferences or the default return
	 * value.
	 * @see #getPersistedString(String)
	 * @see #persistInt(int)
	 */
	private ArrayList<String> getPersistedStringList(ArrayList<String> defaultValue) {
		if (!shouldPersist()) {
			return defaultValue;
		} else //if(hasValueChanged) {
		//	hasValueChanged = false;
			return tinyDB.getListString(KEY);
		//} else return currentValue;
	}

	@Override
	public void onClick(View v) {
		currentValue.add("");
		createItem(null);
	}

	private View createItem(OnFinishedLoadingListener loadingListener) {
		inflater.inflate(R.layout.currencypicker_dialog_item, linearLayout);
		View item = linearLayout.getChildAt(linearLayout.getChildCount() - 1);

		item.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			boolean alreadyLoaded = false;
			int childIndex = linearLayout.getChildCount() - 1;

			@Override
			public void onGlobalLayout() {
				if (!alreadyLoaded) {
					itemPos.append(childIndex, item.getTop());
					itemPosRanges.add(item.getTop(), item.getBottom());
					isItemNew.add(true);
					alreadyLoaded = true;
					if(loadingListener != null)
						loadingListener.onFinishedLoading();
				}
			}
		});

		item.getLayoutParams().width = linearLayout.getWidth();

		if (linearLayout.getChildCount() == 2 && firstTimeItemHeighted) {
			firstTimeItemHeighted = false;
			scrollView.setMaxHeight(linearLayout.getChildAt(0).getHeight()*3);
		}

		item.findViewById(R.id.move).setOnTouchListener(new View.OnTouchListener() {
			float dy;

			@Override
			public boolean onTouch(View v1, MotionEvent event) {
				int childIndex = getChildIndex(item);

				hasValueChanged = true;

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						ViewCompat.animate(item).z(5).setDuration(0).start();
						dy = ViewCompat.getY(item) - event.getRawY();
						scrollView.setScrollingEnabled(false);
						break;
					case MotionEvent.ACTION_MOVE:
						float moveTo = event.getRawY() + dy;

						if (moveTo < ViewCompat.getY(linearLayout))
							moveTo = ViewCompat.getY(linearLayout);
						else if (moveTo > ViewCompat.getTranslationY(scrollView)
								+ scrollView.getBottom() - item.getHeight())
							moveTo = ViewCompat.getTranslationY(scrollView)
									+ scrollView.getBottom() - item.getHeight();

						ViewCompat.animate(item).y(moveTo).setDuration(0).start();

						int overItemIndex = itemPosRanges.get((int) (ViewCompat.getY(item)
								+ item.getHeight()/2f));

						move(childIndex, overItemIndex);

						//reposition
						for (int i = 0; i < linearLayout.getChildCount(); i++) {
							if (childIndex == i) continue;
							ViewCompat.animate(linearLayout.getChildAt(i)).z(2.5f)
									.y(itemPos.get(i)).z(0).setDuration(0).start();
						}
						break;
					case MotionEvent.ACTION_UP:
						scrollView.setScrollingEnabled(true);
						moveTo = itemPos.get(itemPosRanges.get((int) (ViewCompat.getY(item) + item.getHeight()/2f)));
						ViewCompat.animate(item).y(moveTo).z(0).setDuration(0).start();
						break;
					default:
						return false;
				}
				return true;
			}

			private void move(int getToPos, int itemIndex) {
				if (getToPos == itemIndex) return;

				boolean direction = getToPos > itemIndex;
				int toBeMovedIndex = itemIndex + (direction? +1:-1);
				if (toBeMovedIndex != getToPos)
					move(getToPos, toBeMovedIndex);

				swap(itemIndex, toBeMovedIndex);
			}
		});

		EditText text = ((EditText) item.findViewById(R.id.text));
		text.setOnFocusChangeListener((v1, hasFocus)->{
			item.findViewById(R.id.delete).setVisibility(hasFocus? VISIBLE:View.INVISIBLE);
		});
		text.addTextChangedListener(new Utils.SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				currentValue.set(getChildIndex(item)+1, s.toString());
			}
		});

		item.findViewById(R.id.delete).setOnClickListener((v1)->{
			int childIndex = getChildIndex(item);
			if(isItemNew.get(childIndex))
				removeItem(item, childIndex);
			else
				animateDeleteConfirmation(item, childIndex);
		});

		return item;
	}

	private void animateDeleteConfirmation(View item, int childIndex) {
		textDefault.setVisibility(GONE);
		scrollView.setVisibility(GONE);
		add.setVisibility(GONE);

		textItemToDelete.setText(currentValue.get(childIndex+1));
		textItemToDelete.setVisibility(VISIBLE);

		deleteConfirmation.setVisibility(VISIBLE);
		deleteConfirmation.requestFocus();
		deleteConfirmation.findViewById(R.id.deleteData).setOnClickListener(v->{
			removeItem(item, childIndex);
			invisibilizeDeleteConfirmation();
		});
		deleteConfirmation.findViewById(R.id.cancel).setOnClickListener(v->{
			invisibilizeDeleteConfirmation();
		});
	}

	private void invisibilizeDeleteConfirmation() {
		deleteConfirmation.setVisibility(GONE);
		textItemToDelete.setVisibility(GONE);
		textItemToDelete.setText("");

		textDefault.setVisibility(VISIBLE);
		scrollView.setVisibility(VISIBLE);
		add.setVisibility(VISIBLE);
	}

	private void removeItem(View item, int childIndex) {
		currentValue.remove(childIndex+1);
		isItemNew.remove(childIndex);
		itemPos.removeAt(itemPos.size() - 1);
		itemPosRanges.remove(itemPosRanges.size() - 1);
		linearLayout.removeView(item);

		if (childIndex > 0 && linearLayout.getChildAt(childIndex - 1) != null)
			linearLayout.getChildAt(childIndex - 1).findViewById(R.id.text).requestFocus();
	}

	private interface OnFinishedLoadingListener {
		void onFinishedLoading();
	}

	private void swap(int i1, int i2) {
		Collections.swap(currentValue, i1+1, i2+1);//+1 to account for the default elem
		Collections.swap(isItemNew, i1+1, i2+1);//+1 to account for the default elem

		Integer m = itemPos.get(i1);
		itemPos.removeAt(i1);
		itemPos.append(i1, itemPos.get(i2));
		itemPos.removeAt(i2);
		itemPos.append(i2, m);

		itemPosRanges.swap(i1, i2);
	}

	private int getChildIndex(View v) {
		return linearLayout.indexOfChild(v);
	}

}
