// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package mx.mariner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SettingsDialog extends PreferenceActivity 
{
	private GemfCollection gemfCollection;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        gemfCollection = new GemfCollection();
		addPreferencesFromResource(R.xml.preferences);
		ListPreference prefChartLocation = (ListPreference) findPreference("PrefChartLocation");
		String[] entries = gemfCollection.getFileList();
		prefChartLocation.setEntries(entries);
		prefChartLocation.setEntryValues(entries);
		Preference ViewNOAARegionMap = (Preference) findPreference("ViewNOAARegionMap");
		
		
		ViewNOAARegionMap.setOnPreferenceClickListener(onViewNOAARegionMap);
	}
	
	private OnPreferenceClickListener onViewNOAARegionMap = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			ShowNOAARegionMap();
			return false;
		}
		
	};
	
	private void ShowNOAARegionMap()
	{
//		View regionView = findViewById(R.id.noaaregionview);
//		addContentView(regionView, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("NOAA Region Map");
		builder.setIcon(R.drawable.icon);
		//builder.setMessage("");
		//TODO: add region map image here
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				return;
			}
		}); 
		AlertDialog alert = builder.create();
		alert.show();
	}
}
