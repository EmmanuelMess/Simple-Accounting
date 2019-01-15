package com.emmanuelmess.simpleaccounting.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.patreon.PatreonController
import com.google.android.material.snackbar.Snackbar

class DonateActivity : AppCompatActivity() {

	companion object {
		private const val BITCOIN_DIRECTION = "1HFhPxH9bqMKvs44nHqXjEEPC2m7z1V8tW"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_donate)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	fun onClickDonate(view: View) {
		PatreonController.openPage(this)
	}

	fun onClickBitcoin(view: View) {
		try {
			with(Intent(Intent.ACTION_VIEW)) {
				data = Uri.parse("bitcoin:$BITCOIN_DIRECTION?amount=0.0005")
				startActivity(this@with)
			}
		} catch (e: ActivityNotFoundException) {
			Snackbar.make(view, R.string.no_bitcoin_app, Snackbar.LENGTH_LONG).show()
		}
	}

	fun onClickPaypal(view: View) {
		Intent(Intent.ACTION_VIEW).let {
			it.data = Uri.parse("https://www.paypal.com/invoice/p/#NNT9XYPXLB69BG3A")
			startActivity(it)
		}
	}

	fun onClickKofi(view: View) {
		Intent(Intent.ACTION_VIEW).let {
			it.data = Uri.parse("https://ko-fi.com/emmanuelmess")
			startActivity(it)
		}
	}

}