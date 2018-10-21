package com.emmanuelmess.simpleaccounting.patreon

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.support.v7.app.AlertDialog
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