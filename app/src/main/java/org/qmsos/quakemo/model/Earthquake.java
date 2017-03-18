package org.qmsos.quakemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Description of an earthquake instance.
 */
public class Earthquake implements Parcelable {

    // Basic info of earthquake.
    private final long mTime;
    private final double mMagnitude;
    private final double mLongitude;
    private final double mLatitude;
    private final double mDepth;

    // Details description of this earthquake.
    private String mDetails;

    // An URL link to http://earthquake.usgs.gov about this earthquake.
    private String mLink;

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
    public Earthquake(long time,
                      double magnitude,
					  double longitude,
					  double latitude,
					  double depth) {
        this.mTime = time;
        this.mMagnitude = magnitude;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mDepth = depth;
    }

    // Getters and Setters
    public String getDetails() {
        return mDetails;
    }

    public void setDetails(String details) {
        this.mDetails = details;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        this.mLink = link;
    }

    public long getTime() {
        return mTime;
    }

    public double getMagnitude() {
        return mMagnitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getDepth() {
        return mDepth;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.US);

        return dateFormat.format(new Date(mTime))
                + " - "
                + "M "
                + mMagnitude
                + " - "
                + mDetails;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mTime);
        parcel.writeDouble(mMagnitude);
        parcel.writeDouble(mLongitude);
        parcel.writeDouble(mLatitude);
        parcel.writeDouble(mDepth);
        parcel.writeString(mDetails);
        parcel.writeString(mLink);
    }

    public static final Creator<Earthquake> CREATOR = new Creator<Earthquake>() {

        @Override
        public Earthquake createFromParcel(Parcel source) {
            return new Earthquake(source);
        }

        @Override
        public Earthquake[] newArray(int size) {
            return new Earthquake[size];
        }
    };

    private Earthquake(Parcel parcel) {
        mTime = parcel.readLong();
        mMagnitude = parcel.readDouble();
        mLongitude = parcel.readDouble();
        mLatitude = parcel.readDouble();
        mDepth = parcel.readDouble();
        mDetails = parcel.readString();
        mLink = parcel.readString();
    }
}