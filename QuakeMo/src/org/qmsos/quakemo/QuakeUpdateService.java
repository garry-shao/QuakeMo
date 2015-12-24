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
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The service performing earthquake update.
 *
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
	public static final int RESULT_CODE_DISCONNECTED = 4;
	
	// Bundle keys used in ResultReceiver.
	public static final String BUNDLE_KEY_COUNT = "BUNDLE_KEY_COUNT";
	
	public static final long ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
	
	/**
	 * Class name tag. Debug use only.
	 */
	private static final String TAG = QuakeUpdateService.class.getSimpleName();

	/**
	 * Notification ID in this application.
	 */
	private static final int NOTIFICATION_ID = 1;

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
		String action = intent.getAction();
		if (action != null) {
			if (action.equals(ACTION_REFRESH_AUTO)) {
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
			} else if (action.equals(ACTION_REFRESH_MANUAL)) {
				ResultReceiver receiver = intent.getParcelableExtra(UtilResultReceiver.RECEIVER);
				if (receiver != null) {
					if (checkConnection()) {
						int count = executeRefresh();
						
						Bundle bundle = new Bundle();
						bundle.putInt(BUNDLE_KEY_COUNT, count);
						receiver.send(RESULT_CODE_REFRESHED, bundle);
					} else {
						receiver.send(RESULT_CODE_DISCONNECTED, new Bundle());
					}
				}
			} else if (action.equals(ACTION_PURGE_DATABASE)) {
				ResultReceiver receiver = intent.getParcelableExtra(UtilResultReceiver.RECEIVER);
				if (receiver != null) {
					String burndown = intent.getStringExtra(EXTRA_PURGE_BURNDOWN); 
					if (burndown.equals(EXTRA_PURGE_BURNDOWN_NO)) {
						receiver.send(RESULT_CODE_CANCELED, new Bundle());
					} else {
						purgeContentProvider();
						
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
	private void enableAutoUpdate(int frequency) {
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
	private void disableAutoUpdate() {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(QuakeAlarmReceiver.ACTION_REFRESH_ALARM), 0);

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
						sendNotification(earthquake);
					}
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
		getContentResolver().delete(QuakeProvider.CONTENT_URI, null, null);
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
			if (timeStamp > (System.currentTimeMillis() - 30 * 24 * ONE_HOUR_IN_MILLISECONDS)) {
				startTime = dateFormat.format(new Date(timeStamp));
			} else {
				startTime = dateFormat.format(
						new Date(System.currentTimeMillis() - 30 * 24 * ONE_HOUR_IN_MILLISECONDS));
			}
		} else {
			int range = Integer.parseInt(prefs.getString(
					getString(R.string.PREF_QUERY_RANGE), getString(R.string.range_values_default)));
			long startMillis = System.currentTimeMillis() - range * 24 * ONE_HOUR_IN_MILLISECONDS;
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
		
		String[] projections = { "MAX(" + QuakeProvider.KEY_TIME + ") AS " + QuakeProvider.KEY_TIME };
		
		Cursor query = getContentResolver().query(
				QuakeProvider.CONTENT_URI, projections, null, null, null);
		if (query.moveToFirst()) {
			timeStamp = query.getLong(query.getColumnIndexOrThrow(QuakeProvider.KEY_TIME));
		}
		query.close();
		
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
		boolean result = false;
		
		ContentResolver resolver = getContentResolver();

		String where = QuakeProvider.KEY_TIME + " = " + earthquake.getTime();

		Cursor query = resolver.query(QuakeProvider.CONTENT_URI, null, where, null, null);
		if (query != null) {
			if (query.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(QuakeProvider.KEY_TIME, earthquake.getTime());
				values.put(QuakeProvider.KEY_MAGNITUDE, earthquake.getMagnitude());
				values.put(QuakeProvider.KEY_LONGITUDE, earthquake.getLongitude());
				values.put(QuakeProvider.KEY_LATITUDE, earthquake.getLatitude());
				values.put(QuakeProvider.KEY_DEPTH, earthquake.getDepth());
				values.put(QuakeProvider.KEY_DETAILS, earthquake.getDetails());
				values.put(QuakeProvider.KEY_LINK, earthquake.getLink());
				resolver.insert(QuakeProvider.CONTENT_URI, values);
				
				result = true;
			}
			query.close();
		}
		
		return result;
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
