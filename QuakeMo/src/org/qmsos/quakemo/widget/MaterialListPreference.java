package org.qmsos.quakemo.widget;

import org.qmsos.quakemo.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

/**
 * A workaround of ListPreference since that in support library has bug on when
 * configuration changed.
 * 
 *
 */
public class MaterialListPreference extends ListPreference {

	private AppCompatDialog mDialog;

	public MaterialListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MaterialListPreference(Context context) {
		super(context);
	}

	@Override
	protected void onClick() {
		if (getEntries() == null || getEntryValues() == null) {
			throw new IllegalStateException(
					"ListPreference requires an entries array and an entryValues array.");
		}

		int preSelect = findIndexOfValue(getValue());

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getTitle());
		builder.setIcon(getDialogIcon());
		builder.setNegativeButton(R.string.dialog_negative_button, null);
		builder.setSingleChoiceItems(getEntries(), preSelect, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which >= 0 && getEntryValues() != null) {
					String value = getEntryValues()[which].toString();
					if (callChangeListener(value)) {
						setValue(value);
					}
				}

				dialog.dismiss();
			}
		});

		final Dialog fDialog = mDialog = builder.create();
		fDialog.show();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			super.onRestoreInstanceState(state);

			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		setValue(myState.mValue);
		if (myState.mIsDialogShowing) {
			onClick();
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (mDialog == null || !mDialog.isShowing()) {
			return superState;
		}

		// Workaround, havn't found way to dismiss properly.
		mDialog.dismiss();

		final SavedState myState = new SavedState(superState);
		myState.mValue = getValue();
		myState.mIsDialogShowing = true;
		myState.mDialogBundle = mDialog.onSaveInstanceState();

		return myState;
	}

	static class SavedState extends BaseSavedState {
		String mValue;
		boolean mIsDialogShowing;
		Bundle mDialogBundle;

		public SavedState(Parcel source) {
			super(source);

			mValue = source.readString();
			mIsDialogShowing = source.readInt() == 1;
			mDialogBundle = source.readBundle();
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);

			dest.writeString(mValue);
			dest.writeInt(mIsDialogShowing ? 1 : 0);
			dest.writeBundle(mDialogBundle);
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
