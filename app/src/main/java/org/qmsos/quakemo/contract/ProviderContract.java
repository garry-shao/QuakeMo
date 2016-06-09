package org.qmsos.quakemo.contract;

import android.net.Uri;

/**
 * The contract of the earthquake provider. Contains definition of the supported 
 * URIs and data columns.
 */
public final class ProviderContract {

	/**
	 * Authority used in CRUD operations from earthquake provider.
	 */
	public static final String AUTHORITY = "org.qmsos.quakemo.earthquakeprovider";
	
	/**
	 * Class that represents entity of earthquake table.
	 */
	public static final class Entity implements Columns {
		
		/**
		 * The URL for accessing earthquakes(content://).
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/earthquakes");
	
	}

	/**
	 * Columns of the earthquake table.
	 */
	protected interface Columns {
		
		/**
		 * Primary key that auto increments. <p>TYPE: INTEGER</p> 
		 */
		String ID = "_id";
		
		/**
		 * The time that earthquake occurred, in UTC. <p>TYPE: INTEGER</p>
		 */
		String TIME = "time";
		
		/**
		 * The magnitude of that earthquake. <p>TYPE: REAL</p>
		 */
		String MAGNITUDE = "magnitude";
		
		/**
		 * The longitude of the center of that earthquake, negative 
		 * for western longitude. <p>TYPE: REAL</p>
		 */
		String LONGITUDE = "longitude";
		
		/**
		 * The latitude of the center of that earthquake, negative 
		 * for southern latitude. <p>TYPE: REAL</p>
		 */
		String LATITUDE = "latitude";
		
		/**
		 * The depth of the center of that earthquake. <p>TYPE: REAL</p>
		 */
		String DEPTH = "depth";
		
		/**
		 * The description of the center of that earthquake. <p>TYPE: TEXT</p>
		 */
		String DETAILS = "details";
		
		/**
		 * The link of the earthquake, to website of http://earthquake.usgs.gov
		 * about this earthquake. <p>TYPE: TEXT</p>
		 */
		String LINK = "link";
	
	}

}
