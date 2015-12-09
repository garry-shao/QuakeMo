package org.qmsos.quakemo;

import java.lang.reflect.Method;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.util.AttributeSet;

/**
 * Material theme liked ListPreference , using AlertDialog in AppCompat library.
 * 
 *
 */
public class UtilListPreference extends ListPreference {

	private Context context;
	private AlertDialog.Builder builder;
	private AppCompatDialog dialog;
	

	public UtilListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
	}

	public UtilListPreference(Context context) {
		super(context);

		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }
		
		int preselect = findIndexOfValue(getValue());
		builder = new AlertDialog.Builder(context);
		builder.setTitle(getTitle());
		builder.setIcon(getDialogIcon());
		builder.setNegativeButton(R.string.dialog_negative_button, this);
		builder.setSingleChoiceItems(getEntries(), preselect, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which >= 0 && getEntryValues() != null ) {
					String value = getEntryValues()[which].toString();
					if (callChangeListener(value)) {
						setValue(value);
					}
				}
				
				dialog.dismiss();
			}
		});
		
		
		// Workaround, 
		// (un)registerOnActivityDestroyListener method 
		// has package scope originally.
		PreferenceManager manager = getPreferenceManager();
		try {
			Method method = manager.getClass().getDeclaredMethod(
					"registerOnActivityDestroyListener",
					PreferenceManager.OnActivityDestroyListener.class);
			method.setAccessible(true);
			method.invoke(manager, this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		final Dialog fDialog = dialog = builder.create();
		if (state != null) {
			fDialog.onRestoreInstanceState(state);
		}
		
		fDialog.show();
	}

	@Override
	public void onActivityDestroy() {
		if (dialog == null || !dialog.isShowing()) {
			return;
		}
		dialog.dismiss();

		super.onActivityDestroy();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (dialog == null || !dialog.isShowing()) {
			return superState;
		}
		
		final SavedState myState = new SavedState(superState);
		myState.value = getValue();
		myState.isDialogShowing = true;
		myState.dialogBundle = dialog.onSaveInstanceState();

		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			super.onRestoreInstanceState(state);

			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		
		setValue(myState.value);
		if (myState.isDialogShowing) {
			showDialog(myState.dialogBundle);
		}
	}

	static class SavedState extends BaseSavedState {
		String value;
		boolean isDialogShowing;
		Bundle dialogBundle;

		public SavedState(Parcel source) {
			super(source);

			value = source.readString();
			isDialogShowing = source.readInt() == 1;
			dialogBundle = source.readBundle();
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);

			dest.writeString(value);
			dest.writeInt(isDialogShowing ? 1 : 0);
			dest.writeBundle(dialogBundle);
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
