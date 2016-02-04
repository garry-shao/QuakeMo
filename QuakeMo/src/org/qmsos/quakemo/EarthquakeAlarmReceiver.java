package org.qmsos.quakemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Execute update service whenever specified intent received.
 *
 *
 */
public class EarthquakeAlarmReceiver extends BroadcastReceiver {
	
	/**
	 * action flag in intent, being used to set up alarm.
	 */
	public static final String ACTION_REFRESH_ALARM = "org.qmsos.quakemo.ACTION_REFRESH_ALARM";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action != null && action.equals(ACTION_REFRESH_ALARM)) {
			Intent refreshIntent = new Intent(context, EarthquakeService.class);
			refreshIntent.setAction(EarthquakeService.ACTION_REFRESH_AUTO);
			
			context.startService(refreshIntent);
		}
	}

}
