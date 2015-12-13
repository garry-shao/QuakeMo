package org.qmsos.quakemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.quakemo.data.Earthquake;
import org.qmsos.quakemo.util.UtilResultReceiver;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * The service performing earthquake update.
 *
 */
public class QuakeUpdateService extends IntentService {

	/**
	 * The intent action of refreshing widget.
	 */
	public static final String ACTION_REFRESH_WIDGET = "org.qmsos.quakemo.ACTION_REFRESH_WIDGET";
	
	/**
	 * The intent action of refreshing manually.
	 */
	public static final String ACTION_REFRESH_MANUAL = "org.qmsos.quakemo.ACTION_REFRESH_MANUAL";

	/**
	 * The intent action of automatic refreshing.
	 */
	public static final String ACTION_REFRESH_AUTO = "org.qmsos.quakemo.ACTION_REFRESH_AUTO";
	
	/**
	 * The intent action of purge database.
	 */
	public static final String ACTION_PURGE_DATABASE = "org.qmsos.quakemo.ACTION_PURGE_DATABASE";

	// Used in undo purge database feature.
	public static final String EXTRA_PURGE_BURNDOWN = "org.qmsos.quakemo.EXTRA_PURGE_BURNDOWN";
	public static final String EXTRA_PURGE_BURNDOWN_YES = "YES";
	public static final String EXTRA_PURGE_BURNDOWN_NO = "NO";
	
	// Result code passed in ResultReceiver.
	public static final int RESULT_CODE_REFRESHED = 1;
	public static final int RESULT_CODE_PURGED = 2;
	public static final int RESULT_CODE_CANCELED = 3;
	
	/**
	 * Class name tag. Debug use only.
	 */
	private static final String TAG = QuakeUpdateService.class.getSimpleName();

	/**
	 * Notification ID in this application.
	 */
	private static final int NOTIFICATION_ID = 1;
	
	private static final long ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;

	/**
	 * Default constructor of this service.
	 */
	public QuakeUpdateService() {
		super(TAG);
	}

	/**
	 * Constructor with a debug-purpose name of this service.
	 * 
	 * @param workThreadName
	 *            Tag name for debug.
	 */
	public QuakeUpdateService(String workThreadName) {
		super(workThreadName);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean autoToggle = prefs.getBoolean(getString(R.string.PREF_AUTO_TOGGLE), false);
		int autoFrequency = Integer.parseInt(
				prefs.getString(getString(R.string.PREF_AUTO_FREQUENCY), "12"));

		String action = intent.getAction();
		if (action != null) {
			if (action.equals(ACTION_REFRESH_AUTO)) {
				if (autoToggle) {
					setupAutoUpdate(autoFrequency);

					queryQuakes();
				} else {
					cancelAutoUpdate();
				}
			} else if (action.equals(ACTION_REFRESH_MANUAL)) {
				queryQuakes();

				ResultReceiver receiver = intent.getParcelableExtra(UtilResultReceiver.RECEIVER);
				if (receiver != null) {
					receiver.send(RESULT_CODE_REFRESHED, new Bundle());
				}
			} else if (action.equals(ACTION_PURGE_DATABASE)) {
				ResultReceiver receiver = intent.getParcelableExtra(UtilResultReceiver.RECEIVER);
				if (receiver != null) {
					String burndown = intent.getStringExtra(EXTRA_PURGE_BURNDOWN); 
					if (burndown.equals(EXTRA_PURGE_BURNDOWN_NO)) {
						receiver.send(RESULT_CODE_CANCELED, new Bundle());
					} else {
						purgeQuakes();
						
						receiver.send(RESULT_CODE_PURGED, new Bundle());
					}
				}
			}
			
			sendBroadcast(new Intent(ACTION_REFRESH_WIDGET));
		}
	}

