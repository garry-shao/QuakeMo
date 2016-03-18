package org.qmsos.quakemo.preference;

import org.qmsos.quakemo.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;

/**
 * Customized SwitchPreferenceCompat that adds a confirmation dialog letting 
 * user perform an additional check before change this preference's value.
 * 
 *
 */
public class SwitchPreferenceCustom extends SwitchPreferenceCompat {

	private AppCompatDialog mDialog;

	private String mTitle;
	private String mMessage;
	private String mButtonNegative;
	private String mButtonPositive;

	public SwitchPreferenceCustom(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		final Resources res = context.getResources();
		final String defaultTitle = 
				res.getString(R.string.default_switch_preference_title);
		final String defaultMessage = 
				res.getString(R.string.default_switch_preference_message);
		final String defaultNegativeButton = 
				res.getString(R.string.default_switch_preference_negative_button);
		final String defaultPositiveButton = 
				res.getString(R.string.default_switch_preference_positive_button);
		
		TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.SwitchPreferenceCustom, defStyleAttr, 0);
		
		String attributeTitle = 
				a.getString(R.styleable.SwitchPreferenceCustom_switchPreferenceDialogTitle);
		String attributeMessage = 
				a.getString(R.styleable.SwitchPreferenceCustom_switchPreferenceDialogMessage);
		String attributeNevativeButton = 
				a.getString(R.styleable.SwitchPreferenceCustom_switchPreferenceNegativeButton);
		String attributePositiveButton = 
				a.getString(R.styleable.SwitchPreferenceCustom_switchPreferencePositiveButton);
		
		mTitle = 
				(attributeTitle != null) ? attributeTitle : defaultTitle;
		mMessage = 
				(attributeMessage != null) ? attributeMessage : defaultMessage;
		mButtonNegative = 
				(attributeNevativeButton != null) ? attributeNevativeButton : defaultNegativeButton;
		mButtonPositive = 
				(attributePositiveButton != null) ? attributePositiveButton : defaultPositiveButton;
		
		a.recycle();
	}

	public SwitchPreferenceCustom(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.switchPreferenceCompatStyle);
	}

	public SwitchPreferenceCustom(Context context) {
		this(context, null);
	}

	@Override
	protected void onClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(mTitle);
		builder.setMessage(mMessage);
		builder.setNegativeButton(mButtonNegative, null);
		builder.setPositiveButton(mButtonPositive, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SwitchPreferenceCustom.super.onClick();
				
				dialog.dismiss();
			}
		});
		
		mDialog = builder.create();
		if (isChecked()) {
			super.onClick();
		} else {
			mDialog.show();
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		
		if (savedState.mIsDialogShowing) {
			onClick();
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		if (mDialog == null || !mDialog.isShowing()) {
			return superState;
		}
		
		SavedState savedState = new SavedState(superState);
		savedState.mIsDialogShowing = true;
		savedState.mDialogBundle = mDialog.onSaveInstanceState();
		
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		boolean mIsDialogShowing;
		Bundle mDialogBundle;
		
		public SavedState(Parcelable superState) {
			super(superState);
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(mIsDialogShowing ? 1 : 0);
			dest.writeBundle(mDialogBundle);
			
			super.writeToParcel(dest, flags);
		}
		
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		private SavedState(Parcel source) {
			super(source);
			
			mIsDialogShowing = source.readInt() == 1;
			mDialogBundle = source.readBundle();
		}
		
	}

}
