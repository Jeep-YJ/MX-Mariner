// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import mx.mariner.marks.WaypointDbFunctions;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Dialog;
import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class WaypointDialog extends Dialog {
    private MapActivity mapActivity;
    private int iconindex = 0;
    private Drawable icon;
    
    public WaypointDialog(MapActivity context, final GeoPoint point, final int pointIndex) {
        super(context);
        mapActivity = context;
        setContentView(R.layout.waypointdialog);
        
        icon = mapActivity.waypointMarkerIcons.getDrawables().get(0); 
        
        final Spinner iconSpinner = (Spinner) this.findViewById(R.id.wpticonspinner);
        iconSpinner.setAdapter(new ImageTextSpinnerArrayAdapter(mapActivity, R.layout.waypointspinner, mapActivity.waypointMarkerIcons.getNames()));
        iconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                    iconindex = arg2;
                    icon = mapActivity.waypointMarkerIcons.getDrawables().get(iconindex);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                //Auto-generated method stub
            }
        });
        
        final int latitude = point.getLatitudeE6();
        final int longitude = point.getLongitudeE6();
        
        final EditText nameText = (EditText) this.findViewById(R.id.wptnametext);
        final EditText descText = (EditText) this.findViewById(R.id.wptdesctext);
        final EditText latText = (EditText) this.findViewById(R.id.wptlatitude);
        final Spinner latSpin = (Spinner) this.findViewById(R.id.wptlatspinner);
        latText.setFilters(new InputFilter[]{ new InputFilterMinMax(0f, 90f)});
        if (latitude < 0) {
            latSpin.setSelection(1); //Nort
            latText.setText(String.valueOf(-latitude / 1000000f));
        } else
            latText.setText(String.valueOf(latitude / 1000000f));
        final EditText lonText = (EditText) this.findViewById(R.id.wptlongitude);
        final Spinner lonSpin = (Spinner) this.findViewById(R.id.wptlonspinner);
        lonText.setFilters(new InputFilter[]{ new InputFilterMinMax(0f, 180)});
        if (longitude < 0) {
            lonSpin.setSelection(1); //West
            lonText.setText(String.valueOf(-longitude / 1000000f));
        } else
            lonText.setText(String.valueOf(longitude / 1000000f));
        final CheckBox delete = (CheckBox) this.findViewById(R.id.wptdeletecheck);

        
        if (pointIndex != -1) {
            this.setTitle("Edit Waypoint");
            delete.setVisibility(View.VISIBLE);
            final ContentValues details = WaypointDbFunctions.getWaypointDetailsFromDb(mapActivity.featuresDb, latitude, longitude);
            nameText.setText(details.getAsString("name"));
            descText.setText(details.getAsString("desc"));
            iconSpinner.setSelection(mapActivity.waypointMarkerIcons.findPositoinByName(details.getAsString("sym")));
        } else {
            this.setTitle("New Waypoint");
        }
        
        final Button okButton = (Button) this.findViewById(R.id.wptok);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //delete old point if this is not a new point
                if (pointIndex != -1) {
                    WaypointDbFunctions.deleteWaypointFromDb(mapActivity.featuresDb, latitude, longitude);
                    mapActivity.waypointOverlay.removeItem(pointIndex);
                }
                
                //now (re)create a new one
                if (!delete.isChecked()) {
                    final String name = nameText.getText().toString();
                    final String desc = descText.getText().toString();
                    final String sym = mapActivity.waypointMarkerIcons.getNames()[iconindex];
                    int lat = (int) (Float.valueOf(latText.getText().toString()) * 1000000f);
                    int lon = (int) (Float.valueOf(lonText.getText().toString()) * 1000000f);
                    if (latSpin.getSelectedItemPosition() == 1) {
                        lat = -lat;
                    }
                    if (lonSpin.getSelectedItemPosition() == 1) {
                        lon = -lon;
                    }
                    GeoPoint point = new GeoPoint(lat, lon);
                    
                    //add to db with lat,long from new point since last decimal could have been rounded
                    WaypointDbFunctions.addWayPointToDb(mapActivity.featuresDb, name, desc, sym, 
                            point.getLatitudeE6(), point.getLongitudeE6());
                    
                    OverlayItem overlayItem = new OverlayItem(name, "", point);
                    overlayItem.setMarker(icon);
                    mapActivity.waypointOverlay.addItem(overlayItem);
                    mapActivity.mapController.setCenter(point);
                    mapActivity.mapView.invalidate();
                    //Log.i("MXM", "New Waypoint name:"+name+" description:"+desc);
                }
                mapActivity.mapView.invalidate();
                WaypointDialog.this.dismiss();
            }
        });
        
        final Button wptCancel = (Button) findViewById(R.id.wptcancel);
        wptCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });
     
    }

    public WaypointDialog(MapActivity context, boolean cancelable,
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    
    public class ImageTextSpinnerArrayAdapter extends ArrayAdapter<String> {
        public ImageTextSpinnerArrayAdapter(MapActivity context, int textViewResourceId, String[] iconNames) {
            super(context, textViewResourceId, iconNames);
        }
        
        @Override
        public View getDropDownView(int position, View convertView,
        ViewGroup parent) {
        return getCustomView(position, convertView, parent);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
        }
        
        public View getCustomView(int position, View convertView, ViewGroup parent) {
         LayoutInflater inflater = getLayoutInflater();
         
         View row=inflater.inflate(R.layout.waypointspinner, parent, false);
         
         TextView label = (TextView)row.findViewById(R.id.wpticonspinnername);
         label.setText(mapActivity.waypointMarkerIcons.getNames()[position]);
         
         ImageView icon = (ImageView)row.findViewById(R.id.wpticonspinnericon);
         //icon.setBackgroundDrawable(mapActivity.waypointMarkerIcons.getDrawables().get(position));
         icon.setBackgroundResource(mapActivity.waypointMarkerIcons.getRids()[position]);
         
         return row;
         }

    }
    
    

}


