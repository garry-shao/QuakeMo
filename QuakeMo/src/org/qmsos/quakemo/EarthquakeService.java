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
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.quakemo.util.Earthquake;
import org.qmsos.quakemo.util.IpcConstants;

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
	 * Notification ID in this application.
	 */
	private static final int EARTHQUAKE_NOTIFICATION_ID = 3;

	/**
	 * Unique request code of the auto update alarm's PendingIntent.
	 */
	private static final int UPDATE_ALARM_REQUEST_CODE = 5;
	
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
		if (action != null) {
			if (action.equals(IpcConstants.ACTION_REFRESH_AUTO)) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				boolean autoRefresh = prefs.getBoolean(getString(R.string.PREF_AUTO_REFRESH), false);
				if (autoRefresh) {
					int frequency = Integer.parseInt(prefs.getString(
							getString(R.string.PREF_AUTO_FREQUENCY), 
							getString(R.string.frequency_values_default)));
					
					enableAutoUpdate(frequency);

					if (checkConnection()) {
						executeRefresh();
					}
				} else {
					disableAutoUpdate();
				}
			} else if (action.equals(IpcConstants.ACTION_REFRESH_MANUAL)) {
				Intent localIntent = new Intent(IpcConstants.ACTION_REFRESH_EXECUTED);
				
				if (checkConnection()) {
					int count = executeRefresh();
					
					localIntent.putExtra(IpcConstants.EXTRA_REFRESH_EXECUTED, true);
					localIntent.putExtra(IpcConstants.EXTRA_ADDED_COUNT, count);
				} else {
					localIntent.putExtra(IpcConstants.EXTRA_REFRESH_EXECUTED, false);
				}

				LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			} else if (action.equals(IpcConstants.ACTION_PURGE_DATABASE)) {
				Intent localIntent = new Intent(IpcConstants.ACTION_PURGE_EXECUTED);

				boolean flag = intent.getBooleanExtra(IpcConstants.EXTRA_PURGE_DATABASE, false);
				if (flag) {
					purgeContentProvider();
					
					localIntent.putExtra(IpcConstants.EXTRA_PURGE_EXECUTED, true);
				} else {
					localIntent.putExtra(IpcConstants.EXTRA_PURGE_EXECUTED, false);
				}
				
				LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			}
			
			sendBroadcast(new Intent(IpcConstants.ACTION_REFRESH_WIDGET));
		}
	}

	/**
	 * Setting up interval of automatic update.
	 * 
	 * @param frequency
	 *            update interval, in hours.
	 */
	private void enableAutoUpdate(int frequency) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 
				UPDATE_ALARM_REQUEST_CODE,
				new Intent(IpcConstants.ACTION_REFRESH_ALARM), 
				PendingIntent.FLAG_UPDATE_CURRENT);

		long intervalMillis = frequency * AlarmManager.INTERVAL_HOUR;
		long timeToRefresh = SystemClock.elapsedRealtime() + intervalMillis;
		
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
				timeToRefresh, intervalMillis, alarmIntent);
	}

	/**
	 * Cancel automatic update.
	 */
	private void disableAutoUpdate() {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 
				UPDATE_ALARM_REQUEST_CODE,
				new Intent(IpcConstants.ACTION_REFRESH_ALARM),
				PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.cancel(alarmIntent);
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

	/**
	 * Execute the query instance for new earthquakes.
	 * 
	 * @return How many new earthquakes added to content provider.
	 */
	private int executeRefresh() {
		int count = 0;
		
		String result = download(assembleQuery());
		if (result != null && result.length() > 0) {
			try {
				long timeStamp = 0;
				Earthquake quakeStamp = null;
				
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
					double depth = coordinates.getDouble(2);
			
					Earthquake earthquake = new Earthquake(time, magnitude, longitude, latitude, depth);
					earthquake.setDetails(place);
					earthquake.setLink(url);
					
					boolean added = addToProvider(earthquake);
					if (added) {
						count++;
						
						if (time > timeStamp) {
							timeStamp = time;
							quakeStamp = earthquake;
						}
					}
				}
				
				if (timeStamp > 0 && quakeStamp != null) {
					sendNotification(quakeStamp);
				}
			} catch (JSONException e) {
				Log.e(TAG, "Something wrong with the result JSON");
			}
		}
		
		return count;
	}

	/**
	 * Purge all earthquakes stored in content provider.
	 */
	private void purgeContentProvider() {
		getContentResolver().delete(EarthquakeProvider.CONTENT_URI, null, null);
	}

	/**
	 * Assemble query string out of preferences.
	 * 
	 * @return The assembled string.
	 */
	private String assembleQuery() {
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
	 * Find the time stamp of the recent earthquake. 
	 * 
	 * @return Time stamp of the recent earthquake in milliseconds or 0 if not found.
	 */
	private long findTimeStamp() {
		long timeStamp = 0;
		
		Cursor cursor = null;
		try {
			String[] projections = { "MAX(" + EarthquakeProvider.KEY_TIME + ") AS " + EarthquakeProvider.KEY_TIME };
			
			cursor = getContentResolver().query(EarthquakeProvider.CONTENT_URI, projections, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow(EarthquakeProvider.KEY_TIME));
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
	 * Download from remote server for results.
	 * 
	 * @param query
	 *            The query string.
	 * @return Results of this query, NULL otherwise.
	 */
	private String download(String query) {
		StringBuilder builder = new StringBuilder();

		try {
			URL url = new URL(query);
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
	 * Add new earthquake instance to the earthquake content provider.
	 * 
	 * @param earthquake
	 *            the instance to add.
	 * @return TRUE if earthquake successfully added, FALSE otherwise.
	 */
	private boolean addToProvider(Earthquake earthquake) {
		if (earthquake == null) {
			return false;
		}
		
		boolean flag = false;
		
		Cursor cursor = null;
		try {
			ContentResolver resolver = getContentResolver();
			String where = EarthquakeProvider.KEY_TIME + " = " + earthquake.getTime();
			cursor = resolver.query(EarthquakeProvider.CONTENT_URI, null, where, null, null);
			if (cursor != null && !cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(EarthquakeProvider.KEY_TIME, earthquake.getTime());
				values.put(EarthquakeProvider.KEY_MAGNITUDE, earthquake.getMagnitude());
				values.put(EarthquakeProvider.KEY_LONGITUDE, earthquake.getLongitude());
				values.put(EarthquakeProvider.KEY_LATITUDE, earthquake.getLatitude());
				values.put(EarthquakeProvider.KEY_DEPTH, earthquake.getDepth());
				values.put(EarthquakeProvider.KEY_DETAILS, earthquake.getDetails());
				values.put(EarthquakeProvider.KEY_LINK, earthquake.getLink());
				resolver.insert(EarthquakeProvider.CONTENT_URI, values);
				
				flag = true;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error found when adding earthquake to provider");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		return flag;
	}

	/**
	 * Create and broadcast a new notification of earthquake.
	 * 
	 * @param earthquake
	 *            the earthquake to notify.
	 */
	private void sendNotification(Earthquake earthquake) {
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
					.setWhen(earthquake.getTime())
					.setContentTitle("M " + earthquake.getMagnitude())
					.setContentText(earthquake.getDetails());
	
			boolean notifySound = prefs.getBoolean(getString(R.string.PREF_NOTIFY_SOUND), false);
			if (notifySound) {
				Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	
				builder.setSound(ringURI);
			}
	
			NotificationManager notificationManager = 
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(EARTHQUAKE_NOTIFICATION_ID, builder.build());
		}
	}

}
