package com.emmanuelmess.simpleaccounting.activities.views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.utils.removeBackground
import com.emmanuelmess.simpleaccounting.utils.textDisplay
import kotlinx.android.parcel.Parcelize

class LedgerRow(context: Context, attrs: AttributeSet) : TableRow(context, attrs) {

    lateinit var formatter: LedgerView.BalanceFormatter
    var model: LedgerRowModel = LedgerRowModel("", "", "", "", "")
        set(value) {
            if(value.date != model.date) {
                datePair.textDisplay = value.date
            }
            if(value.reference != model.reference) {
                referencePair.textDisplay = value.reference
            }
            if(value.credit != model.credit) {
                creditPair.textDisplay = value.credit
            }
            if(value.debit != model.debit) {
                debitPair.textDisplay = value.debit
            }
            if(value.balance != model.balance) {
                balanceText.textDisplay = value.balance
            }

            field = value
        }

    private lateinit var datePair: TextView
    private lateinit var referencePair: TextView
    private lateinit var creditPair: TextView
    private lateinit var debitPair: TextView

    private lateinit var balanceText: TextView

    override fun onFinishInflate() {
        super.onFinishInflate()

        datePair = findViewById(R.id.textDate)
        referencePair = findViewById(R.id.textRef)
        creditPair = findViewById(R.id.textCredit)
        debitPair = findViewById(R.id.textDebit)


        balanceText = findViewById(R.id.textBalance)
    }

    fun setCredit(credit: String) {
        model = model.copy(credit = credit)
    }

    fun setDebit(debit: String) {
        model = model.copy(debit = debit)
    }

    fun setBalance(balance: String) {
        model = model.copy(balance = balance)
    }

    fun getBalanceText(): CharSequence {
        return balanceText.text
    }

    fun invertDebitCredit() {
        findViewById<View>(R.id.textCredit).setId(0)
        findViewById<View>(R.id.textDebit).setId(R.id.textCredit)
        findViewById<View>(0).setId(R.id.textDebit)

        creditPair = findViewById(R.id.textCredit)
        debitPair = findViewById(R.id.textDebit)
    }

    fun removeSelectableItemBackground() {
        removeBackground()
    }

    @Parcelize
    data class LedgerRowModel(
        val date: String = "",
        val reference: String = "",
        val credit: String = "",
        val debit: String = "",
        val balance: String = ""
    ) : Parcelable

}
