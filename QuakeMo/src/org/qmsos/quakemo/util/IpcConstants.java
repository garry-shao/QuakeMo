package org.qmsos.quakemo.util;

/**
 * Utility class containing various information(action, bundle's key etc.) in app's components, 
 * including Inter-Process Communication.
 * 
 *
 */
public class IpcConstants {

	// base package name, normally app's package name.
	private static final String BASE_PACKAGE_NAME = "org.qmsos.quakemo" + ".";

	// below are standard intent actions.

	/**
	 * Broadcast action: setting up repeat alarm.
	 */
	public static final String ACTION_REFRESH_ALARM = BASE_PACKAGE_NAME + "ACTION_REFRESH_ALARM";

	/**
	 * Service action: refreshing widget.
	 */
	public static final String ACTION_REFRESH_WIDGET = BASE_PACKAGE_NAME + "ACTION_REFRESH_WIDGET";

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
	public static final String EXTRA_PURGE_DATABASE = "EXTRA_PURGE_DATABASE";

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
	public static final String EXTRA_REFRESH_EXECUTED = "EXTRA_REFRESH_EXECUTED";

	/**
	 * Used as an integer field containing how many new entries(aka earthquakes) added.
	 */
	public static final String EXTRA_ADDED_COUNT = "EXTRA_ADDED_COUNT";

	/**
	 * Service action: purging database executed, normally used in 
	 * {@linkplain android.support.v4.content.LocalBroadcastManager LocalBroadcastManager}, 
	 * this should be seen as reporting back of {@link #ACTION_PURGE_DATABASE}.
	 */
	public static final String ACTION_PURGE_EXECUTED = BASE_PACKAGE_NAME + "ACTION_PURGE_EXECUTED";
	
	/**
	 * Used as a boolean field in determine whether the database is purged.
	 */
	public static final String EXTRA_PURGE_EXECUTED = "EXTRA_PURGE_EXECUTED";

	/**
	 * Search feature: Used to pass query string, should be in a bundle.
	 */
	public static final String QUERY_CONTENT_KEY = "QUERY_CONTENT_KEY";

}
