package com.emmanuelmess.simpleaccounting.fragments.superclasses

import android.support.annotation.CallSuper
import android.support.v4.app.Fragment

abstract class OnBackPressableFragment: KeyboardListenableFragment() {
	lateinit var onDetachCallback: OnBackPressableFragment.() -> Unit

	abstract fun onBackPressed(): Boolean

	@CallSuper
	override fun onDetach() {
		super.onDetach()
		onDetachCallback()
	}
}