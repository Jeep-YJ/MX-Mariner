// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package mx.mariner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsDialog extends PreferenceActivity {
    
    Context context;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        context = this;
        
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
                ShowWarning(context);
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
        
        Preference prefDayBright = (Preference) this.findPreference("DayBrightPref");
        prefDayBright.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                BrightnessDialog dialog = new BrightnessDialog(context, (float) 1.0, 20);
                dialog.setTitle("Adjust Day Mode Brightness");
                dialog.show("DayBright");
                return true;
            } 
        });
        
        Preference prefDuskBright = (Preference) this.findPreference("DuskBrightPref");
        prefDuskBright.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                BrightnessDialog dialog = new BrightnessDialog(context, (float) 0.1, 5);
                dialog.setTitle("Adjust Dusk Mode Brightness");
                dialog.show("DuskBright");
                return true;
            } 
        });
        
        Preference prefNightBright = (Preference) this.findPreference("NightBrightPref");
        prefNightBright.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                BrightnessDialog dialog = new BrightnessDialog(context, (float) 0.01, 1);
                dialog.setTitle("Adjust Night Mode Brightness");
                dialog.show("NightBright");
                return true;
            } 
        });
        
        Preference reset = (Preference) this.findPreference("Reset");
        reset.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ConfirmReset();
                return true;
            } 
        });
        
    }
    
    private void ShowAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String version = String.format("Version: %s\n", getResources().getString(R.string.app_version));
        String build = String.format("Build: %s\n\n", getResources().getString(R.string.app_buildno));
        builder.setTitle("About MX Mariner");
        builder.setIcon(R.drawable.icon);
        builder.setMessage(version + build+ getResources().getString(R.string.credits));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }); 
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private static void ShowWarning(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning");
        builder.setIcon(R.drawable.icon);
        builder.setMessage(context.getResources().getString(R.string.nav_warning));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }); 
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void ConfirmReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset backlight levels to default?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Reset();
                return;
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void Reset() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("DayBright", (float) 1.0);
        editor.putFloat("DuskBright", (float) 0.1);
        editor.putFloat("NightBright", (float) 0.05);
        editor.commit();
    }
    
}
