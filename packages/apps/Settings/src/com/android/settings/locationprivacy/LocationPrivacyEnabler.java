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

package com.android.settings.locationprivacy;

import android.content.Context;
import android.locationprivacy.control.LocationPrivacyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;

/**
 * Provides a switch element for enabling and disabling the
 * location privacy framework
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyEnabler {
	private Switch mSwitch;
	private LocationPrivacyManager mLPService;

	private OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			mLPService.setStatus(mSwitch.isChecked());
		}

	};

	/**
	 * Creates new instance of LocationPrivacyEnabler
     *
	 * @param context Context the LocationPrivacyEnabler is running in
	 */
	public LocationPrivacyEnabler(Context context) {
		mLPService = new LocationPrivacyManager(context);
	}

	/**
	 * Initializes the switch
	 */
	public void resume() {
		if (mSwitch != null) {
			mSwitch.setChecked(mLPService.getStatus());
			mSwitch.setOnClickListener(listener);
		}

	}

	/**
	 * Sets a new switch and initializes it.
     *
	 * @param aSwitch new switch
	 */
	public void setSwitch(Switch aSwitch) {
		mSwitch = aSwitch;
		resume();
	}

}
