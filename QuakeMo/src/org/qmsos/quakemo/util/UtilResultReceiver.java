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
	
	/**
	 * The result callback interface.
	 * 
	 * 
	 */
	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	private Receiver receiver;

	public UtilResultReceiver(Handler handler) {
		super(handler);
	}

	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (receiver != null) {
			receiver.onReceiveResult(resultCode, resultData);
		}
	}

}
