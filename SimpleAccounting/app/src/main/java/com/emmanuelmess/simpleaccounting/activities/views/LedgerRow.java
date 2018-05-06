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

	private TextView dateText, referenceText, creditText, debitText, balanceText;
	private EditText dateEditText, referenceEditText, creditEditText, debitEditText;

	public LedgerRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		dateText = findViewById(R.id.textDate);
		referenceText = findViewById(R.id.textRef);
		creditText = findViewById(R.id.textCredit);
		debitText = findViewById(R.id.textDebit);
		balanceText = findViewById(R.id.textBalance);

		dateEditText = findViewById(R.id.editDate);
		referenceEditText = findViewById(R.id.editRef);
		creditEditText = findViewById(R.id.editCredit);
		debitEditText = findViewById(R.id.editDebit);
	}

	public void setDate(String date) {
		setText(dateText, dateEditText, date);
	}

	public void setReference(@StringRes int ref) {
		setText(referenceText, referenceEditText, ref);
	}

	public void setReference(String ref) {
		setText(referenceText, referenceEditText, ref);
	}

	public void setCredit(String credit) {
		setText(creditText, creditEditText, credit);
	}

	public CharSequence getCreditText() {
		return creditText.getText();
	}

	public void setDebit(String debit) {
		setText(debitText, debitEditText, debit);
	}

	public CharSequence getDebitText() {
		return debitText.getText();
	}

	public void setCredit(BigDecimal credit) {
		if(creditText.getVisibility() != VISIBLE) {
			throw new IllegalStateException("setCredit(BigDecimal) CANNOT be used while the row is editable!");
		}
		creditText.setText(credit.toPlainString());
	}

	public void setDebit(BigDecimal debit) {
		if(creditText.getVisibility() != VISIBLE) {
			throw new IllegalStateException("setDebit(BigDecimal) CANNOT be used while the row is editable!");
		}

		debitText.setText(debit.toPlainString());
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

		creditText = findViewById(R.id.textCredit);
		debitText = findViewById(R.id.textDebit);
		creditEditText = findViewById(R.id.editCredit);
		debitEditText = findViewById(R.id.editDebit);
	}

	public void makeRowNotEditable() {
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

	private void setText(TextView a, TextView b, CharSequence s) {
		if(a.getVisibility() != VISIBLE) {
			b.setText(s);
		} else {
			a.setText(s);
		}
	}

	private void setText(TextView a, TextView b, @StringRes int s) {
		if(a.getVisibility() != VISIBLE) {
			b.setText(s);
		} else {
			a.setText(s);
		}
	}

}
