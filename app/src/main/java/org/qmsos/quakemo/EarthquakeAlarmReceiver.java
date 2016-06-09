package org.qmsos.quakemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.qmsos.quakemo.contract.IntentContract;

/**
 * Execute update service whenever specified intent received.
 */
public class EarthquakeAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null && action.equals(IntentContract.ACTION_REFRESH_ALARM)) {
			Intent refreshIntent = new Intent(context, EarthquakeService.class);
			refreshIntent.setAction(IntentContract.ACTION_REFRESH_AUTO);
			refreshIntent.putExtra(IntentContract.EXTRA_REFRESH_AUTO, true);
			
			context.startService(refreshIntent);
		}
	}

}
