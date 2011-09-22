// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package mx.mariner;

import android.os.Bundle;
import android.preference.ListPreference;
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
		
//		ListPreference prefChartDownloader = (ListPreference) findPreference("PrefChartDownloader");
//		String[] regionEntries = {"District 1", "District 5", "District 7", "District 8", "District 9", "District 11", "District 13", "District 14", "District 17"};
//		prefChartDownloader.setEntries(regionEntries);
//		prefChartDownloader.setEntryValues(regionEntries);
	}
}
