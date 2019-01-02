package com.emmanuelmess.simpleaccounting.fragments.settings;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import android.text.Editable;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.activities.MainActivity;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.activities.preferences.CurrencyPicker;
import com.emmanuelmess.simpleaccounting.activities.views.LockableScrollView;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;
import com.emmanuelmess.simpleaccounting.utils.RangedStructure;
import com.emmanuelmess.simpleaccounting.utils.SimpleTextWatcher;
import com.emmanuelmess.simpleaccounting.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CurrencyPickerFragment extends PreferenceDialogFragmentCompat implements View.OnClickListener {

	public static final String CURRENT_VALUE_KEY = "currency_picker";
	public static final String DFLT = "DFLT";

	public static CurrencyPickerFragment newInstance(String key) {
		CurrencyPickerFragment fragment = new CurrencyPickerFragment();
		Bundle b = new Bundle(1);
		b.putString("key", key);
		fragment.setArguments(b);
		return fragment;
	}

	private final ArrayList<String> DEFAULT_VALUE = new ArrayList<>();

	private ArrayList<String> currentValue = new ArrayList<>();
	private LayoutInflater inflater;
	private boolean firstTimeItemHeighted = true;
	private SparseIntArray itemPos = new SparseIntArray(1);
	private RangedStructure itemPosRanges = new RangedStructure();
	private ArrayList<Boolean> isItemNew = new ArrayList<>();
	private ArrayList<String> deleteElements = new ArrayList<>();
	private LinearLayout linearLayout;
	private View deleteConfirmation;
	private View add;
	private EditText textDefault;
	private TextView textItemToDelete;
	private LockableScrollView scrollView;

	private int dialogBackground;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		CurrencyPicker currencyPicker = (CurrencyPicker) getPreference();

		if(savedInstanceState == null) {
			currentValue = currencyPicker.getPersistedStringList(DEFAULT_VALUE);
		} else {
			currentValue = savedInstanceState.getStringArrayList(CURRENT_VALUE_KEY);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putStringArrayList(CURRENT_VALUE_KEY, currentValue);
	}

	@Override
	protected View onCreateDialogView(Context context) {
		View view = super.onCreateDialogView(context);

		textDefault = view.findViewById(R.id.textDefault);

		if(currentValue.size() > 0)
			textDefault.setText(Utils.INSTANCE.equal(currentValue.get(0), DFLT)? "":currentValue.get(0));
		else
			currentValue.add("");

		textDefault.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				currentValue.set(0, s.toString());
			}
		});

		textItemToDelete = view.findViewById(R.id.textItemToDelete);
		linearLayout = view.findViewById(R.id.scrollView);
		scrollView = view.findViewById(R.id.scrollerView);
		add = view.findViewById(R.id.add);
		deleteConfirmation = view.findViewById(R.id.deleteConfirmation);
		deleteConfirmation.findViewById(R.id.cancel).setOnClickListener(v->{
			deleteConfirmation.setVisibility(GONE);
			scrollView.setVisibility(VISIBLE);
			add.setVisibility(VISIBLE);
		});
		add.setOnClickListener(this);

		return view;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);

		dialogBackground =
				Utils.INSTANCE.getBackgroundColor(dialog.getWindow().getDecorView().getBackground(), -1);

		for(int i = 1; i < currentValue.size(); i++)
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

		return dialog;
	}

	private void load(int i) {
		((EditText) createItem(()->{
			if(i+1 < currentValue.size())
				load(i+1);
		}).findViewById(R.id.text)).setText(currentValue.get(i));
	}

	@Override
	public void onClick(View v) {
		currentValue.add("");
		createItem(null);
	}

	private View createItem(OnFinishedLoadingListener loadingListener) {
		inflater.inflate(R.layout.item_currencypicker, linearLayout);
		View item = linearLayout.getChildAt(linearLayout.getChildCount() - 1);

		item.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			boolean alreadyLoaded = false;
			int childIndex = linearLayout.getChildCount() - 1;

			@Override
			public void onGlobalLayout() {
				if (!alreadyLoaded) {
					if(dialogBackground != -1)
						item.setBackgroundColor(dialogBackground);
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

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			item.findViewById(R.id.move).setOnTouchListener(new View.OnTouchListener() {
				float dy;

				@Override
				public boolean onTouch(View v1, MotionEvent event) {
					int childIndex = getChildIndex(item);

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
									+ item.getHeight() / 2f));

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
							moveTo = itemPos.get(itemPosRanges.get((int) (ViewCompat.getY(item) + item.getHeight() / 2f)));
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
					int toBeMovedIndex = itemIndex + (direction ? +1 : -1);
					if (toBeMovedIndex != getToPos)
						move(getToPos, toBeMovedIndex);

					swap(itemIndex, toBeMovedIndex);
				}
			});
		}


		EditText text = ((EditText) item.findViewById(R.id.text));
		text.setOnFocusChangeListener((v1, hasFocus)->{
			item.findViewById(R.id.delete).setVisibility(hasFocus? VISIBLE:View.INVISIBLE);
		});
		text.addTextChangedListener(new SimpleTextWatcher() {
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
			if(!isItemNew.get(childIndex))
				deleteElements.add(currentValue.get(childIndex+1));
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

	protected boolean needInputMethod() {
		return true;
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
		CurrencyPicker currencyPicker = (CurrencyPicker) getPreference();

		// When the user selects "OK", persist the new value
		if (positiveResult) {
			for (int i = 1; i < currentValue.size(); i++) // STARTS on 1 to save default
				if (Utils.INSTANCE.equal(currentValue.get(i).replace(" ", ""), ""))
					currentValue.remove(i);

			if(Utils.INSTANCE.equal(currentValue.get(0).replace(" ", ""), "")) {
				if(currentValue.size() == 1)
					currentValue.remove(0);
				else
					currentValue.set(0, DFLT);
			}

			if(deleteElements.size() > 0) {
				TableGeneral tableGeneral = new TableGeneral(getContext());//DO NOT change the order of table creation!
				TableMonthlyBalance tableMonthlyBalance = new TableMonthlyBalance(getContext());

				boolean deletedCurrencyWasSelected = false;

				for (String s : deleteElements) {
					if(Utils.INSTANCE.equal(MainActivity.getCurrency(), s))
						deletedCurrencyWasSelected = true;

					tableGeneral.deleteAllForCurrency(s);
					tableMonthlyBalance.deleteAllForCurrency(s);
				}

				if(deletedCurrencyWasSelected)
					MainActivity.setCurrency(""); //Default is "" (check MainActivity.editableCurrency)
			}

			if(currencyPicker.callChangeListener(currentValue)) {
				currencyPicker.setCurrencyList(currentValue);
			}
			MainActivity.invalidateToolbar();
		} else currentValue = currencyPicker.getPersistedStringList(DEFAULT_VALUE);

		deleteElements.clear();
		itemPos.clear();
		itemPosRanges.clear();
		isItemNew.clear();
	}

}
