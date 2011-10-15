// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package mx.mariner;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SettingsDialog extends PreferenceActivity 
{
	private GemfCollection gemfCollection = new GemfCollection();
	//private Context context;
	
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
		Preference chartDownloader = (Preference) this.findPreference("ChartDownloader");
		chartDownloader.setOnPreferenceClickListener(onChartDlClick);
		
	}
	
	private OnPreferenceClickListener onChartDlClick = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			//Toast.makeText(getBaseContext(), "hello", Toast.LENGTH_LONG).show();
			startActivity(new Intent(getBaseContext(), RegionActivity.class));
			return true;
		}
		
	};
	

}
