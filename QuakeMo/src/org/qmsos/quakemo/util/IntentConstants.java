package org.qmsos.quakemo.util;

/**
 * Utility class containing various information(action, bundle's key etc.) in app's components, 
 * including Inter-Process Communication.
 * 
 *
 */
public class IntentConstants {

	// base package name, normally app's package name.
	private static final String BASE_PACKAGE_NAME = "org.qmsos.quakemo" + ".";

	// below are standard intent actions.

	/**
	 * Broadcast action: setting up repeat alarm.
	 */
	public static final String ACTION_REFRESH_ALARM = BASE_PACKAGE_NAME + "ACTION_REFRESH_ALARM";

	/**
	 * Service action: refreshing appwidget.
	 */
	public static final String ACTION_REFRESH_APPWIDGET = BASE_PACKAGE_NAME + "ACTION_REFRESH_APPWIDGET";

	/**
	 * Service action: automatic refreshing for new earthquakes.
	 */
	public static final String ACTION_REFRESH_AUTO = BASE_PACKAGE_NAME + "ACTION_REFRESH_AUTO";
	
	/**
	 * Activity action: manually refreshing for new earthquakes.
	 */
	public static final String ACTION_REFRESH_MANUAL = BASE_PACKAGE_NAME + "ACTION_REFRESH_MANUAL";

	/**
	 * Activity action: purging database.
	 */
	public static final String ACTION_PURGE_DATABASE = BASE_PACKAGE_NAME + "ACTION_PURGE_DATABASE";

	/**
	 * Used in UNDO purging feature, containing extra info to determine whether undo the 
	 * purging process. 
	 */
	public static final String EXTRA_PURGE_DATABASE = BASE_PACKAGE_NAME + "EXTRA_PURGE_DATABASE";

	// below are callback actions, used in LocalBroadcastManager.
	
	/**
	 * Service action: manually refreshing for new earthquakes executed, normally used in 
	 * {@linkplain android.support.v4.content.LocalBroadcastManager LocalBroadcastManager}, 
	 * this should be seen as reporting back of {@link #ACTION_REFRESH_MANUAL}.
	 */
	public static final String ACTION_REFRESH_EXECUTED = BASE_PACKAGE_NAME + "ACTION_REFRESH_EXECUTED";

	/**
	 * Used as a boolean field in determine whether the refreshing succeeded(sync with server).
	 */
	public static final String EXTRA_REFRESH_EXECUTED = BASE_PACKAGE_NAME + "EXTRA_REFRESH_EXECUTED";

	/**
	 * Used as an integer field containing how many new entries(aka earthquakes) added.
	 */
	public static final String EXTRA_ADDED_COUNT = BASE_PACKAGE_NAME + "EXTRA_ADDED_COUNT";

	/**
	 * Service action: purging database executed, normally used in 
	 * {@linkplain android.support.v4.content.LocalBroadcastManager LocalBroadcastManager}, 
	 * this should be seen as reporting back of {@link #ACTION_PURGE_DATABASE}.
	 */
	public static final String ACTION_PURGE_EXECUTED = BASE_PACKAGE_NAME + "ACTION_PURGE_EXECUTED";
	
	/**
	 * Used as a boolean field in determine whether the database is purged.
	 */
	public static final String EXTRA_PURGE_EXECUTED = BASE_PACKAGE_NAME + "EXTRA_PURGE_EXECUTED";

}
