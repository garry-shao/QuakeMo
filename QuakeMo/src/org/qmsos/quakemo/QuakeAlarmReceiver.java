package org.qmsos.quakemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * Execute update service whenever specified intent received.
 *
 */
public class QuakeAlarmReceiver extends BroadcastReceiver {
	
	public static final String ACTION_REFRESH_EARTHQUAKE_ALARM = 
			"org.qmsos.quakemo.ACTION_REFRESH_EARTHQUAKE_ALARM";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startIntent = new Intent(context, QuakeUpdateService.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startService(startIntent);
	}

}
