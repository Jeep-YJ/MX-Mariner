package mx.mariner;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Dialog;
import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class NewWaypointDialog extends Dialog {
    private MapActivity mapActivity;
    private int iconindex = 0;
    private Drawable icon;
    
    protected static final String[] iconNames = {"Green Flag", "Red Flag", "Blue Flag", "Yellow Flag",
        "Anchor", "Camp", "Diver", "Fish", "Food",
        "Grass", "Rock", "Sand", "Shark"};
    protected static final int[] iconIds = {R.drawable.greenflagpt, R.drawable.redflagpt, R.drawable.blueflagpt, R.drawable.yellowflagpt,
        R.drawable.anchorpt, R.drawable.camppt, R.drawable.divept, R.drawable.fishpt, R.drawable.foodpt, 
        R.drawable.grasspt, R.drawable.rockpt, R.drawable.sandpt, R.drawable.sharkpt};
    private final int[] spinnericons = {R.drawable.greenflag, R.drawable.redflag, R.drawable.blueflag, R.drawable.yellowflag,
            R.drawable.anchor, R.drawable.camp, R.drawable.dive, R.drawable.fish, R.drawable.food, 
            R.drawable.grass, R.drawable.rock, R.drawable.sand, R.drawable.shark};

    public NewWaypointDialog(MapActivity context) {
        super(context);
        mapActivity = context;
        setContentView(R.layout.newwaypoint);
        
        icon = mapActivity.getResources().getDrawable(R.drawable.greenflag);
        
        this.setTitle("New Waypoint");
        
        final EditText nameText = (EditText) this.findViewById(R.id.wptnametext);
        final EditText descText = (EditText) this.findViewById(R.id.wptdesctext);
        
        final Button saveButton = (Button) this.findViewById(R.id.wptsave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                GeoPoint point = (GeoPoint) mapActivity.mapView.getMapCenter();
                ContentValues values = new ContentValues(4);
                final String name = nameText.getText().toString();
                final String description = descText.getText().toString();
                values.put("iconindex", iconindex);
                values.put("name", name);
                values.put("description", description);
                values.put("latitude", point.getLatitudeE6());
                values.put("longitude", point.getLongitudeE6());
                //add to database
                mapActivity.featuresDb.insert("waypoints", null, values);
                OverlayItem overlayItem = new OverlayItem(name, description, point);
                overlayItem.setMarker(icon);
                mapActivity.waypointOverlay.addItem(overlayItem);
                mapActivity.mapView.invalidate();
                Log.i("MXM", "New Waypoint name:"+name+" description:"+description);
                NewWaypointDialog.this.dismiss();
            }
        });
        
        final Button wptCancel = (Button) findViewById(R.id.wptcancel);
        wptCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });
     
     
     Spinner iconSpinner = (Spinner) this.findViewById(R.id.wpticonspinner);
     iconSpinner.setAdapter(new ImageTextSpinnerArrayAdapter(mapActivity, R.layout.waypointspinner, iconNames));
     iconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         public void onItemSelected(AdapterView<?> arg0, View arg1,
                 int arg2, long arg3) {
                 iconindex = arg2;
                 icon = mapActivity.getResources().getDrawable(iconIds[iconindex]);
         }
         public void onNothingSelected(AdapterView<?> arg0) {
             //Auto-generated method stub
         }
     });
     
    }

    public NewWaypointDialog(MapActivity context, boolean cancelable,
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
         label.setText(iconNames[position]);
         
         ImageView icon = (ImageView)row.findViewById(R.id.wpticonspinnericon);
         icon.setBackgroundResource(spinnericons[position]);
         
         return row;
         }

    }    
    
    

}


