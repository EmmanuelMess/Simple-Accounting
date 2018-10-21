package com.emmanuelmess.simpleaccounting.activities.views

import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.emmanuelmess.simpleaccounting.R

class LedgerRowContextualActionBar(
	private val listener: LedgerRowContextualActionBarListener
): ActionMode.Callback {

	var actionMode: ActionMode? = null
		private set

	override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
		actionMode = mode

		val inflater: MenuInflater = mode.menuInflater
		inflater.inflate(R.menu.contextual_menu_ledger_item, menu)
		return true
	}

	override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
		when (item.itemId) {
			R.id.deleteItem -> {

				mode.finish()
				true
			}
			else -> false
		}

	override fun onDestroyActionMode(mode: ActionMode) {
		listener.onCloseActionMenu()
		actionMode = null
	}
}

interface LedgerRowContextualActionBarListener {
	fun onCloseActionMenu()
}