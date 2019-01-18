package com.emmanuelmess.simpleaccounting.fragments.superclasses

import androidx.annotation.CallSuper

abstract class OnBackPressableFragment: KeyboardListenableFragment() {
	lateinit var onDetachCallback: OnBackPressableFragment.() -> Unit

	abstract fun onBackPressed(): Boolean

	@CallSuper
	override fun onDetach() {
		super.onDetach()
		onDetachCallback()
	}
}