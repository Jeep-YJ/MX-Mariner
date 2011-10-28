// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package mx.mariner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SettingsDialog extends PreferenceActivity 
{
    private GemfCollection gemfCollection;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gemfCollection = new GemfCollection();
        addPreferencesFromResource(R.xml.preferences);
        
        //select chart region
        ListPreference prefChartLocation = (ListPreference) this.findPreference("PrefChartLocation");
        String[] entries = gemfCollection.getFileList();
        prefChartLocation.setEntries(entries);
        prefChartLocation.setEntryValues(entries);
        
        //get new chart regions
        Preference chartDownloader = (Preference) this.findPreference("ChartDownloader");
        chartDownloader.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), RegionActivity.class));
                return true;
            } 
        });
        
        Preference about = (Preference) this.findPreference("About");
        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ShowAbout();
                return true;
            } 
        });
        
        Preference warning = (Preference) this.findPreference("Warning");
        warning.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ShowWarning();
                return true;
            } 
        });
        
        Preference license = (Preference) this.findPreference("License");
        license.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ShowCopyright();
                return true;
            } 
        });
        
    }
    
    public void OnResume() {
        ListPreference prefChartLocation = (ListPreference) this.findPreference("PrefChartLocation");
        gemfCollection = new GemfCollection();
        String[] entries = gemfCollection.getFileList();
        prefChartLocation.setEntries(entries);
        prefChartLocation.setEntryValues(entries);
        super.onResume();
    }
    
    private void ShowAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About MX Mariner");
        builder.setIcon(R.drawable.icon);
        builder.setMessage(getResources().getString(R.string.credits));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }); 
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void ShowWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setIcon(R.drawable.icon);
        builder.setMessage(getResources().getString(R.string.nav_warning));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }); 
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void ShowCopyright() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("License Information");
        builder.setIcon(R.drawable.icon);
        builder.setMessage(getResources().getString(R.string.copyright));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }); 
        AlertDialog alert = builder.create();
        alert.show();
    }
    
}
