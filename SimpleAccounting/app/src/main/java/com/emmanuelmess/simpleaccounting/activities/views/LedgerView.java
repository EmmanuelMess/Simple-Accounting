package com.emmanuelmess.simpleaccounting.activities.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.R;

import static com.emmanuelmess.simpleaccounting.MainActivity.EDIT_IDS;
import static com.emmanuelmess.simpleaccounting.MainActivity.TEXT_IDS;

/**
 * @author Emmanuel
 *         on 27/7/2017, at 18:29.
 */

public class LedgerView extends TableLayout {

	private LayoutInflater inflater;
	private LedgeCallbacks listener;

	private boolean invertCreditAndDebit = false;
	/**
	 * pointer to row being edited STARTS IN 1
	 */
	private int editableRow = -1;

	public LedgerView(Context context) {
		super(context);
		createInstance(context);
	}

	public LedgerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		createInstance(context);
	}

	private void createInstance(Context c) {
		addView(inflate(getContext(), R.layout.view_ledger, null));

		inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setListener(LedgeCallbacks l) {
		listener = l;
	}

	public void setInvertCreditAndDebit(boolean invert) {
		if(invert != invertCreditAndDebit) {
			int tempId = 0;

			findViewById(R.id.credit).setId(tempId);
			findViewById(R.id.debit).setId(R.id.credit);
			findViewById(tempId).setId(R.id.debit);

			((TextView) findViewById(R.id.credit)).setText(R.string.credit);
			((TextView) findViewById(R.id.debit)).setText(R.string.debit);

			invertCreditAndDebit = invert;
		}
	}

	public int getEditableRow() {
		return editableRow;
	}

	public TableRow getLastRow() {
		int rowViewIndex = getChildCount() - 1;
		return (TableRow) getChildAt(rowViewIndex);
	}

	/**
	 * Creates and inflates a new row
	 */
	public View inflateEmptyRow() {
		return inflateRow();
	}

	public void rowViewToEditable(int index) {
		if(index <= 0) throw new IllegalArgumentException("Can't edit table header!");

		final View row = getChildAt(index);

		for (int i = 0; i < TEXT_IDS.length; i++) {
			TextView t1 = (TextView) row.findViewById(TEXT_IDS[i]);
			EditText t = (EditText) row.findViewById(EDIT_IDS[i]);

			t.setText(t1.getText());
			t1.setText("");

			t1.setVisibility(GONE);
			t.setVisibility(VISIBLE);
		}

		updateEditableRow(index);
	}

	/**
	 * Converts editable row into not editable.
	 */
	public void editableRowToView() {
		View row = getChildAt(editableRow);
		if (row != null && editableRow >= 0) {
			listener.onBeforeMakeRowNotEditable(row);

			updateEditableRow(-1);

			for (int i = 0; i < TEXT_IDS.length; i++) {
				EditText t = row.findViewById(EDIT_IDS[i]);
				TextView t1 = row.findViewById(TEXT_IDS[i]);

				t.setOnTouchListener(null);

				t1.setText(t.getText());
				t.setText("");

				t.setVisibility(GONE);
				t1.setVisibility(VISIBLE);
			}

			listener.onAfterMakeRowNotEditable(row);
		}
	}

	public boolean isEditingRow() {
		return editableRow != -1;
	}

	public void clear() {
		removeAllViews();
		updateEditableRow(-1);
	}

	private View inflateRow() {
		inflater.inflate(R.layout.row_main, this);

		View row = getChildAt(getChildCount() - 1);

		if (invertCreditAndDebit) {
			row.findViewById(R.id.textCredit).setId(0);
			row.findViewById(R.id.textDebit).setId(R.id.textCredit);
			row.findViewById(0).setId(R.id.textDebit);

			row.findViewById(R.id.editCredit).setId(0);
			row.findViewById(R.id.editDebit).setId(R.id.editCredit);
			row.findViewById(0).setId(R.id.editDebit);

			((EditText) row.findViewById(R.id.editCredit)).setHint(R.string.credit);
			((EditText) row.findViewById(R.id.editDebit)).setHint(R.string.debit);
		}

		return row;
	}

	private void updateEditableRow(int index) {
		listener.onUpdateEditableRow(index);
		editableRow = index;
	}

	public interface LedgeCallbacks {
		void onUpdateEditableRow(int index);
		void onBeforeMakeRowNotEditable(View row);
		void onAfterMakeRowNotEditable(View row);
	}

}
