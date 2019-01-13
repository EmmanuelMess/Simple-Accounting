package com.emmanuelmess.simpleaccounting

import android.view.ViewTreeObserver

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.View
import com.emmanuelmess.simpleaccounting.constants.SettingsConstants.INVERT_CREDIT_DEBIT_SETTING
import com.emmanuelmess.simpleaccounting.utils.addSingleUseOnGlobalLayoutListener
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThan

@RunWith(RobolectricTestRunner::class)
class MainActivitySwitchCreditDebitTest : MainActivityTest() {
	override fun startSetUp() {
		super.startSetUp()

		getDefaultSharedPreferences(context).edit().putBoolean(INVERT_CREDIT_DEBIT_SETTING, true).apply()
	}

	@Test
	fun testSwitchCreditDebitTest() {
		table.addSingleUseOnGlobalLayoutListener {
			assertThat(table.findViewById<View>(R.id.debit).x, lessThan<Float>(table.findViewById<View>(R.id.credit).x))

			table.setInvertCreditAndDebit(false)

			assertThat(table.findViewById<View>(R.id.credit).x, lessThan<Float>(table.findViewById<View>(R.id.debit).x))
		}
	}
}
