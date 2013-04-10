package android.locationprivacy.algorithm;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;

public class TemplateAlgorithm extends AbstractLocationPrivacyAlgorithm {
	private static final String NAME = "templatealgorithm";
	
	public TemplateAlgorithm() {
		super(NAME);
	}
	
	private TemplateAlgorithm(Parcel in) {
		super(in, NAME);
	}

	@Override
	public AbstractLocationPrivacyAlgorithm newInstance() {
		return new TemplateAlgorithm();
	}

	@Override
	public LocationPrivacyConfiguration getDefaultConfiguration() {
		/*
		 * Hier ist Platz um die Configuration zu definieren. 
		 */
		return null;
	}

	@Override
	public Location calculateLocation(Location location) {
		/*
		 *Hier ist Platz um die eigentliche Berechnung zu implementieren. 
		 */
		return null;
	}

	@Override
	protected AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new TemplateAlgorithm(in);
	}

}
