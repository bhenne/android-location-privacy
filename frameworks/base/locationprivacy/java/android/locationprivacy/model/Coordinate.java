package android.locationprivacy.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Die Klasse Coordinate stellt eine Geographische Koordinate dar. Diese hat
 * einen Breitengrad, einen Längengrad und eine Höhe.
 * 
 * @author Christian Kater
 * 
 */
public class Coordinate implements Parcelable {
	/** Längengrad */
	public double longitude;
	/** Breitengrad */
	public double latitude;

	/** Höhe */
	public double altitude;

	public static final Parcelable.Creator<Coordinate> CREATOR = new Parcelable.Creator<Coordinate>() {

		@Override
		public Coordinate createFromParcel(Parcel source) {
			return new Coordinate(source);
		}

		@Override
		public Coordinate[] newArray(int size) {
			return new Coordinate[size];
		}
	};

	/**
	 * Instanziiert eine neues Coordinate-Objekt
	 * 
	 * @param longitude
	 *            the longitude
	 * @param latitude
	 *            the latitude
	 * @param altitude
	 *            the altitude
	 */
	public Coordinate(double longitude, double latitude, double altitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}

	/**
	 * Instanziiert eine neues Coordinate-Objekt. Die Höhe hat den Wert 0.
	 * 
	 * @param longitude
	 *            the longitude
	 * @param latitude
	 *            the latitude
	 */
	public Coordinate(double longitude, double latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = 0;
	}

	/**
	 * Instanziiert eine neues Coordinate-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt, mit Längen- und Breitengrad, sowie die Höhe.
	 */
	public Coordinate(Parcel in) {
		longitude = in.readDouble();
		latitude = in.readDouble();
		altitude = in.readDouble();
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (Double.doubleToLongBits(altitude) != Double
				.doubleToLongBits(other.altitude))
			return false;
		if (Double.doubleToLongBits(latitude) != Double
				.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double
				.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(longitude);
		dest.writeDouble(latitude);
		dest.writeDouble(altitude);
	}

	/**
	 * Gibt ein Location-Objekt mit den Koordinaten des Coordinate-Objekt
	 * zurück. Basis für alle anderen Daten des Location-Objektes, wie die
	 * Geschwindigkeit wird aus einem anderen Location-Objekt bezogen.
	 * 
	 * @param coordinate
	 *            Koordinaten 
	 * @param location
	 *            Basis
	 * @return Location mit Längen- und Breitengrad entsprechend der übergebenen Koordinate.
	 */
	public static Location getLocation(Coordinate coordinate, Location location) {
		Location newLoc = new Location(location);
		newLoc.setLatitude(coordinate.getLatitude());
		newLoc.setLongitude(coordinate.getLongitude());
		newLoc.setAltitude(coordinate.getAltitude());
		return newLoc;
	}

	/**
	 * Gibt ein Location-Objekt mit den Koordinaten des Coordinate-Objekt
	 * zurück. 
	 * 
	 * @param coordinate
	 *            Koordinaten 
	 * @return Location mit Längen- und Breitengrad entsprechend der übergebenen Koordinate.
	 */
	public static Location getLocation(Coordinate coordinate) {
		Location newLoc = new Location("GPS");
		newLoc.setLatitude(coordinate.getLatitude());
		newLoc.setLongitude(coordinate.getLongitude());
		newLoc.setAltitude(coordinate.getAltitude());
		return newLoc;
	}

	/**
	 * Erzeugt ein Coordinate-Objekt aus einem Location-Objekt. 
	 * 
	 * @param location
	 *            Standort
	 * @return Koordinaten des Standortes
	 */
	public static Coordinate getCoordinate(Location location) {
		Coordinate coordinate = new Coordinate(location.getLongitude(),
				location.getLatitude(), location.getAltitude());
		return coordinate;
	}

}
