package org.qmsos.quakemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.quakemo.provider.EarthquakeContract.Entity;
import org.qmsos.quakemo.util.IntentConstants;

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
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * The main service performing background jobs.
 *
 *
 */
public class EarthquakeService extends IntentService {

	/**
	 * Class name tag. Debug use only.
	 */
	private static final String TAG = EarthquakeService.class.getSimpleName();

	/**
	 * Default constructor of this service.
	 */
	public EarthquakeService() {
		super(TAG);
	}

	/**
	 * Constructor with a debug-purpose name of this service.
	 * 
	 * @param workThreadName
	 *            Tag name for debug.
	 */
	public EarthquakeService(String workThreadName) {
		super(workThreadName);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (action == null) {
			return;
		}
		
		if (action.equals(IntentConstants.ACTION_REFRESH_AUTO)) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean flagAuto = prefs.getBoolean(getString(R.string.PREF_AUTO_REFRESH), false);
			
			scheduleAutoRefresh(flagAuto);
			
			if (flagAuto && checkConnection()) {
				executeRefresh();
			}
		} else if (action.equals(IntentConstants.ACTION_REFRESH_MANUAL)) {
			Intent localIntent = new Intent(IntentConstants.ACTION_REFRESH_EXECUTED);
			
			if (checkConnection()) {
				int count = executeRefresh();
				
				if (count >= 0) {
					localIntent.putExtra(IntentConstants.EXTRA_REFRESH_EXECUTED, true);
					localIntent.putExtra(IntentConstants.EXTRA_ADDED_COUNT, count);
				} else {
					localIntent.putExtra(IntentConstants.EXTRA_REFRESH_EXECUTED, false);
				}
			} else {
				localIntent.putExtra(IntentConstants.EXTRA_REFRESH_EXECUTED, false);
			}
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
		} else if (action.equals(IntentConstants.ACTION_PURGE_DATABASE)) {
			Intent localIntent = new Intent(IntentConstants.ACTION_PURGE_EXECUTED);
			
			boolean flagPurge = intent.getBooleanExtra(IntentConstants.EXTRA_PURGE_DATABASE, false);
			if (flagPurge) {
				executePurgeDatabase();
				
				localIntent.putExtra(IntentConstants.EXTRA_PURGE_EXECUTED, true);
			} else {
				localIntent.putExtra(IntentConstants.EXTRA_PURGE_EXECUTED, false);
			}
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
		}
		
