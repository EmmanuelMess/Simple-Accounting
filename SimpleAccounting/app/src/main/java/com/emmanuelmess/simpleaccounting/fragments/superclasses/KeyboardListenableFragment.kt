package com.emmanuelmess.simpleaccounting.fragments.superclasses

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.emmanuelmess.simpleaccounting.utils.KeyboardEventListener
import com.emmanuelmess.simpleaccounting.utils.addKeyboardEventListener
import com.emmanuelmess.simpleaccounting.utils.removeOnKeyboadHiddenListener

open class KeyboardListenableFragment: Fragment() {

	private /*lateinit*/ var rootView: View? = null
	private var listener: KeyboardEventListener? = null

	@CallSuper
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		rootView = view

		listener = rootView!!.addKeyboardEventListener(::keyboardEventListener)

		super.onViewCreated(rootView!!, savedInstanceState)
	}

	override fun onPause() {
		super.onPause()
		removeListener()
	}

	override fun onDestroy() {
		super.onDestroy()
		removeListener()
	}

	private fun removeListener() {
		listener?.let { listener ->
			rootView!!.removeOnKeyboadHiddenListener(listener)
		}
	}

	private fun keyboardEventListener(isShown: Boolean) {
		if (isShown) onKeyboardOpened()
		else onKeyboardClosed()
	}

	open fun onKeyboardOpened() {}

	open fun onKeyboardClosed() {}
}