package com.emmanuelmess.simpleaccounting.activities.views

import android.content.Context
import androidx.annotation.StringRes
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView

import com.emmanuelmess.simpleaccounting.R

import java.math.BigDecimal

import com.emmanuelmess.simpleaccounting.activities.MainActivity.EDIT_IDS
import com.emmanuelmess.simpleaccounting.activities.MainActivity.TEXT_IDS

class LedgerRow(context: Context, attrs: AttributeSet) : TableRow(context, attrs) {

    lateinit var formatter: LedgerView.BalanceFormatter

    private lateinit var datePair: EditableViewPair<TextView, TextView, EditText>
    private lateinit var referencePair: EditableViewPair<TextView, TextView, EditText>
    private lateinit var creditPair: EditableViewPair<TextView, TextView, EditText>
    private lateinit var debitPair: EditableViewPair<TextView, TextView, EditText>

    private lateinit var balanceText: TextView

    val date: CharSequence
        get() = datePair.get().text

    val creditText: CharSequence
        get() = creditPair.get().text

    val debitText: CharSequence
        get() = debitPair.get().text

    override fun onFinishInflate() {
        super.onFinishInflate()

        datePair = EditableViewPair(findViewById(R.id.textDate), findViewById(R.id.editDate))
        referencePair = EditableViewPair(findViewById(R.id.textRef), findViewById(R.id.editRef))
        creditPair = EditableViewPair(findViewById(R.id.textCredit), findViewById(R.id.editCredit))
        debitPair = EditableViewPair(findViewById(R.id.textDebit), findViewById(R.id.editDebit))

        balanceText = findViewById(R.id.textBalance)
    }

    fun setDate(date: String) {
        datePair.get().text = date
    }

    fun setReference(@StringRes ref: Int) {
        referencePair.get().setText(ref)
    }

    fun setReference(ref: String) {
        referencePair.get().text = ref
    }

    fun setCredit(credit: String) {
        creditPair.get().text = credit
    }

    fun setDebit(debit: String) {
        debitPair.get().text = debit
    }

    fun setCredit(credit: BigDecimal) {
        if (creditPair.isBeingEdited) {
            throw IllegalStateException("setCredit(BigDecimal) CANNOT be used while the row is editable!")
        }

        creditPair.get().text = credit.toPlainString()
    }

    fun setDebit(debit: BigDecimal) {
        if (debitPair.isBeingEdited) {
            throw IllegalStateException("setDebit(BigDecimal) CANNOT be used while the row is editable!")
        }

        debitPair.get().text = debit.toPlainString()
    }

    fun setBalance(balance: BigDecimal) {
        balanceText.text = formatter.format(balance)
    }

    fun getBalanceText(): CharSequence {
        return balanceText.text
    }

    fun invertDebitCredit() {
        findViewById<View>(R.id.textCredit).setId(0)
        findViewById<View>(R.id.textDebit).setId(R.id.textCredit)
        findViewById<View>(0).setId(R.id.textDebit)

        findViewById<View>(R.id.editCredit).setId(0)
        findViewById<View>(R.id.editDebit).setId(R.id.editCredit)
        findViewById<View>(0).setId(R.id.editDebit)

        findViewById<EditText>(R.id.editCredit).setHint(R.string.credit)
        findViewById<EditText>(R.id.editDebit).setHint(R.string.debit)

        creditPair = EditableViewPair(findViewById(R.id.textCredit), findViewById(R.id.editCredit))
        debitPair = EditableViewPair(findViewById(R.id.textDebit), findViewById(R.id.editDebit))
    }

    fun makeRowEditable() {
        datePair.isBeingEdited = true
        referencePair.isBeingEdited = true
        creditPair.isBeingEdited = true
        debitPair.isBeingEdited = true
    }

    fun makeRowNotEditable() {
        datePair.isBeingEdited = false
        referencePair.isBeingEdited = false
        creditPair.isBeingEdited = false
        debitPair.isBeingEdited = false

        for (i in TEXT_IDS.indices) {
            val t = findViewById<EditText>(EDIT_IDS[i])
            val t1 = findViewById<TextView>(TEXT_IDS[i])

            t.setOnTouchListener(null)

            t1.text = t.text
            t.setText("")

            t.visibility = View.GONE
            t1.visibility = View.VISIBLE
        }
    }

}
