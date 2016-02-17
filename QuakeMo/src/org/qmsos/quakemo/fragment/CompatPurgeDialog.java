package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Simple Material Design liked Dialog fragment, implements the listener inside
 * to get callback.
 * 
 *
 */
public class CompatPurgeDialog extends DialogFragment {

	private OnPurgeSelectedListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (OnPurgeSelectedListener) activity;
		} catch (ClassCastException e) {
			String listenerName = OnPurgeSelectedListener.class.getSimpleName();
			
			throw new ClassCastException(activity.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_purge_title);
		builder.setMessage(R.string.dialog_purge_message);
		builder.setNegativeButton(R.string.dialog_purge_negative, null);
		builder.setPositiveButton(R.string.dialog_purge_positive, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onPurgeSelected();
			}
		});

		return builder.create();
	}

	/**
	 * Interface for a callback to be invoked when purging action is called.
	 * 
	 *
	 */
	public interface OnPurgeSelectedListener {
		/**
		 * Called when the purge action is executed.
		 */
		void onPurgeSelected();
	}

}