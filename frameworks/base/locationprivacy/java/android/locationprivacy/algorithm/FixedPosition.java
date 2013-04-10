package android.locationprivacy.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;

/**
 * FixedPosition verschleiert den Standort indem eine feste, vom Benutzer festgelegter, Standort zurückgegeben wird. 
 */
public class FixedPosition extends AbstractLocationPrivacyAlgorithm {
	
	/** The Constant NAME. */
	private static final String NAME = "fixedposition";

	/**
	 * Instanziiert eine neues FixedPosition-Objekt.
	 * 
	 */
	public FixedPosition() {
		super(NAME);
	}
	
	/**
	 * Instanziiert eine neues FixedPosition-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt, dass die Konfiguration des Algorithmus enthält.
	 */	
	private FixedPosition(Parcel in) {
		super(in, NAME);
	}


	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#newInstance()
	 */
	@Override
	public AbstractLocationPrivacyAlgorithm newInstance() {
		return new FixedPosition();
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#getDefaultConfiguration()
	 */
	@Override
	public LocationPrivacyConfiguration getDefaultConfiguration() {
		Map<String, Coordinate> coordinateValues = new HashMap<String, Coordinate>();
		coordinateValues.put("position", new Coordinate(0, 0, 0));
		return new LocationPrivacyConfiguration(new HashMap<String, Integer>(),
				new HashMap<String, Double>(), new HashMap<String, String>(),
				new HashMap<String, ArrayList<String>>(),
				new HashMap<String, String>(),
				coordinateValues ,
				new HashMap<String, Boolean>());
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#calculateLocation(android.location.Location)
	 */
	@Override
	public Location calculateLocation(Location location) {
		Coordinate coord = configuration.getCoordinate("position");
		return Coordinate.getLocation(coord, location);
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#instanceFromParcel(android.os.Parcel)
	 */
	@Override
	public AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new FixedPosition(in);
	}

}
