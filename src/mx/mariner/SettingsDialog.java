// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package mx.mariner;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.ListView;

public class SettingsDialog extends PreferenceActivity 
{
	private GemfCollection gemfCollection = new GemfCollection();
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		//select chart region
		ListPreference prefChartLocation = (ListPreference) this.findPreference("PrefChartLocation");
		String[] entries = gemfCollection.getRegionList();
		prefChartLocation.setEntries(entries);
		prefChartLocation.setEntryValues(entries);
		
		//get new chart regions
		PreferenceScreen chartDownloader = (PreferenceScreen) this.findPreference("PreferenceScreen");
		chartDownloader.setTitle("Chart Region Downloader");
		ListView cdl = (ListView) this.findViewById(R.id.regionLV);
		cdl.setAdapter(adapter);
		
		
//		Preference ViewNOAARegionMap = findPreference("ViewNOAARegionMap");
//		ViewNOAARegionMap.setOnPreferenceClickListener(onViewNOAARegionMap);
	}
	
//	private OnPreferenceClickListener onViewNOAARegionMap = new OnPreferenceClickListener() {
//		@Override
//		public boolean onPreferenceClick(Preference preference) {
//			//do something
//			return false;
//		}
//		
//	};
	

}
