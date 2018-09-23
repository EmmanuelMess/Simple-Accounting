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

import java.math.BigDecimal;

import static com.emmanuelmess.simpleaccounting.activities.MainActivity.EDIT_IDS;
import static com.emmanuelmess.simpleaccounting.activities.MainActivity.TEXT_IDS;

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

	private BalanceFormatter formatter;

	public LedgerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		addView(inflate(getContext(), R.layout.view_ledger, null));

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setFormatter(BalanceFormatter f) {
		formatter = f;
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
	 * Creates and inflates a new row.
     * Restores editable row to view.
	 */
	public View inflateEmptyRow() {
		editableRowToView();
		return inflateRow();
	}

	public void rowViewToEditable(int index) {
		if(index <= 0) throw new IllegalArgumentException("Can't edit table header!");

		final LedgerRow row = (LedgerRow) getChildAt(index);

		row.makeRowEditable();

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
		LedgerRow row = (LedgerRow) getChildAt(editableRow);
		if (row != null && editableRow >= 0) {
			listener.onBeforeMakeRowNotEditable(row);

			updateEditableRow(-1);

			row.makeRowNotEditable();

			listener.onAfterMakeRowNotEditable(row);
		}
	}

	public boolean isEditingRow() {
		return editableRow != -1;
	}

	public void clear() {
		for(int i = getChildCount()-1; i > 0; i--) {//DO NOT remove first line, the column titles
			removeViewAt(i);
		}

		updateEditableRow(-1);
	}

	private LedgerRow inflateRow() {
		inflater.inflate(R.layout.row_main, this);
		editableRow = getChildCount() -1;
		LedgerRow row = (LedgerRow) getChildAt(editableRow);
		row.setFormatter(formatter);

		if (invertCreditAndDebit) {
			row.invertDebitCredit();
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

	public interface BalanceFormatter {
		String format(BigDecimal balance);
	}

}