		sendBroadcast(new Intent(IntentConstants.ACTION_REFRESH_WIDGET));
	}

	/**
	 * Execute the workflow of refreshing for new earthquakes.
	 * 
	 * @return How many new earthquakes added to database. -1 if some error happened.
	 */
	private int executeRefresh() {
		int errorCount = -1;
		
		String request = assembleRequest();
		String result = download(request);
		
		if (result == null) {
			return errorCount;
		}
		
		try {
			JSONObject reader = new JSONObject(result);
			JSONArray features = reader.getJSONArray("features");
			int length = features.length();
			if (length <= 0) {
				return errorCount;
			}

			long timeStamp = 0;
			ContentValues valueStamp = null;

			LinkedList<ContentValues> valueList = new LinkedList<ContentValues>();
			for (int i = 0; i < length; i++) {
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
				double depth = coordinates.getDouble(2);

				Cursor cursor = null;
				try {
					ContentResolver resolver = getContentResolver();
					String where = Entity.TIME + " = " + time;
					cursor = resolver.query(Entity.CONTENT_URI, null, where, null, null);
					if (cursor != null && !cursor.moveToNext()) {
						ContentValues value = new ContentValues();
						value.put(Entity.TIME, time);
						value.put(Entity.MAGNITUDE, magnitude);
						value.put(Entity.LONGITUDE, longitude);
						value.put(Entity.LATITUDE, latitude);
						value.put(Entity.DEPTH, depth);
						value.put(Entity.DETAILS, place);
						value.put(Entity.LINK, url);
						
						valueList.add(value);
						
						if (time > timeStamp) {
							timeStamp = time;
							valueStamp = value;
						}
					}
				} finally {
					if (cursor != null && !cursor.isClosed()) {
						cursor.close();
					}
				}
			}
			
			int valueListSize = valueList.size();
			ContentValues[] values = new ContentValues[valueListSize];
			valueList.toArray(values);
			
			int count = getContentResolver().bulkInsert(Entity.CONTENT_URI, values);
			if (count > 0 && count == valueListSize && timeStamp > 0 && valueStamp != null) {
				sendNotification(valueStamp);
			}
			
			return count;
		} catch (JSONException e) {
			Log.e(TAG, "Something wrong with the result JSON");
			
			return errorCount;
		}
	}

	/**
	 * Purge all earthquakes stored in database.
	 */
	private void executePurgeDatabase() {
		getContentResolver().delete(Entity.CONTENT_URI, null, null);
	}

	/**
	 * Schedule the behavior of the alarm that invoked repeatedly to execute automatic refresh.
	 * 
	 * @param flag
	 *            TRUE when setting up the alarm, FALSE when canceling the alarm.
	 */
	private void scheduleAutoRefresh(boolean flag) {
		int requestCode = 1;
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, requestCode,
				new Intent(IntentConstants.ACTION_REFRESH_ALARM), PendingIntent.FLAG_UPDATE_CURRENT);
		
		if (flag) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int frequency = Integer.parseInt(prefs.getString(
					getString(R.string.PREF_AUTO_FREQUENCY), 
					getString(R.string.frequency_values_default)));
			
			long intervalMillis = frequency * AlarmManager.INTERVAL_HOUR;
			long timeToRefresh = SystemClock.elapsedRealtime() + intervalMillis;
			
			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
					timeToRefresh, intervalMillis, alarmIntent);
		} else {
			alarmManager.cancel(alarmIntent);
		}
	}

	/**
	 * Create and broadcast a new notification of earthquake.
	 * 
	 * @param value
	 *            The ContentValues instance that contains the earthquake.
	 */
	private void sendNotification(ContentValues value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean flagNotify = prefs.getBoolean(getString(R.string.PREF_NOTIFY_TOGGLE), false);
		if (!flagNotify) {
			return;
		}
		
		PendingIntent launchIntent = 
				PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

		long time = value.getAsLong(Entity.TIME);
		double magnitude = value.getAsDouble(Entity.MAGNITUDE);
		String details = value.getAsString(Entity.DETAILS);
		
		Notification.Builder builder = new Notification.Builder(this);
		builder.setAutoCancel(true)
				.setTicker(getString(R.string.notification_ticker))
				.setSmallIcon(R.drawable.ic_notification)
				.setContentIntent(launchIntent)
				.setWhen(time)
				.setContentTitle("M " + magnitude)
				.setContentText(details);

		boolean flagSound = prefs.getBoolean(getString(R.string.PREF_NOTIFY_SOUND), false);
		if (flagSound) {
			Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

			builder.setSound(ringUri);
		}

		int nofiticationId = 1;
		NotificationManager notificationManager = 
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(nofiticationId, builder.build());
	}

	/**
	 * Download from remote server for results.
	 * 
	 * @param request
	 *            The request string of URL.
	 * @return Results of this query, NULL otherwise.
	 */
	private String download(String request) {
		StringBuilder builder = new StringBuilder();
	
		try {
			// workaround: whitespace makes the url invalid, replace with URL-encode.
			URL url = new URL(request.replace(" ", "%20"));
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			try {
				int responseCode = httpConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream inStream = httpConnection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
					
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "Error reading from the http connection");
			} finally {
				httpConnection.disconnect();
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malformed URL");
		} catch (IOException e) {
			Log.e(TAG, "Error opening the http connection");
		}
	
		return builder.toString();
	}

	/**
	 * Assemble string that sent to remote server.
	 * 
	 * @return The assembled string.
	 */
	private String assembleRequest() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		long timeStamp = findTimeStamp();
		String startTime;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		boolean querySeamless = prefs.getBoolean(getString(R.string.PREF_QUERY_FOLLOW), false);
		if (querySeamless) {
			long defaultStartMillis = System.currentTimeMillis() - 7 * AlarmManager.INTERVAL_DAY;
			if (timeStamp > defaultStartMillis) {
				startTime = dateFormat.format(new Date(timeStamp));
			} else {
				startTime = dateFormat.format(new Date(defaultStartMillis));
			}
		} else {
			int range = Integer.parseInt(prefs.getString(
					getString(R.string.PREF_QUERY_RANGE), getString(R.string.range_values_default)));
			long startMillis = System.currentTimeMillis() - range * AlarmManager.INTERVAL_DAY;
			if (startMillis < timeStamp) {
				startTime = dateFormat.format(new Date(timeStamp));
			} else {
				startTime = dateFormat.format(new Date(startMillis));
			}
		}
		String minMagnitude = prefs.getString(
				getString(R.string.PREF_QUERY_MINIMUM), getString(R.string.minimum_values_default));

		String query = "http://earthquake.usgs.gov/fdsnws/event/1/query?" + "format=geojson" + 
				"&" + "starttime=" + startTime + 
				"&" + "minmagnitude=" + minMagnitude;

		return query;
	}

	/**
	 * Find the time stamp of most recent earthquake. 
	 * 
	 * @return Time stamp of the most recent earthquake in milliseconds or 0 if not found.
	 */
	private long findTimeStamp() {
		long timeStamp = 0;
		
		Cursor cursor = null;
		try {
			String[] projection = { "MAX(" + Entity.TIME + ") AS " + Entity.TIME };
			
			cursor = getContentResolver().query(Entity.CONTENT_URI, projection, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow(Entity.TIME));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Column does not exists");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		return timeStamp;
	}

	/**
	 * Check if there is a valid connection to Internet.
	 * 
	 * @return TRUE if a valid connection exists, FALSE otherwise.
	 */
	private boolean checkConnection() {
		ConnectivityManager manager = 
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String type = prefs.getString(
					getString(R.string.PREF_CONNECTION), getString(R.string.connection_values_wifi));
			if (type.equals(getString(R.string.connection_values_any)) 
					|| (info.getType()) == ConnectivityManager.TYPE_WIFI) {
				
				return true;
			}
		}
		
		return false;
	}

}
