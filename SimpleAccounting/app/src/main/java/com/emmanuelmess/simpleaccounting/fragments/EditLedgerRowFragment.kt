package com.emmanuelmess.simpleaccounting.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emmanuelmess.simpleaccounting.R

class EditLedgerRowFragment : Fragment() {
	companion object {
		@JvmStatic
		fun newInstance() =
			EditLedgerRowFragment().apply {
				arguments = Bundle().apply {

				}
			}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {

		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_edit_ledger_row, container, false)
	}
}
