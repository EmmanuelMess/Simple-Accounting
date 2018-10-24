package com.emmanuelmess.simpleaccounting.activities.controllers

import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRowContextualActionBar
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRowContextualActionBarListener
import com.emmanuelmess.simpleaccounting.fragments.EditLedgerRowFragment

class LedgerRowEditingController(
	private val mainActivity: MainActivity
) : LedgerRowContextualActionBarListener {

	private val ledgerRowContextualActionBar = LedgerRowContextualActionBar(this)
	private var editLedgerRowFragment: EditLedgerRowFragment? = null

	fun startEditing() = with(mainActivity) {
		startSupportActionMode(ledgerRowContextualActionBar)

		editLedgerRowFragment = EditLedgerRowFragment.newInstance()
		mainActivity.startEditingRow(editLedgerRowFragment)
	}

	fun stopEditing() = with(mainActivity) {
		ledgerRowContextualActionBar.actionMode?.finish()
		stopEditingRow(editLedgerRowFragment)

	}

	override fun onCloseActionMenu() = with(mainActivity) {
		stopEditing()
	}

}