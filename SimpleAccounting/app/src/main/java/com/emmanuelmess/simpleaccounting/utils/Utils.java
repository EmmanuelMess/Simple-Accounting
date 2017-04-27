package com.emmanuelmess.simpleaccounting.utils;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Emmanuel
 *         on 2/12/2016, at 16:45.
 */

public class Utils {

	public static BigDecimal parseView(TextView v) {
		return parseString(parseViewToString(v));
	}

	public static String parseViewToString(TextView v) {
		return v.getText().toString();
	}

	public static BigDecimal parseString(String s) {
		if(s.length() == 0 || equal(s, "."))
			return new BigDecimal("0");
		else return new BigDecimal(s);
	}

	public static boolean equal(Object o1, Object o2) {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Objects.equals(o1, o2)) || o1.equals(o2);
	}

	public static class SimpleTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void afterTextChanged(Editable editable) {}
	}

	public static int getBackgroundColor(Drawable drawable, @ColorRes int defaultColor) {
		if(Build.VERSION.SDK_INT >= 21)
			return defaultColor;//Other method will fail

		if (Build.VERSION.SDK_INT >= 11 && drawable instanceof ColorDrawable)
			return ((ColorDrawable) drawable).getColor();

		try {
			Field field = drawable.getClass().getDeclaredField("mState");
			field.setAccessible(true);
			Object object = field.get(drawable);
			field = object.getClass().getDeclaredField("mUseColor");
			field.setAccessible(true);
			return field.getInt(object);
		} catch (Exception e) {
			return defaultColor;
		}
	}

}
