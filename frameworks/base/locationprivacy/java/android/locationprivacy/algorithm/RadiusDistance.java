package android.locationprivacy.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

/**
 * Die Klasse RadiusDistance verschleiert den Standort unter Angabe eines Maximalradius,
 * Mindestabstands und unter Angabe, wieviel Meter das Endgerät bewegt werden
 * muss, um einen neuen Standort zu berechnen. 
 */
public class RadiusDistance extends AbstractLocationPrivacyAlgorithm {

	/** The Constant NAME. */
	private static final String NAME = "radiusdistance";

	/** The radius2. */
	public static double radius2;

	/**
	 * Instanziert eine neues RadiusDistance-Objekt.
	 */
	public RadiusDistance() {
		super(NAME);
	}

	/**
	 * Instanziert eine neues RadiusDistance-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt, dass die Konfiguration des Algorithmus enthält.
	 */
	public RadiusDistance(Parcel in) {
		super(in, NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#writeToParcel
	 * (android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#newInstance
	 * ()
	 */
	public AbstractLocationPrivacyAlgorithm newInstance() {
		return new RadiusDistance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * getDefaultConfiguration()
	 */
	public LocationPrivacyConfiguration getDefaultConfiguration() {
		HashMap<String, Integer> intValues = new HashMap<String, Integer>();
		intValues.put("radius", 500);
		intValues.put("movement", 50);
		intValues.put("distance", 200);
		Map<String, Coordinate> coordinateValues = new HashMap<String, Coordinate>();
		coordinateValues.put("private_lastlocation", new Coordinate(
				Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		return new LocationPrivacyConfiguration(intValues,
				new HashMap<String, Double>(), new HashMap<String, String>(),
				new HashMap<String, ArrayList<String>>(),
				new HashMap<String, String>(), coordinateValues,
				new HashMap<String, Boolean>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * calculateLocation(android.location.Location)
	 */
	public Location calculateLocation(Location location) {
		double EARTH_RADIUS = 6371000;
		double TO_RADIAN = Math.PI / 180;
		double METER_PER_LATITUDE = 111320;

		int movement = configuration.getInt("movement");
		int distance = configuration.getInt("distance");
		int radius = configuration.getInt("radius");
		Location lastLocation = Coordinate.getLocation(configuration
				.getCoordinate("private_lastlocation"));

		if (lastLocation.getLongitude() == 999.0
				|| location.distanceTo(lastLocation) > movement) {
			Location calcLoc = new Location(location);
			Random rnd = new Random();
			double alpha = rnd.nextDouble() * 360.0 * TO_RADIAN;
			double r = rnd.nextDouble() * (radius - distance) + distance;
			double meterPerLong = Math.abs((2 * Math.PI
					* Math.cos(location.getLatitude() * TO_RADIAN)
					* EARTH_RADIUS / 360));
			double moveLong = r * Math.cos(alpha) / meterPerLong;
			double moveLat = r * Math.sin(alpha) / METER_PER_LATITUDE;
			calcLoc.setLongitude(location.getLongitude() + moveLong);
			calcLoc.setLatitude(location.getLatitude() + moveLat);
			if (calcLoc.getLatitude() > 90) {
				calcLoc.setLatitude(180 - calcLoc.getLatitude());
			} else if (calcLoc.getLatitude() < -90) {
				calcLoc.setLatitude(-180 + calcLoc.getLatitude());
			}
			if (calcLoc.getLongitude() > 180) {
				calcLoc.setLongitude(-360 + calcLoc.getLongitude());
			} else if (calcLoc.getLongitude() < -180) {
				calcLoc.setLongitude(360 + calcLoc.getLongitude());
			}
			configuration.setCoordinate("private_lastlocation",
					Coordinate.getCoordinate(location));
			configuration.setCoordinate("private_lastcalculatedlocation",
					Coordinate.getCoordinate(calcLoc));
			Log.d(TAG, "new Location");
			return calcLoc;
		} else {
			return Coordinate.getLocation(configuration
					.getCoordinate("private_lastcalculatedlocation"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * instanceFromParcel(android.os.Parcel)
	 */
	public AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new RadiusDistance(in);
	}

}
