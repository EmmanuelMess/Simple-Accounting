package com.emmanuelmess.simpleaccounting.activities.controllers

import android.arch.lifecycle.LifecycleObserver
import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRowContextualActionBar
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRowContextualActionBarListener

class LedgerRowContextualActionBarController(
	private val mainActivity: MainActivity
) : LedgerRowContextualActionBarListener {

	private val ledgerRowContextualActionBar = LedgerRowContextualActionBar(this)

	fun startEditing() = with(mainActivity) {
		startSupportActionMode(ledgerRowContextualActionBar)
	}

	fun stopEditing() = with(mainActivity) {
		ledgerRowContextualActionBar.actionMode?.finish()
	}

	override fun onCloseActionMenu() = with(mainActivity) {
		stopEditing()
	}

}