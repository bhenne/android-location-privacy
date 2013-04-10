package com.android.settings.locationprivacy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.locationprivacy.control.LocationPrivacyManager;
import android.locationprivacy.model.LocationPrivacyApplication;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.settings.R;

/**
 * Die Klasse LocationPrivacyAppPreference ist eine modifizierte Form von
 * SwitchPreference. Im gegensatz zum SwitchPreference wird der Hintergrund der
 * angezeigten Zeile mit einem OnClickListener überlagert.
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyAppPreference extends SwitchPreference implements
		OnClickListener, OnPreferenceChangeListener {

	private LocationPrivacyApplication app;
	private LocationPrivacySettings settings;
	private LocationPrivacyManager lpManager;

	/**
	 * Erzeugt ein neues LocationPrivacyAppPreference-Objekt
	 * 
	 * @param context
	 *            Context in dem das Objekt ausgeführt wird.
	 * @param app
	 *            Anwendung, die dargestellt werden soll.
	 * @param settings
	 *            LocationPrivacySettings-Objekt in dem die Anwendung
	 *            dargestellt werden soll.
	 */
	public LocationPrivacyAppPreference(Context context,
			LocationPrivacyApplication app, LocationPrivacySettings settings) {
		super(context);
		this.app = app;
		lpManager = new LocationPrivacyManager(context);
		this.settings = settings;
		this.setOnPreferenceChangeListener(this);
		this.setTitle(app.getName());
		Resources r = context.getResources();
		if (app.isDefaultAlgorithm()) {
			// ToDo R.string
			this.setSummary(R.string.lp_defaultalgo);
		} else {
			String text = "lp_" + app.getAlgorithm().getName();
			int resIdText = r.getIdentifier(text, "string",
					"com.android.settings");
			this.setSummary(resIdText);
		}

		this.setChecked(app.isEnabled());
		this.setEnabled(lpManager.getStatus());
	}

	protected void onBindView(View view) {
		super.onBindView(view);
		view.setOnClickListener(this);
	}

	/**
	 * Reagiert, sobald auf den Hintergrund oder dem Namen der Anwendung bzw.
	 * dem Algorithmus geklickt wurde.
	 * 
	 * @param v
	 *            View-Element das angeklickt wurde.
	 */
	public void onClick(View v) {
		Bundle extras = new Bundle();
		extras.putParcelable("app", app);
		v.setSelected(true);
		// v.setBackgroundColor(android.R.color.holo_blue_light);
		v.setBackgroundColor(Color.rgb(6, 128, 170));
		settings.startFragment(settings,
				LocationPrivacyAppSetteings.class.getName(), 0, extras);

	}

	/**
	 * Speichert die Änderung des Switch-Elements in die Datenbank. 
	 * @param preference LocationPrivacyAppPreference-Objekt
	 * @param newValue Neuer Zustand des Switch-Objektes
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Boolean on = (Boolean) newValue;
		app.setEnabled(on);
		lpManager.setApplication(app);
		return true;
	}

}
