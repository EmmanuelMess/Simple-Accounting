package com.emmanuelmess.simpleaccounting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView
import com.emmanuelmess.simpleaccounting.utils.SimpleBalanceFormatter
import kotlinx.android.synthetic.main.content_main.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal


@RunWith(AndroidJUnit4::class)
@LargeTest
class TableAndRowEditingTest {

	@get:Rule
	val activityRule = ActivityTestRule(MainActivity::class.java)

	@Before
	fun setup() {
		setShowTutorial(false)
	}

	@Test
	fun testFab() {
		for (i in 1..30) {
			onView(withId(R.id.fab)).perform(click())
			onView(withText("DONE")).perform(click())
		}
	}

	@Test
	fun testEditRow() {
		val creditText = "300"
		val debitText = "500"

		createNewRow(creditText, debitText)

		val ledgerLastRow = lastRowOf(withId(R.id.table))

		onView(withId(R.id.textCredit).andParent(ledgerLastRow)).check(matches(withText(creditText)))
		onView(withId(R.id.textDebit).andParent(ledgerLastRow)).check(matches(withText(debitText)))

		onView(ledgerLastRow).perform(longClick())

		onView(withId(R.id.editCredit)).check(matches(withText(creditText)))
		onView(withId(R.id.editDebit)).check(matches(withText(debitText)))
	}

	@Test
	fun testArithmetic() {
		val zero = BigDecimal.ZERO.setScale(1) //0.0
		val toTest = listOf("300" to "300", "100" to "200", "500.1" to "300", "-52" to "52.000001")
		val lastRow = activityRule.activity.table.lastRow as? LedgerRow

		var result = if(lastRow?.model?.balance != null) BigDecimal(lastRow.model.balance) else zero

		for ((credit, debit) in toTest) {
			onView(withId(R.id.fab)).perform(click())

			val lastBalance = onView(withId(R.id.textBalance).andParent(lastRowOf(withId(R.id.table))))

			onView(withId(R.id.textBalance).andParent(withId(R.id.row)))
				.eitherOf(hasSameTextAs(lastBalance), matches(withText("$ 0")))

			val creditBigDecimal = BigDecimal(credit)
			val debitBigDecimal = BigDecimal(debit)

			val newResult = result + creditBigDecimal - debitBigDecimal

			onView(withId(R.id.editCredit)).perform(typeText(credit))
			onView(withId(R.id.editDebit)).perform(typeText(debit))

			val textBalanceEditing = onView(withId(R.id.textBalance).andParent(withId(R.id.row)))
			if(newResult != BigDecimal.ZERO) {
				textBalanceEditing.check(matches(withText(SimpleBalanceFormatter.format(newResult))))
			} else {
				textBalanceEditing
					.eitherOf(matches(withText("$ 0")), matches(withText("$ 0.0")))
			}

			result = newResult

			onView(withText("DONE")).perform(click())
		}
	}


	protected fun createNewRow(credit: String, debit: String) {
		onView(withId(R.id.fab)).perform(click())

		onView(withId(R.id.editCredit)).perform(typeText(credit))
		onView(withId(R.id.editDebit)).perform(typeText(debit))

		onView(withText("DONE")).perform(click())
	}

	private fun setShowTutorial(show: Boolean) {
		getPrefs().edit().putBoolean(MainActivity.PREFS_FIRST_RUN, show).commit()
	}
}