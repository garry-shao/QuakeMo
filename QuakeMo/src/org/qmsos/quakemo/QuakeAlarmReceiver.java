package org.qmsos.quakemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Execute update service whenever specified intent received.
 *
 *
 */
public class QuakeAlarmReceiver extends BroadcastReceiver {
	
	/**
	 * action flag in intent, being used to set up alarm.
	 */
	public static final String ACTION_REFRESH_ALARM = "org.qmsos.quakemo.ACTION_REFRESH_ALARM";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startIntent = new Intent(context, QuakeUpdateService.class);
		startIntent.setAction(QuakeUpdateService.ACTION_REFRESH_AUTO);
		
		context.startService(startIntent);
	}

}
