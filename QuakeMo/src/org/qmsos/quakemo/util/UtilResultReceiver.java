package org.qmsos.quakemo.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Abstracting an interface, so those who implements the inner interface inside
 * this class can receive a callback result from someone.
 * 
 *
 */
public class UtilResultReceiver extends ResultReceiver {

	/**
	 * Feel free to use, as key of parceled ResultReceiver.
	 */
	public static final String RECEIVER = "org.qmsos.quakemo.RECEIVER";
	
	private OnReceiveListener listener;

	public UtilResultReceiver(Handler handler) {
		super(handler);
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (listener != null) {
			listener.onReceiveResult(resultCode, resultData);
		}
	}

	public void setListener(OnReceiveListener listener) {
		this.listener = listener;
	}

	/**
	 * Callback interface when results are delivered. 
	 * 
	 * 
	 * 
	 */
	public interface OnReceiveListener {
		void onReceiveResult(int resultCode, Bundle resultData);
	}

}
