package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emmanuelmess.simpleaccounting.IO.FileIO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
	private TableLayout table = null;
	private FileIO f;
	private final int[] ids = {R.id.editDate, R.id.editRef, R.id.editCredit, R.id.editDebt, R.id.editBalance};
	private float size, finalSize = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
		table = (TableLayout) findViewById(R.id.table);
		f = new FileIO(getApplicationContext());

		table.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int[] normalIDs = {R.id.date, R.id.ref, R.id.credit, R.id.debit, R.id.balance};
				View headerRow = table.getChildAt(0);
				TextView lastColumn = (TextView) headerRow.findViewById(normalIDs[4]);

				if (!isTooLarge(lastColumn, lastColumn.getText().toString())) {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
						table.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					else//noinspection deprecation
						table.getViewTreeObserver().removeGlobalOnLayoutListener(this);

					findViewById(R.id.space).setMinimumHeight(findViewById(R.id.fab).getHeight() - findViewById(R.id.fab).getPaddingTop());

					finalSize = size;
					String[][] dbRows = f.getAll();
					for (String[] dbRow : dbRows) {
						inflater.inflate(R.layout.newrow_main, table);

						View row = loadRow();

						for (int j = 0; j < ids.length; j++) {
							TextView t = (TextView) row.findViewById(ids[j]);
							t.setTextSize(finalSize);
							t.setText(dbRow[j]);
						}
					}
					scrollView.fullScroll(View.FOCUS_DOWN);
				} else {
					for (int i = 0; i < 5; i++) {
						TextView t = (TextView) headerRow.findViewById(normalIDs[i]);
						size = t.getTextSize() - 0.5f;
						t.setTextSize(size);
					}
				}
			}
		});

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (finalSize == 0) {
					Toast.makeText(getApplicationContext(), R.string.loading, Toast.LENGTH_SHORT).show();
				} else {
					inflater.inflate(R.layout.newrow_main, table);

					scrollView.fullScroll(View.FOCUS_DOWN);

					f.newRow();
					View row = loadRow();

					for (int i = 0; i < 5; i++) {
						TextView v = (TextView) row.findViewById(ids[i]);
						v.setTextSize(finalSize);
					}

					EditText date = (EditText) row.findViewById(R.id.editDate);
					date.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));

					row.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(date, InputMethodManager.SHOW_IMPLICIT);
				}
			}
		});
	}

	private View loadRow() {
		int rowViewIndex = table.getChildCount() - 1, dbIndex = rowViewIndex - 1;
		View row = table.getChildAt(rowViewIndex);
		checkStatus(rowViewIndex, row);
		f.update(dbIndex, FileIO.COLUMNS[4], "$0.0");
		addToDB(dbIndex, row);
		return row;
	}

	private void checkStatus(final int index, View row) {
		final EditText debt = (EditText) row.findViewById(R.id.editDebt),
				credit = (EditText) row.findViewById(R.id.editCredit);

		final TextView lastBalance = index > 1? (TextView) table.getChildAt(index-1).findViewById(R.id.editBalance):null,
				balance = (TextView) row.findViewById(R.id.editBalance);

		balance.setText(lastBalance != null? lastBalance.getText():"$ 0.0");

		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				double balanceNum;
				balanceNum = lastBalance != null? parse(lastBalance.getText().toString().substring(1)):0;
				balanceNum += parse(credit.getText().toString());
				balanceNum -= parse(debt.getText().toString());

				String s = "$ " + balanceNum;
				balance.setText(s);
			}
		};

		credit.addTextChangedListener(watcher);
		debt.addTextChangedListener(watcher);
		if (lastBalance != null)
			lastBalance.addTextChangedListener(watcher);
	}

	private double parse(String s) {
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void addToDB(final int index, View row) {
		for (int i = 0; i < ids.length; i++) {
			final String rowName = FileIO.COLUMNS[i];
			TextWatcher watcher = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					if (!equal(editable.toString(), ""))
						f.update(index, rowName, editable.toString());
				}
			};

			((TextView) row.findViewById(ids[i])).addTextChangedListener(watcher);
		}
	}

	private boolean equal(Object o1, Object o2) {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Objects.equals(o1, o2)) || o1.equals(o2);
	}

	private boolean isTooLarge (TextView text, String newText) {
		float textWidth = text.getPaint().measureText(newText);
		return (textWidth+2 >= text.getMeasuredWidth ());
	}

}
