package com.emmanuelmess.simpleaccounting.activities.views;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.R;

import java.math.BigDecimal;

import static com.emmanuelmess.simpleaccounting.MainActivity.EDIT_IDS;
import static com.emmanuelmess.simpleaccounting.MainActivity.TEXT_IDS;

/**
 * @author Emmanuel
 *         on 7/8/2017, at 23:54.
 */

public class LedgerRow extends TableRow {

	protected LedgerView.BalanceFormatter formatter;

	private EditableViewPair<TextView, TextView, EditText> datePair, referencePair, creditPair, debitPair;

	private TextView balanceText;

	public LedgerRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		datePair = new EditableViewPair<>(findViewById(R.id.textDate), findViewById(R.id.editDate));
		referencePair = new EditableViewPair<>(findViewById(R.id.textRef), findViewById(R.id.editRef));
		creditPair = new EditableViewPair<>(findViewById(R.id.textCredit), findViewById(R.id.editCredit));
		debitPair = new EditableViewPair<>(findViewById(R.id.textDebit), findViewById(R.id.editDebit));

		balanceText = findViewById(R.id.textBalance);
	}

	public void setDate(String date) {
		datePair.get().setText(date);
	}

	public void setReference(@StringRes int ref) {
	    referencePair.get().setText(ref);
	}

	public void setReference(String ref) {
		referencePair.get().setText(ref);
	}

	public void setCredit(String credit) {
		creditPair.get().setText(credit);
	}

	public CharSequence getCreditText() {
	    return creditPair.get().getText();
	}

	public void setDebit(String debit) {
		debitPair.get().setText(debit);
	}

	public CharSequence getDebitText() {
		return debitPair.get().getText();
	}

	public void setCredit(BigDecimal credit) {
		if(creditPair.isBeingEdited()) {
			throw new IllegalStateException("setCredit(BigDecimal) CANNOT be used while the row is editable!");
		}

		creditPair.get().setText(credit.toPlainString());
	}

	public void setDebit(BigDecimal debit) {
		if(debitPair.isBeingEdited()) {
			throw new IllegalStateException("setDebit(BigDecimal) CANNOT be used while the row is editable!");
		}

		debitPair.get().setText(debit.toPlainString());
	}

	public void setBalance(BigDecimal balance) {
		balanceText.setText(formatter.format(balance));
	}

	public CharSequence getBalanceText() {
		return balanceText.getText();
	}

	public void invertDebitCredit() {
		findViewById(R.id.textCredit).setId(0);
		findViewById(R.id.textDebit).setId(R.id.textCredit);
		findViewById(0).setId(R.id.textDebit);

		findViewById(R.id.editCredit).setId(0);
		findViewById(R.id.editDebit).setId(R.id.editCredit);
		findViewById(0).setId(R.id.editDebit);

		((EditText) findViewById(R.id.editCredit)).setHint(R.string.credit);
		((EditText) findViewById(R.id.editDebit)).setHint(R.string.debit);

		creditPair = new EditableViewPair<>(findViewById(R.id.textCredit), findViewById(R.id.editCredit));
		debitPair = new EditableViewPair<>(findViewById(R.id.textDebit), findViewById(R.id.editDebit));
	}

	public void makeRowEditable() {
		datePair.setBeingEdited(true);
		referencePair.setBeingEdited(true);
		creditPair.setBeingEdited(true);
		debitPair.setBeingEdited(true);
	}

	public void makeRowNotEditable() {
		datePair.setBeingEdited(false);
		referencePair.setBeingEdited(false);
		creditPair.setBeingEdited(false);
		debitPair.setBeingEdited(false);

		for (int i = 0; i < TEXT_IDS.length; i++) {
			EditText t = findViewById(EDIT_IDS[i]);
			TextView t1 = findViewById(TEXT_IDS[i]);

			t.setOnTouchListener(null);

			t1.setText(t.getText());
			t.setText("");

			t.setVisibility(GONE);
			t1.setVisibility(VISIBLE);
		}
	}

}
