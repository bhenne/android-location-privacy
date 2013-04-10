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
import java.util.Map;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;

/**
 * The algorithm FixedPosition always returns a fixed location set up by the user
 *
 * @author Christian Kater
 *
 */
public class FixedPosition extends AbstractLocationPrivacyAlgorithm {
	
	/** The Constant NAME. */
	private static final String NAME = "fixedposition";

	/**
	 * Creates new instance of FixedPosition
	 * 
	 */
	public FixedPosition() {
		super(NAME);
	}
	
	/**
	 * Creates new instance of FixedPosition
	 * 
	 * @param in
	 *            Parcel object containing the configuration of the algorithm
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
