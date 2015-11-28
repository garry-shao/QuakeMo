package org.qmsos.quakemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

/**
 * 
 * Description of an earthquake instance.
 *
 */
public class Earthquake {

	/**
	 * Date of this earthquake.
	 */
	private Date date;

	/**
	 * Details description of this earthquake.
	 */
	private String details;

	/**
	 * The spot of this earthquake as Location.class.
	 */
	private Location location;

	/**
	 * The magnitude of this earthquake as "Ms".
	 */
	private Double magnitude;

	/**
	 * An URL link to usgs.gov about this earthquake.
	 */
	private String link;

	/**
	 * Construct an earthquake instance.
	 * 
	 * @param date
	 *            date of this earthquake.
	 * @param details
	 *            details of this earthquake.
	 * @param location
	 *            location of this earthquake.
	 * @param magnitude
	 *            magnitude of this earthquake.
	 * @param link
	 *            link to usgs.gov about this earthquake.
	 */
	public Earthquake(Date date, String details, Location location, Double magnitude, String link) {
		this.date = date;
		this.details = details;
		this.location = location;
		this.magnitude = magnitude;
		this.link = link;
	}

	/**
	 * Get the date of this earthquake.
	 * 
	 * @return date of this earthquake.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Get details of this earthquake.
	 * 
	 * @return details of this earthquake.
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * Get location of this earthquake.
	 * 
	 * @return location of this earthquake.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Get magnitude of this earthquake.
	 * 
	 * @return magnitude of this earthquake.
	 */
	public Double getMagnitude() {
		return magnitude;
	}

	/**
	 * Link to USGS about this earthquake.
	 * 
	 * @return link to this earthquake.
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Get dialog-suitable details of this earthquake.
	 * 
	 * @return the dialog-suitable details string.
	 */
	public String getDialogDetails() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		String dateString = dateFormat.format(date);

		return dateString + "\n" + "Magnitude " + magnitude + "\n" + details + "\n" + link;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm", Locale.US);
		String dateString = dateFormat.format(date);

		return dateString + ": " + magnitude + "M: " + details;
	}

}
