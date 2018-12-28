package com.emmanuelmess.simpleaccounting.activities.superclases

import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.emmanuelmess.simpleaccounting.fragments.superclasses.OnBackPressableFragment

open class FragmentCanGoBackActivity : AppCompatActivity() {

	private val backStack = mutableListOf<OnBackPressableFragment>()

	@CallSuper
	override fun onAttachFragment(fragment: Fragment) {
		if(fragment is OnBackPressableFragment) {
			fragment.onDetachCallback = ::onDetachFragment
			backStack.add(0, fragment)
		}
	}

	@CallSuper
	override fun onBackPressed() {
		for(i in 0 until backStack.size) {
			val consumed = backStack[i].onBackPressed()
			if(consumed) {
				return
			}
		}
	}

	private fun onDetachFragment(fragment: OnBackPressableFragment) {
		backStack.remove(fragment)
	}

}