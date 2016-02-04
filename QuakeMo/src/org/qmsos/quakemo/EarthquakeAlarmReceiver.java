package org.qmsos.quakemo;

import org.qmsos.quakemo.util.IpcConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Execute update service whenever specified intent received.
 *
 *
 */
public class EarthquakeAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action != null && action.equals(IpcConstants.ACTION_REFRESH_ALARM)) {
			Intent refreshIntent = new Intent(context, EarthquakeService.class);
			refreshIntent.setAction(IpcConstants.ACTION_REFRESH_AUTO);
			
			context.startService(refreshIntent);
		}
	}

}
