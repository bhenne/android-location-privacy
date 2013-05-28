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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

/**
 * Provides a switch element for enabling and disabling the location privacy
 * framework
 * 
 * @author Christian Kater
 */
public class LocationPrivacyEnabler implements OnCheckedChangeListener {
    private Switch mSwitch;
    private LocationPrivacyManager mLPService;

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
            mSwitch.setOnCheckedChangeListener(this);
        }
    }

    /**
     * Sets a new switch and initializes it.
     * 
     * @param switch_ new switch
     */
    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_)
            return;
        if (mSwitch != null) {
            mSwitch.setOnCheckedChangeListener(null);
        }
        mSwitch = switch_;
        resume();
    }

    public void pause() {
        if (mSwitch != null) {
            mSwitch.setOnCheckedChangeListener(null);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mLPService.setStatus(isChecked);
    }

}
