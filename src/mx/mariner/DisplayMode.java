// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class DisplayMode extends Dialog {
    MapActivity mapActivity;
    private boolean init1 = true;
    private boolean init2 = true;

    public DisplayMode(final MapActivity ctx, final String[] regionItems) {
        super(ctx); //create default dialog window
        mapActivity = ctx;
        this.setContentView(R.layout.displaydialog);
        
        Button okButton = (Button) findViewById(R.id.buttonOK);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ok();
            }
        });
        
        Spinner brightness = (Spinner) findViewById(R.id.spinnerBrightness);
        brightness.setSelection(mapActivity.dayDuskNight);
        brightness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (!init1) { //keep from executing twice
                    mapActivity.dayDuskNight = arg2;
                    mapActivity.editor.putInt("DDN", arg2);
                    mapActivity.setBrightMode();
                } else
                    init1 = false;
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                //Auto-generated method stub
            }     
        });
        
        Spinner regionSpinner = (Spinner) findViewById(R.id.spinnerRegion);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mapActivity, 
                android.R.layout.simple_spinner_item, regionItems);
        regionSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setSelection( Search(regionItems, mapActivity.prefs.getString("PrefChartLocation", "None")) );
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (!init2) { //keep from executing twice
                    mapActivity.editor.putString("PrefChartLocation", regionItems[arg2]);
                    mapActivity.editor.commit();
                    mapActivity.refreshChartLayer();
                }
                else
                    init2 = false;
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                //Auto-generated method stub
            }     
        });
        
        ToggleButton toggleChart = (ToggleButton) findViewById(R.id.toggleChart);
        toggleChart.setChecked(mapActivity.prefs.getBoolean("UseChartOverlay", true));
        toggleChart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mapActivity.editor.putBoolean("UseChartOverlay", arg1);
                mapActivity.editor.commit();
                mapActivity.refreshChartLayer();
            }
        });
        
    }
    
    public void Ok() {
        this.dismiss();
    }
    
    private int Search(String[] arg0, String arg1) {
        for (int i=0; i<arg0.length; i++ ) {
            if ( arg0[i].equals(arg1) )
                return i;
        }
        return 0;
    }
    
}
