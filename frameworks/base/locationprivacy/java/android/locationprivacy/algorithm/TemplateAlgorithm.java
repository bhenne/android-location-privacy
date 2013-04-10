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

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;

/**
 * Template for algorithms
 *
 * @author Christian Kater
 *
 */
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
		 * Setup configuration here
		 */
		return null;
	}

	@Override
	public Location calculateLocation(Location location) {
		/*
		 * Implement obfuscation here 
		 */
		return null;
	}

	@Override
	protected AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new TemplateAlgorithm(in);
	}

}
