package org.qmsos.quakemo.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Description of an earthquake instance.
 *
 *
 */
public class Earthquake {

	// Basic info of earthquake.
	private final long time;
	private final double magnitude;
	private final double longitude;
	private final double latitude;
	private final double depth;

	// Details description of this earthquake.
	private String details;

	// An URL link to usgs.gov about this earthquake.
	private String link;

	/**
	 * Data structure of an earthquake.
	 * 
	 * @param time
	 *            Time when the event occurred, milliseconds since Jan. 1, 1970
	 *            UTC.
	 * @param magnitude
	 *            The magnitude for the event.
	 * @param longitude
	 *            Decimal degrees longitude. Negative values for western
	 *            longitudes.
	 * @param latitude
	 *            Decimal degrees latitude. Negative values for southern
	 *            latitudes.
	 * @param depth
	 *            Depth of the event in kilometers.
	 */
	public Earthquake(long time, double magnitude, double longitude, double latitude, double depth) {
		this.time = time;
		this.magnitude = magnitude;
		this.longitude = longitude;
		this.latitude = latitude;
		this.depth = depth;
	}

	// Getters and Setters
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public long getTime() {
		return time;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getDepth() {
		return depth;
	}

	@Override
	public String toString() {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.US);

		return dateFormat.format(new Date(time)) + " - " + "M " + magnitude + " - " + details;
	}

}
