package android.locationprivacy.algorithm;

import java.util.ArrayList;
import java.util.HashMap;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;

public class TestAlgorithm extends AbstractLocationPrivacyAlgorithm{
	private static final String NAME = "testalgorithm";	
	
	public TestAlgorithm() {
		super(NAME);
	}
	
	private TestAlgorithm(Parcel in) {
		super(in, NAME);
	}

	public LocationPrivacyConfiguration getDefaultConfiguration() {
		HashMap<String, Integer> intValues = new HashMap<String, Integer>();
		intValues.put("integer", 1);
		HashMap<String, Double> doubleValues = new HashMap<String, Double>();
		doubleValues.put("double", 3.5);
		HashMap<String, String> stringValues = new HashMap<String, String>();
		stringValues.put("string", "Hallo");
		HashMap<String, ArrayList<String>> enumValues = new HashMap<String, ArrayList<String>>();
		HashMap<String, String> enumChoosen = new HashMap<String, String>();
		ArrayList<String> enumTest = new ArrayList<String>();
		enumTest.add("christian");
		enumTest.add("benjamin");
		enumValues.put("enum", enumTest);
		enumChoosen.put("enum", "Christian");
		HashMap<String, Coordinate> coordinateValues = new HashMap<String, Coordinate>();
		coordinateValues.put("coordinate", new Coordinate(52, 52, 0));
		HashMap<String, Boolean> booleanValues = new HashMap<String, Boolean>();
		booleanValues.put("boolean", true);
		
		return new LocationPrivacyConfiguration(intValues, doubleValues, stringValues, enumValues, enumChoosen, coordinateValues, booleanValues);
	}

	public Location calculateLocation(Location location) {
		Location newLoc = new Location(location);
		newLoc.setLatitude(configuration.getInt("integer") + location.getLatitude());
		newLoc.setLongitude(configuration.getDouble("double") + location.getLongitude());
		return newLoc;
	}

	public TestAlgorithm newInstance() {
		return new TestAlgorithm();
	}

	public AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in){
		return new TestAlgorithm(in);
	}

}
