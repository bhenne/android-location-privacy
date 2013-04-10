/*
 * Copyright (C) 2013 Distributed Computing & Security Group,
 *                    Leibniz Universitaet Hannover, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.locationprivacy.algorithm;

import java.util.ArrayList;
import java.util.HashMap;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;

/**
 * The algorithm Deactivate disables location use by returning null
 *
 * @author Christian Kater
 *
 */
public class Deactivate extends AbstractLocationPrivacyAlgorithm {
	
	private static final String NAME = "deactivate";

	/**
	 * Creates new instance of Deactivate
	 * 
	 * @param in
	 *            Parcel object containing the configuration of the algorithm
	 */
	public Deactivate(Parcel in) {
		super(in, NAME);
	}
	
	/**
	 * Creates new instance of Deactivate
	 */
	public Deactivate() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#newInstance()
	 */
	@Override
	public AbstractLocationPrivacyAlgorithm newInstance() {
		return new Deactivate();
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#getDefaultConfiguration()
	 */
	@Override
	public LocationPrivacyConfiguration getDefaultConfiguration() {
		return new LocationPrivacyConfiguration(new HashMap<String, Integer>(),
				new HashMap<String, Double>(), new HashMap<String, String>(),
				new HashMap<String, ArrayList<String>>(),
				new HashMap<String, String>(), new HashMap<String, Coordinate>(),
				new HashMap<String, Boolean>());
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#calculateLocation(android.location.Location)
	 */
	@Override
	public Location calculateLocation(Location location) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#instanceFromParcel(android.os.Parcel)
	 */
	@Override
	protected AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new Deactivate(in);
	}

}
