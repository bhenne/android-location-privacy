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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.locationprivacy.model.Coordinate;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.settings.R;
import com.mapquest.android.maps.AnnotationView;
import com.mapquest.android.maps.DefaultItemizedOverlay;
import com.mapquest.android.maps.GeoPoint;
import com.mapquest.android.maps.ItemizedOverlay;
import com.mapquest.android.maps.MapActivity;
import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.Overlay;
import com.mapquest.android.maps.OverlayItem;

/**
 * LocationPrivacyMap provides a map to select coordinates
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyMap extends MapActivity implements OnClickListener {

	private AutoCompleteTextView searchAddress;
	private ImageButton submitSearch;
	private Button ok;
	private Button cancel;
	private MapView map;
	/** marker showing select coordinates */
	private AnnotationView annotation;
	private boolean showPoiInfo = false;
	private DefaultItemizedOverlay poiOverlay;
	private Geocoder geocoder;
	private GeoPoint geopoint;
	private String key;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locationprivacy_coordinate);

		Bundle extras = getIntent().getExtras();
		key = extras.getString("key");
		Coordinate coordinate = extras.getParcelable("coordinate");
		geopoint = new GeoPoint(coordinate.getLatitude(),
				coordinate.getLongitude());

		searchAddress = (AutoCompleteTextView) findViewById(R.id.lp_searchaddress);
		map = (MapView) findViewById(R.id.lp_map);
		ok = (Button) findViewById(R.id.lp_ok);
		cancel = (Button) findViewById(R.id.lp_cancel);
		submitSearch = (ImageButton) findViewById(R.id.lp_submitsearch);

		Drawable iconPosition = getResources().getDrawable(
				R.drawable.ic_settings_location);
		Drawable icon = getResources().getDrawable(R.drawable.point);

		submitSearch.setImageDrawable(iconPosition);

		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);
		submitSearch.setOnClickListener(this);

		searchAddress.setAdapter(new AutoCompleteAdapter(this));

		poiOverlay = new DefaultItemizedOverlay(icon);
		map.getOverlays().add(new TapOverlay());
		map.getOverlays().add(poiOverlay);

		annotation = new AnnotationView(map);
		annotation.getTitle().setMaxLines(2);
		annotation.getTitle().setTextSize(19);
		annotation.getSnippet().setTextSize(19);

		map.getController().setZoom(10);
		map.getController().setCenter(geopoint);
		map.setBuiltInZoomControls(true);

		geocoder = new Geocoder(this, Locale.getDefault());
		setPOI(geopoint);

	}

	@Override
	public boolean isRouteDisplayed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v == ok) {
			Intent i = new Intent();
			Coordinate coordinate = new Coordinate(geopoint.getLongitude(),
					geopoint.getLatitude());
			i.putExtra("key", key);
			i.putExtra("coordinate", coordinate);
			setResult(1, i);
			finish();
		} else if (v == cancel) {
			setResult(0);
			finish();
		} else if (v == submitSearch) {
			String address = searchAddress.getText().toString();
			List<Address> positions = null;
			try {
				positions = geocoder.getFromLocationName(address, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (positions != null && positions.size() > 0) {
				Address newPosition = positions.get(0);
				GeoPoint geo = new GeoPoint(newPosition.getLatitude(),
						newPosition.getLongitude());
				map.getController().setCenter(geo);
				setPOI(geo);
			}
		}

	}

	/**
     * Set new coordinates. Address information is gathered and shown on the map.
	 *
	 * @param geo
	 *            new coordinates
	 */
	public void setPOI(GeoPoint geo) {
		geopoint = geo;
		poiOverlay.clear();
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(geo.getLatitude(),
					geo.getLongitude(), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Address nextAddress = null;
		String street = "";
		String city = "";
		if (addresses != null && addresses.size() > 0) {
			nextAddress = addresses.get(0);
			if (nextAddress.getThoroughfare() != null) {
				street = nextAddress.getThoroughfare();
				if (street.length() > 18) {
					street = street.substring(0, 18);
				}
				if (nextAddress.getSubThoroughfare() != null) {
					street += " " + nextAddress.getSubThoroughfare();
				}
			}
			if (nextAddress.getPostalCode() != null) {
				city = nextAddress.getPostalCode();
			}
			if (nextAddress.getLocality() != null) {
				if (city.length() > 0) {
					city += " " + nextAddress.getLocality();
				} else {
					city += nextAddress.getLocality();
				}
			}
			if (nextAddress.getCountryName() != null) {
				if (city.length() > 0) {
					city += ", " + nextAddress.getCountryName();
				} else {
					city += nextAddress.getCountryName();
				}
			}

		}

		OverlayItem poi = new OverlayItem(geo, street, city);

		poiOverlay.addItem(poi);
		poiOverlay.setTapListener(new ItemizedOverlay.OverlayTapListener() {
			@Override
			public void onTap(GeoPoint pt, MapView mapView) {
				int lastTouchedIndex = poiOverlay.getLastFocusedIndex();
				if (lastTouchedIndex > -1) {
					showPoiInfo = !showPoiInfo;
					if (showPoiInfo) {
						OverlayItem tapped = poiOverlay
								.getItem(lastTouchedIndex);
						annotation.showAnnotationView(tapped);
					} else {
						annotation.hide();
					}

				}
			}
		});

	}

	/**
	 * @author Nicolas Klein
	 *         (http://android.foxykeep.com/dev/how-to-add-autocompletion-to-an-edittext)
	 */
	private class AutoCompleteAdapter extends ArrayAdapter<Address> implements
			Filterable {

		private LayoutInflater inflater;
		private Geocoder geocoder;

		public AutoCompleteAdapter(Context context) {
			super(context, -1);
			inflater = LayoutInflater.from(context);
			geocoder = new Geocoder(context);
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView row = null;
			row = (TextView) inflater.inflate(
					android.R.layout.simple_dropdown_item_1line, parent, false);
			row.setText(adressToString(getItem(position)));
			return row;
		}

		/**
		 * @author Christian Kater
         * format postal address to typical German formatting.
		 * @param address address to be formated
		 * @return address as typically used in Germany
		 */
		public String adressToString(Address address) {
			String postal = address.getPostalCode();
			String street = address.getThoroughfare();
			String streetNumber = address.getSubThoroughfare();
			String city = address.getLocality();
			String country = address.getCountryName();
			String addressString = "";
			if (street != null) {
				addressString += street;
				if (streetNumber != null) {
					addressString += " " + streetNumber + ", ";
				} else {
					addressString += ", ";
				}
			}
			if (city != null) {
				if (postal != null) {
					addressString += postal + " ";
				}
				addressString += city + ", ";
			}
			if (country != null) {
				addressString += country;
			} else if (addressString.endsWith(", ")) {
				addressString.substring(0, addressString.length() - 2);
			}
			return addressString;
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					clear();
					List<Address> addresses = (List<Address>) results.values;
					for (Address address : addresses) {
						add(address);
					}
					if (addresses.size() > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					List<Address> addresses = null;
					if (constraint != null) {
						try {
							addresses = geocoder.getFromLocationName(
									(String) constraint, 10);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (addresses == null) {
						addresses = new ArrayList<Address>();
					}
					FilterResults results = new FilterResults();
					results.values = addresses;
					results.count = addresses.size();
					return results;
				}

				public CharSequence convertResultToString(
						final Object resultValue) {
					return resultValue == null ? ""
							: adressToString(((Address) resultValue));
				}

			};
			return filter;
		}

	}

	/**
	 * TapOverlay acts on tapping on the map
     *
	 * @author Christian Kater
	 *
	 */
	private class TapOverlay extends Overlay {

		/**
		 * Update selected coordinate
		 */
		@Override
		public boolean onTap(GeoPoint geo, MapView mapView) {
			LocationPrivacyMap.this.setPOI(geo);
			return super.onTap(geo, mapView);
		}

	}

}
