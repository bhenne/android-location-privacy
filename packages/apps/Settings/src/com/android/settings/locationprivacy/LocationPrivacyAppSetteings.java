package com.android.settings.locationprivacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.locationprivacy.control.LocationPrivacyManager;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyApplication;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Die Klasse LocationPrivacyAppSetteings realisiert die GUI für die Zuweisung
 * von Anwendung zu AbstractLocationPrivacyAlgorithm und
 * LocationPrivacyConfiguration.
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyAppSetteings extends SettingsPreferenceFragment
		implements OnPreferenceChangeListener, OnClickListener,
		OnPreferenceClickListener {
	private Resources r;

	private LocationPrivacyApplication app;
	private LocationPrivacyConfiguration config;
	private LocationPrivacyManager lpManager;
	private List<String> lpAlgorithm;
	/*
	 * Zuordnung Preference zu Name und Typ des Wertes der
	 * LocationPrivacyConfiguration
	 */
	private HashMap<Preference, String[]> prefToKey;
	private ListPreference algorithm;

	/**
	 * Erzeugt die einzelnen Preference-Objekte für die Auswahl des
	 * AbstractLocationPrivacyAlgorithm und der Parameter der
	 * LocationPrivacyConfiguration. Der Bildschirm wird gelöscht und ihm werden
	 * diese Objekte zugeordnet.
	 */
	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceScreen();
		if (root != null) {
			root.removeAll();
		}
		addPreferencesFromResource(R.xml.lp_app);
		root = getPreferenceScreen();

		CharSequence[] entries = new CharSequence[lpAlgorithm.size()];
		CharSequence[] values = new CharSequence[lpAlgorithm.size()];
		r = getActivity().getResources();
		for (int i = 0; i < lpAlgorithm.size(); i++) {
			String text = "lp_" + lpAlgorithm.get(i);
			int resIdText = r.getIdentifier(text, "string",
					"com.android.settings");
			entries[i] = r.getText(resIdText);
			values[i] = lpAlgorithm.get(i);
		}
		algorithm = new ListPreference(getActivity());
		algorithm.setEntries(entries);
		algorithm.setEntryValues(values);
		algorithm.setTitle(R.string.lp_algo);
		algorithm.setSummary(R.string.lp_algo_summary);
		algorithm.setOnPreferenceChangeListener(this);
		algorithm.setEnabled(app.isEnabled());
		if (app.isDefaultAlgorithm()) {
			algorithm.setDefaultValue("default");
		} else {
			algorithm.setDefaultValue(app.getAlgorithm().getName());
		}

		root.addPreference(algorithm);

		if (!app.isDefaultAlgorithm()) {
			PreferenceCategory configurationCategory = new PreferenceCategory(
					getActivity());
			configurationCategory.setTitle(R.string.lp_configuration);
			root.addPreference(configurationCategory);

			Map<String, Integer> intValues = config.getIntValues();
			for (String key : intValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getIntegerPreference(key,
							intValues.get(key)));
				}
			}

			Map<String, Double> doubleValues = config.getDoubleValues();
			for (String key : doubleValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getDoublePreference(key,
							doubleValues.get(key)));
				}
			}

			Map<String, String> stringValues = config.getStringValues();
			for (String key : stringValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getStringPreference(key,
							stringValues.get(key)));
				}
			}

			Map<String, ArrayList<String>> enumValues = config.getEnumValues();
			Map<String, String> enumChoosen = config.getEnumChoosen();
			for (String key : enumValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getEnumPreference(key,
							enumValues.get(key), enumChoosen.get(key)));
				}
			}

			Map<String, Boolean> booleanValues = config.getBooleanValues();
			for (String key : booleanValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getBooleanPreference(key,
							booleanValues.get(key)));
				}
			}
			Map<String, Coordinate> coordinateValues = config
					.getCoordinateValues();
			for (String key : coordinateValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getCoordinatePreference(key,
							coordinateValues.get(key)));
				}
			}

		}
		return root;

	}

	/**
	 * @see SettingsPreferenceFragment#onStart()
	 * 
	 *      Erzeugt den Hauptbildschirm und das Switch-Objket im Header
	 */
	public void onStart() {
		super.onStart();
		lpAlgorithm = LocationPrivacyManager.getAllAlgorithm();
		Bundle extras = getArguments();
		app = (LocationPrivacyApplication) extras.getParcelable("app");
		config = app.getAlgorithm().getConfiguration();
		prefToKey = new HashMap<Preference, String[]>();
		Activity activity = getActivity();
		lpManager = new LocationPrivacyManager(getActivity());
		Switch actionBarSwitch = new Switch(activity);
		actionBarSwitch.setOnClickListener(this);
		actionBarSwitch.setChecked(app.isEnabled());

		final int padding = activity.getResources().getDimensionPixelSize(
				R.dimen.action_bar_switch_padding);
		actionBarSwitch.setPadding(0, 0, padding, 0);
		activity.getActionBar().setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		activity.getActionBar().setCustomView(
				actionBarSwitch,
				new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
						ActionBar.LayoutParams.WRAP_CONTENT,
						Gravity.CENTER_VERTICAL | Gravity.RIGHT));
		activity.getActionBar().setTitle(app.getName());
		createPreferenceHierarchy();
	}

	@Override
	public void onStop() {
		lpManager.close();
		lpManager = null;
		super.onStop();
	}

	@Override
	public void onResume() {
		lpManager = new LocationPrivacyManager(getActivity());
		super.onResume();
	}

	/**
	 * Ändert sich der Wert eines Preference-Objektes wird diese Methode
	 * aufgerufen. Über den LocationPrivacyManager werden die neuen Werte in die
	 * Datenbank geschrieben.
	 * 
	 * @param preference
	 *            Verändertes Preference-Objekt
	 * @param newValue
	 *            neue Wert des Preference-Objekt
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == algorithm) {
			String algorithmName = (String) newValue;
			System.out.println("preference == algorithm and newValue = "
					+ algorithmName);
			if (algorithmName.equals("default")) {
				app.setDefaultAlgorithm(true);
				System.out.println("app.setDefault(true)");
				app.setAlgorithm(lpManager.getDefaultAlgorithm());
			} else {
				app.setDefaultAlgorithm(false);
				app.setAlgorithm(LocationPrivacyManager
						.getAlgorithm(algorithmName));
			}
			lpManager.setApplication(app);
			System.out.println(app);
			config = app.getAlgorithm().getConfiguration();
			prefToKey = new HashMap<Preference, String[]>();
			createPreferenceHierarchy();
		} else {
			String[] extras = prefToKey.get(preference);
			String type = extras[1];
			String key = extras[0];
			System.out.println("Type " + type);
			System.out.println("Key " + key);
			System.out.println("new Value" + newValue);

			if (type.equals("int")) {
				config.setInt(key, Integer.parseInt((String) newValue));
			} else if (type.equals("double")) {
				config.setDouble(key, Double.parseDouble((String) newValue));
			} else if (type.equals("string")) {
				config.setString(key, (String) newValue);
			} else if (type.equals("enum")) {
				config.setEnumChoosen(key, (String) newValue);
			} else if (type.equals("boolean")) {
				config.setBoolean(key, (Boolean) newValue);
			} else {
				return false;
			}
			AbstractLocationPrivacyAlgorithm algorithm = app.getAlgorithm();
			algorithm.setConfiguration(config);
			app.setAlgorithm(algorithm);
			lpManager.setApplication(app);
		}
		return true;
	}

	/**
	 * Wechselt bei Klick auf das Switch-Element den Zustand der Anwendung
	 */
	public void onClick(View v) {
		app.setEnabled(!app.isEnabled());
		lpManager.setApplication(app);
		createPreferenceHierarchy();
	}

	/**
	 * Startet die Activity zum Einstellen einer Koordinate. 
	 */
	public boolean onPreferenceClick(Preference preference) {
		String[] extras = prefToKey.get(preference);
		String key = extras[0];
		Coordinate coord = config.getCoordinate(key);
		Intent i = new Intent(getActivity(), LocationPrivacyMap.class);
		i.putExtra("key", key);
		i.putExtra("coordinate", coord);
		startActivityForResult(i, 0);
		return true;
	}

	/**
	 * Fängt die Rückgabe der MapActivtiy ab. Die neue Koordinate wird in der Datenbank gespeichert. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == 1) {
				Coordinate newCoordinate = data
						.getParcelableExtra("coordinate");
				String key = data.getStringExtra("key");
				config.setCoordinate(key, newCoordinate);
				AbstractLocationPrivacyAlgorithm algorithm = app.getAlgorithm();
				algorithm.setConfiguration(config);
				app.setAlgorithm(algorithm);
				lpManager.setApplication(app);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Gibt die Ressourcen-Id zum Titels der Preference-Objekte zurück
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @return ID Ressourcen-Id
	 */
	private int getTitleId(String key) {
		String title = "lp_" + app.getAlgorithm().getName() + "_" + key;
		return r.getIdentifier(title, "string", "com.android.settings");
	}

	/**
	 * Gibt die Ressourcen-Id zur Beschreibung der Preference-Objekte zurück
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @return the Ressourcen-Id
	 */
	private int getSummaryId(String key) {
		String summary = "lp_" + app.getAlgorithm().getName() + "_" + key
				+ "_summary";
		return r.getIdentifier(summary, "string", "com.android.settings");
	}


	/**
	 * Speichert Zuordnung von Preference zu Parameterschlüssel und Datentyp.
	 * 
	 * @param pref
	 *            Preference
	 * @param key
	 *            Schlüssel des zur Preference gehörenden Parameter der
	 *            Konfiguration
	 * @param type
	 *            Datentyp des Parameters
	 */
	private void setPreferenceExtras(Preference pref, String key, String type) {
		String[] prefExtras = new String[2];
		prefExtras[0] = key;
		prefExtras[1] = type;
		prefToKey.put(pref, prefExtras);
	}

	/**
	 * Erzeugt ein EditTextPreference für Integer-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt. Das Tastaturlayout
	 * und die aktzeptierten Eingaben sind an Integer angepasst.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return EditTextPreference für Integer Parameter
	 */
	private EditTextPreference getIntegerPreference(String key, int value) {
		EditTextPreference intETP = new EditTextPreference(getActivity());

		intETP.setOnPreferenceChangeListener(this);
		intETP.setTitle(getTitleId(key));
		intETP.setSummary(getSummaryId(key));
		intETP.setDefaultValue("" + value);
		intETP.setEnabled(app.isEnabled());

		EditText editText = intETP.getEditText();
		editText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
		editText.setRawInputType(Configuration.KEYBOARD_12KEY);
		editText.setKeyListener(DigitsKeyListener.getInstance(true, false));
		if (key.startsWith("secret_")) {
			editText.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
		}

		setPreferenceExtras(intETP, key, "int");

		return intETP;
	}

	/**
	 * Erzeugt ein EditTextPreference für Double-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt. Das Tastaturlayout
	 * und die aktzeptierten Eingaben sind an Double angepasst.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return EditTextPreference für Double Parameter
	 */
	private EditTextPreference getDoublePreference(String key, double value) {
		EditTextPreference doubleETP = new EditTextPreference(getActivity());

		doubleETP.setOnPreferenceChangeListener(this);
		doubleETP.setTitle(getTitleId(key));
		doubleETP.setSummary(getSummaryId(key));
		doubleETP.setDefaultValue("" + value);
		doubleETP.setEnabled(app.isEnabled());

		EditText editText = doubleETP.getEditText();
		editText.setRawInputType(Configuration.KEYBOARD_12KEY);
		editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		editText.setKeyListener(DigitsKeyListener.getInstance(false, true));
		if (key.startsWith("secret_")) {
			editText.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
		}

		setPreferenceExtras(doubleETP, key, "double");

		return doubleETP;
	}

	/**
	 * Erzeugt ein EditTextPreference für String-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return EditTextPreference für String Parameter
	 */
	private EditTextPreference getStringPreference(String key, String value) {
		EditTextPreference stringETP = new EditTextPreference(getActivity());

		stringETP.setOnPreferenceChangeListener(this);
		stringETP.setTitle(getTitleId(key));
		stringETP.setSummary(getSummaryId(key));
		stringETP.setDefaultValue("" + value);
		stringETP.setEnabled(app.isEnabled());

		EditText editText = stringETP.getEditText();
		if (key.startsWith("secret_")) {
			editText.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
		}

		setPreferenceExtras(stringETP, key, "string");

		return stringETP;
	}

	/**
	 * Erzeugt ein ListPreference für Enum-Parameter. Der zum Schlüssel passende
	 * Titel, sowie die Beschreibung wird angezeigt. Zu den Einträgen werden die
	 * entsprechenden Strings ermittelt und angezeigt.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörende Werte
	 * 
	 * @param choosen
	 *            Als ausgewählt angezeigter Wert
	 * @return ListPreference für Enum-Parameter
	 */
	private ListPreference getEnumPreference(String key, List<String> values,
			String choosen) {
		ListPreference enumLP = new ListPreference(getActivity());

		CharSequence[] eEntries = new CharSequence[values.size()];
		CharSequence[] eValues = new CharSequence[values.size()];
		for (int i = 0; i < values.size(); i++) {
			String enumValueText = "lp_" + app.getAlgorithm().getName() + "_"
					+ key + "_" + values.get(i);
			int resIDEnumValue = r.getIdentifier(enumValueText, "string",
					"com.android.settings");
			eEntries[i] = r.getText(resIDEnumValue);
			eValues[i] = values.get(i);
		}

		enumLP.setOnPreferenceChangeListener(this);
		enumLP.setEntries(eEntries);
		enumLP.setEntryValues(eValues);
		enumLP.setTitle(getTitleId(key));
		enumLP.setSummary(getSummaryId(key));
		enumLP.setPersistent(false);
		enumLP.getEntries();
		enumLP.setDefaultValue(choosen);
		enumLP.setEnabled(app.isEnabled());

		setPreferenceExtras(enumLP, key, "enum");

		return enumLP;
	}

	/**
	 * Erzeugt eine CheckBoxPreference für Boolean-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return CheckBoxPreference für Boolean Parameter
	 */
	private CheckBoxPreference getBooleanPreference(String key, boolean value) {
		CheckBoxPreference booleanCBP = new CheckBoxPreference(getActivity());

		booleanCBP.setOnPreferenceChangeListener(this);
		booleanCBP.setChecked(value);
		booleanCBP.setTitle(getTitleId(key));
		booleanCBP.setSummary(getSummaryId(key));
		booleanCBP.setPersistent(false);
		booleanCBP.setEnabled(app.isEnabled());

		setPreferenceExtras(booleanCBP, key, "boolean");

		return booleanCBP;
	}
	
	/**
	 * Erzeugt eine Preference für Coordinate-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt. Wird auf das
	 * Preference Objekt geklickt wird eine MapActivity zum Einstellen der
	 * Koordinate gestartet.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return CheckBoxPreference für Boolean Parameter
	 */
	private Preference getCoordinatePreference(String key, Coordinate value) {
		Preference coordPref = new Preference(getActivity());
		coordPref.setEnabled(app.isEnabled());
		coordPref.setTitle(getTitleId(key));
		coordPref.setSummary(getSummaryId(key));
		coordPref.setOnPreferenceClickListener(this);

		setPreferenceExtras(coordPref, key, "coordinate");
		return coordPref;
	}
}
