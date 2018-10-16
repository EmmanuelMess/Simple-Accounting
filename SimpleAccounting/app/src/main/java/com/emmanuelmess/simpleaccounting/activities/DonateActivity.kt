package com.emmanuelmess.simpleaccounting.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleAdapter

import com.emmanuelmess.simpleaccounting.R
import com.google.android.material.snackbar.Snackbar

import java.util.ArrayList
import java.util.HashMap

class DonateActivity : AppCompatActivity() {

	companion object {
		private const val BITCOIN_DIRECTION = "1HFhPxH9bqMKvs44nHqXjEEPC2m7z1V8tW"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_donate)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		// create a new ListView, set the adapter and item click listener
		findViewById<ListView>(R.id.listView).let {
			val adapterData = listOf(mapOf(
				"title" to getString(R.string.bitcoin),
				"summary" to BITCOIN_DIRECTION
			))

			it.adapter = SimpleAdapter(this, adapterData, R.layout.item_donate,
				arrayOf("title", "summary"), intArrayOf(R.id.title, R.id.summary))

			it.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
				when (position) {
					0 -> {
						try {
							with(Intent(Intent.ACTION_VIEW)) {
								data = Uri.parse("bitcoin:$BITCOIN_DIRECTION?amount=0.0005")
								startActivity(this@with)
							}
						} catch (e: ActivityNotFoundException) {
							Snackbar.make(it, R.string.no_bitcoin_app, Snackbar.LENGTH_LONG).show()
						}
					}
				}
			}
		}
	}

}