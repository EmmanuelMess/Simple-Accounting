package com.emmanuelmess.simpleaccounting.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
/**
 * @author Emmanuel
 *         on 1/2/2017, at 14:23.
 */

public class DialogPreferenceWithKeyboard extends DialogPreference {
	private AlertDialog.Builder mBuilder;
	private Dialog mDialog;
	private int mWhichButtonClicked;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public DialogPreferenceWithKeyboard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public DialogPreferenceWithKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public DialogPreferenceWithKeyboard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public DialogPreferenceWithKeyboard(Context context) {
		super(context);
	}

	/**
	 * Gets the dialog that is shown by this preference.
	 *
	 * @return The dialog, or null if a dialog is not being shown.
	 */
	public Dialog getDialog() {
		return mDialog;
	}

	@Override
	protected void onClick() {
		if (getDialog() != null && getDialog().isShowing()) return;

		showDialog(null);
	}

	@Override
	protected void showDialog(Bundle state) {
		Context context = getContext();

		mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE;

		mBuilder = new AlertDialog.Builder(context)
				.setTitle(getTitle())
				.setIcon(getDialogIcon())
				.setPositiveButton(getPositiveButtonText(), this)
				.setNegativeButton(getNegativeButtonText(), this);

		View contentView = onCreateDialogView();
		if (contentView != null) {
			onBindDialogView(contentView);
			mBuilder.setView(contentView);
		} else {
			mBuilder.setMessage(getDialogMessage());
		}

		onPrepareDialogBuilder(mBuilder);

		//getPreferenceManager().registerOnActivityDestroyListener(this); TODO wat

		// Create the dialog
		final Dialog dialog = mDialog = mBuilder.create();
		if (state != null) {
			dialog.onRestoreInstanceState(state);
		}
		if (needInputMethod()) {
			requestInputMethod(dialog);
		}
		dialog.setOnDismissListener(this);
		dialog.show();
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	}

	/**
	 * Returns whether the preference needs to display a soft input method when the dialog
	 * is displayed. Default is false. Subclasses should override this method if they need
	 * the soft input method brought up automatically.
	 * @hide
	 */
	public boolean needInputMethod() {
		return false;
	}

	/**
	 * Sets the required flags on the dialog window to enable input method window to show up.
	 */
	private void requestInputMethod(Dialog dialog) {
		Window window = dialog.getWindow();
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	protected View onCreateDialogView() {
		if (getDialogLayoutResource() == 0) {
			return null;
		}

		LayoutInflater inflater = LayoutInflater.from(mBuilder.getContext());
		return inflater.inflate(getDialogLayoutResource(), null);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (getDialog() == null || !getDialog().isShowing()) {
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.isDialogShowing = true;
		myState.dialogBundle = mDialog.onSaveInstanceState();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		if (myState.isDialogShowing) {
			showDialog(myState.dialogBundle);
		}
	}

	private static class SavedState extends BaseSavedState {
		boolean isDialogShowing;
		Bundle dialogBundle;

		public SavedState(Parcel source) {
			super(source);
			isDialogShowing = source.readInt() == 1;
			dialogBundle = source.readBundle();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(isDialogShowing ? 1 : 0);
			dest.writeBundle(dialogBundle);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}
}
