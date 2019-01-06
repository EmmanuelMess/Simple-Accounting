package com.emmanuelmess.simpleaccounting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.utils.SimpleBalanceFormatter
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class MainTest {

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