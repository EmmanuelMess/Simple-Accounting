package com.emmanuelmess.simpleaccounting.utils

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.Editable
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.emmanuelmess.simpleaccounting.R

val View.yInScreen: Int
	get() {
		var list = IntArray(2)
		getLocationOnScreen(list)
		return list[1]
	}

var TextView.textDisplay: CharSequence
	get() = text
	set(value) {
		text = Editable.Factory.getInstance().newEditable(value)
	}

fun EditText.openKeyboard() {
	requestFocus()

	val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
	imm!!.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

typealias KeyboardEventListener = () -> Unit

fun View.addKeyboardEventListener(
	listener: (Boolean) -> Unit
): ()->Unit {

	val defaultKeyboardDP = 100

	// From @nathanielwolf answer...  Lollipop includes button bar in the root. Add height of button bar (48dp) to maxDiff
	val estimatedKeyboardDP = defaultKeyboardDP + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 48 else 0

	val rect = Rect()

	var wasOpened = false

	val callback = {
		val estimatedKeyboardHeight = estimatedKeyboardDP.dp.toPx().toInt()

		rootView.getWindowVisibleDisplayFrame(rect)
		val heightDiff = rootView.rootView.height - (rect.bottom - rect.top)
		val isShown = heightDiff >= estimatedKeyboardHeight

		if (isShown != wasOpened) {
			wasOpened = isShown

			listener(isShown)
		}
	}

	viewTreeObserver.addOnGlobalLayoutListener(callback)

	return callback
}

fun View.removeOnKeyboadHiddenListener(callback: ()->Unit) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		viewTreeObserver.removeOnGlobalLayoutListener(callback)
	} else {
		viewTreeObserver.removeGlobalOnLayoutListener(callback)
	}
}

var View.layoutGravity: Int
	get() {
		val params = layoutParams as FrameLayout.LayoutParams
		return params.gravity
	}
	set(value) {
		val params = layoutParams as FrameLayout.LayoutParams
		params.gravity = value
		layoutParams = params
	}

fun View.removeBackground() {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
		setBackgroundDrawable(null)
	} else {
		background = null
	}
}

inline fun View.addSingleUseOnGlobalLayoutListener(crossinline callback: () -> Unit) {
	viewTreeObserver.addOnGlobalLayoutListener(
		object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					viewTreeObserver.removeOnGlobalLayoutListener(this)
				} else {
					viewTreeObserver.removeGlobalOnLayoutListener(this)
				}

				callback()
			}
		})
}