package org.qmsos.quakemo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * The service performing earthquake update.
 *
 */
public class EarthquakeUpdateService extends IntentService {
	
	public static final String TAG = "EARTHQUAKE_UPDATE_SERVICE";
	
	public static final String QUAKES_REFRESHED = "org.qmsos.quakemo.QUAKES_REFRESHED";
	public static final String MANUAL_REFRESH = "org.qmsos.quakemo.MANUAL_REFRESH";
	public static final String PURGE_DATABASE = "org.qmsos.quakemo.PURGE_DATABASE";
	
	public static final int NOTIFICATION_ID = 1;
	
	private AlarmManager alarmManager;
	private PendingIntent alarmIntent;
	private Notification.Builder notificationBuilder;
	
	/**
	 * Default constructor of this service.
	 */
	public EarthquakeUpdateService() {
		super(TAG);
	}

	/**
	 * Constructor with a debug-purpose name of this service.
	 * @param workThreadName Tag name for debug.
	 */
	public EarthquakeUpdateService(String workThreadName) {
		super(workThreadName);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		Intent intentToFire = new Intent(EarthquakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM);
		alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
		
		notificationBuilder = new Notification.Builder(this);
		notificationBuilder
			.setAutoCancel(true)
			.setTicker(getBaseContext().getString(R.string.notification_ticker))
			.setSmallIcon(R.drawable.notification_icon);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		int updateFreq = Integer.parseInt(
				prefs.getString(MainPreferenceActivity.PREF_UPDATE_FREQ, "60"));
		boolean autoUpdateChecked = 
				prefs.getBoolean(MainPreferenceActivity.PREF_AUTO_UPDATE, false);
		if (autoUpdateChecked) {
			int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
			int intervalMillis = updateFreq * 60 * 1000;
			long timeToRefresh = SystemClock.elapsedRealtime() + intervalMillis;
			
			refreshEarthquakes();
			sendBroadcast(new Intent(QUAKES_REFRESHED));

			alarmManager.setInexactRepeating(
					alarmType, timeToRefresh, intervalMillis, alarmIntent);
		} else {
			alarmManager.cancel(alarmIntent);
		}
		
		if (intent.getBooleanExtra(MANUAL_REFRESH, false)) {
			refreshEarthquakes();
			sendBroadcast(new Intent(QUAKES_REFRESHED));
		}
		
		if (intent.getBooleanExtra(PURGE_DATABASE, false)) {
			purgeAllEarthquakes();
			sendBroadcast(new Intent(PURGE_DATABASE));
		}
		
		Context context = getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName earthquakeWidget = new ComponentName(context, EarthquakeWidgetList.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(earthquakeWidget);
		
		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
	}

	/**
	 * Query source for new earthquakes.
	 */
	public void refreshEarthquakes() {
		URL url;
		
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String dateString = dateFormat.format(new Date());
			
			SharedPreferences prefs = 
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			int minMagnitude = Integer.parseInt(
					prefs.getString(MainPreferenceActivity.PREF_MIN_MAG, "3"));
			
			String quakeFeed = 
					"http://earthquake.usgs.gov/fdsnws/event/1/query?" +
					"format=xml" + "&" + 
					"starttime=" + dateString + "&" + 
					"minmagnitude=" + minMagnitude;
			url = new URL(quakeFeed);
			
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream inStream = httpConnection.getInputStream();
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				
				Document dom = builder.parse(inStream);
				Element domElement = dom.getDocumentElement();
				
				Element eventParameters = (Element) 
						domElement.getElementsByTagName("eventParameters").item(0);
				
				dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'.'SSS'Z'", Locale.US);
				NodeList list = eventParameters.getElementsByTagName("event");
				if ((list != null) && (list.getLength() > 0)) {
					for (int i = 0; i < list.getLength(); i++) {
						Element event = (Element) list.item(i);
						
						Element origin = (Element) event.getElementsByTagName("origin").item(0);
						Element time = (Element) origin.getElementsByTagName("time").item(0);
						Element value = (Element) time.getElementsByTagName("value").item(0);
						Date earthquakeDate = new Date();
						try {
							earthquakeDate = dateFormat.parse(value.getFirstChild().getNodeValue());
						} catch (ParseException e) {
							Log.d(TAG, "Date parsing exception.", e);
						}
						
						Element description = (Element) event.getElementsByTagName("description").item(0);
						Element text = (Element) description.getElementsByTagName("text").item(0);
						String details = text.getFirstChild().getNodeValue();
					
						Location loc = new Location("dummyGPS");
						Element longitude = (Element) origin.getElementsByTagName("longitude").item(0);
						value = (Element) longitude.getElementsByTagName("value").item(0);
						loc.setLongitude(Double.parseDouble(value.getFirstChild().getNodeValue()));
						Element latitude = (Element) origin.getElementsByTagName("latitude").item(0);
						value = (Element) latitude.getElementsByTagName("value").item(0);
						loc.setLatitude(Double.parseDouble(value.getFirstChild().getNodeValue()));
						
						Element magnitude = (Element) event.getElementsByTagName("magnitude").item(0);
						value = (Element) magnitude.getElementsByTagName("value").item(0);
						double mag = Double.parseDouble(value.getFirstChild().getNodeValue());
					
						String link = origin.getAttribute("publicID");
						link = link.replace("quakeml:", "http://");
						
						Earthquake earthquake = new Earthquake(earthquakeDate, details, loc, mag, link);

						addNewEarthquake(earthquake);
					}
				}
			}
		} catch (IOException e) {
			Log.d(TAG, "IOException");
		} catch (ParserConfigurationException e) {
			Log.d(TAG, "Parser Configuration Exception");
		} catch (SAXException e) {
			Log.d(TAG, "SAX Exception");
		} finally {
		}
	}
	
	/**
	 * Add new earthquake instance to the earthquake content provider.
	 * @param earthquake the instance to add.
	 */
	private void addNewEarthquake(Earthquake earthquake) {
		ContentResolver resolver = getContentResolver();
		
		String where = EarthquakeProvider.KEY_DATE + " = " + earthquake.getDate().getTime();
		
		Cursor query = resolver.query(EarthquakeProvider.CONTENT_URI, null, where, null, null);
		if (query.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(EarthquakeProvider.KEY_DATE, earthquake.getDate().getTime());
			values.put(EarthquakeProvider.KEY_DETAILS, earthquake.getDetails());
			values.put(EarthquakeProvider.KEY_SUMMARY, earthquake.toString());
			values.put(EarthquakeProvider.KEY_LOCATION_LA, earthquake.getLocation().getLatitude());
			values.put(EarthquakeProvider.KEY_LOCATION_LO, earthquake.getLocation().getLongitude());
			values.put(EarthquakeProvider.KEY_LINK, earthquake.getLink());
			values.put(EarthquakeProvider.KEY_MAGNITUDE, earthquake.getMagnitude());
			
			resolver.insert(EarthquakeProvider.CONTENT_URI, values);
			
			broadcastNotification(earthquake);
		}
		query.close();
	}

	/**
	 * Purge all earthquakes stored in content provider.
	 */
	private void purgeAllEarthquakes() {
		ContentResolver resolver = getContentResolver();
		
		resolver.delete(EarthquakeProvider.CONTENT_URI, null, null);
	}

	/**
	 * Create and broadcast a new notification of earthquake.
	 * @param earthquake the earthquake to notify.
	 */
	private void broadcastNotification(Earthquake earthquake) {
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean notificationEnable = 
				prefs.getBoolean(MainPreferenceActivity.PREF_NOTIFICATION_ENABLE, true);
		boolean notificationSound = 
				prefs.getBoolean(MainPreferenceActivity.PREF_NOTIFICATION_SOUND, true);
		boolean notificationVibrate = 
				prefs.getBoolean(MainPreferenceActivity.PREF_NOTIFICATION_VIBRATE, true);
		
		if (notificationEnable) {
			PendingIntent launchIntent = 
					PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

			notificationBuilder
				.setContentIntent(launchIntent)
				.setWhen(earthquake.getDate().getTime())
				.setContentTitle("M:" + earthquake.getMagnitude())
				.setContentText(earthquake.getDetails());
		
			if (notificationVibrate) {
				double vibrateLength = 100 * Math.exp(0.53 * earthquake.getMagnitude());
				
				notificationBuilder.setVibrate(new long[] { 100, 100, (long) vibrateLength });
			}
			if (notificationSound) {
				Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

				notificationBuilder.setSound(ringURI);
			}
		
			NotificationManager notificationManager = 
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
		}
	}
	
}