	/**
	 * Setting up interval of automatic update.
	 * 
	 * @param frequency
	 *            update interval, in hours.
	 */
	private void setupAutoUpdate(int frequency) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(QuakeAlarmReceiver.ACTION_REFRESH_ALARM), 0);

		long intervalMillis = frequency * ONE_HOUR_IN_MILLISECONDS;
		long timeToRefresh = SystemClock.elapsedRealtime() + intervalMillis;
		int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;

		alarmManager.setInexactRepeating(alarmType, timeToRefresh, intervalMillis, alarmIntent);
	}

	/**
	 * Cancel automatic update.
	 */
	private void cancelAutoUpdate() {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(QuakeAlarmReceiver.ACTION_REFRESH_ALARM), 0);

		alarmManager.cancel(alarmIntent);
	}

	/**
	 * Query source for new earthquakes.
	 */
	private void queryQuakes() {
		String request = assembleRequest();
		String result = executeQuery(request);
		if (result != null) {
			parseResult(result);
		}
	}

	/**
	 * Assemble query string by preferences.
	 * 
	 * @return The assembled string.
	 */
	private String assembleRequest() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int range = Integer.parseInt(prefs.getString(getString(R.string.PREF_QUERY_RANGE), "1"));
		long startMillis = System.currentTimeMillis() - range * 24 * ONE_HOUR_IN_MILLISECONDS;
		String startTime = dateFormat.format(new Date(startMillis));
		
		String minMagnitude = prefs.getString(getString(R.string.PREF_QUERY_MINIMUM), "3");

		String request = "http://earthquake.usgs.gov/fdsnws/event/1/query?" + "format=geojson" + 
				"&" + "starttime=" + startTime + 
				"&" + "minmagnitude=" + minMagnitude;

		return request;
	}

	/**
	 * Query remote server for results.
	 * 
	 * @param request
	 *            The query string.
	 * @return Results of this query, NULL otherwise.
	 */
	private String executeQuery(String request) {
		StringBuilder builder = new StringBuilder();

		try {
			URL url = new URL(request);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream inStream = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(TAG, "Http connnection error! " + "responseCode = " + responseCode);
			}
		} catch (IOException e) {
			Log.e(TAG, "I/O exception");
		}

		return builder.toString();
	}

	/**
	 * Parse the result JSON string for valid earthquakes.
	 * 
	 * @param result
	 *            the result string as JSON.
	 */
	private void parseResult(String result) {
		try {
			JSONObject reader = new JSONObject(result);

			JSONArray features = reader.getJSONArray("features");
			for (int i = 0; i < features.length(); i++) {
				JSONObject feature = features.getJSONObject(i);

				JSONObject properties = feature.getJSONObject("properties");

				double magnitude = properties.getDouble("mag");
				String place = properties.getString("place");
				long time = properties.getLong("time");
				String url = properties.getString("url");

				JSONObject geometry = feature.getJSONObject("geometry");
				JSONArray coordinates = geometry.getJSONArray("coordinates");
				double longitude = coordinates.getDouble(0);
				double latitude = coordinates.getDouble(1);

				Date date = new Date(time);

				Location location = new Location("GPS");
				location.setLongitude(longitude);
				location.setLatitude(latitude);

				Earthquake earthquake = new Earthquake(date, place, location, magnitude, url);
				addQuake(earthquake);
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSON Exception");
		}
	}

	/**
	 * Add new earthquake instance to the earthquake content provider.
	 * 
	 * @param earthquake
	 *            the instance to add.
	 */
	private void addQuake(Earthquake earthquake) {
		ContentResolver resolver = getContentResolver();

		String where = QuakeProvider.KEY_DATE + " = " + earthquake.getDate().getTime();

		Cursor query = resolver.query(QuakeProvider.CONTENT_URI, null, where, null, null);
		if (query.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(QuakeProvider.KEY_DATE, earthquake.getDate().getTime());
			values.put(QuakeProvider.KEY_DETAILS, earthquake.getDetails());
			values.put(QuakeProvider.KEY_SUMMARY, earthquake.toString());
			values.put(QuakeProvider.KEY_LOCATION_LA, earthquake.getLocation().getLatitude());
			values.put(QuakeProvider.KEY_LOCATION_LO, earthquake.getLocation().getLongitude());
			values.put(QuakeProvider.KEY_LINK, earthquake.getLink());
			values.put(QuakeProvider.KEY_MAGNITUDE, earthquake.getMagnitude());

			resolver.insert(QuakeProvider.CONTENT_URI, values);

			notifyQuake(earthquake);
		}
		query.close();
	}

	/**
	 * Purge all earthquakes stored in content provider.
	 */
	private void purgeQuakes() {
		getContentResolver().delete(QuakeProvider.CONTENT_URI, null, null);
	}

	/**
	 * Create and broadcast a new notification of earthquake.
	 * 
	 * @param earthquake
	 *            the earthquake to notify.
	 */
	private void notifyQuake(Earthquake earthquake) {
		Notification.Builder builder = new Notification.Builder(this);
		builder.setAutoCancel(true)
				.setTicker(getString(R.string.notification_ticker))
				.setSmallIcon(R.drawable.ic_notification);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean notifyToggle = prefs.getBoolean(getString(R.string.PREF_NOTIFY_TOGGLE), false);
		if (notifyToggle) {
			PendingIntent launchIntent = 
					PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

			builder.setContentIntent(launchIntent)
					.setWhen(earthquake.getDate().getTime())
					.setContentTitle("M " + earthquake.getMagnitude())
					.setContentText(earthquake.getDetails());

			boolean notifyVibrate = prefs.getBoolean(getString(R.string.PREF_NOTIFY_VIBRATE), false);
			if (notifyVibrate) {
				double vibrateLength = 100 * Math.exp(0.53 * earthquake.getMagnitude());

				builder.setVibrate(new long[] { 100, 100, (long) vibrateLength });
			}

			boolean notifySound = prefs.getBoolean(getString(R.string.PREF_NOTIFY_SOUND), false);
			if (notifySound) {
				Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

				builder.setSound(ringURI);
			}

			NotificationManager notificationManager = 
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, builder.build());
		}
	}

}
