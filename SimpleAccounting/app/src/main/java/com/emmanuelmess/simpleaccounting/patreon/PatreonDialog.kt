package com.emmanuelmess.simpleaccounting.patreon

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.emmanuelmess.simpleaccounting.R

fun Context.createPatreonDialog(): AlertDialog = with(AlertDialog.Builder(this)) {
	setIcon(R.drawable.ic_patreon)
	setTitle(R.string.patreon_dialog_title)
	setMessage(R.string.patreon_dialog_text)

	setNegativeButton("NOPE") { alertDialog, _ ->
		alertDialog.dismiss()
	}
	setPositiveButton("OK!") { _, _ ->
		PatreonController.Metrics.okWasClicked()

		PatreonController.openPage(context)
	}
	return create()
}