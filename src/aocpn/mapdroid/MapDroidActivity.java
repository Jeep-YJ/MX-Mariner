// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package aocpn.mapdroid;

import aocpn.mapdroid.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MapDroidActivity extends Activity
{
	private MapView mapView;
	private int dayDuskNight = 0; //0-day, 1-dusk, 2-night
	private static final String TAG = "MDA";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBackgroundColor(0xff000000);
		mapView.SetParent(this);
		mapView.SetFollowPosition(true);
		mapView.SetPreferences(PreferenceManager.getDefaultSharedPreferences(getBaseContext()));

		
        Button btnZoomIn = (Button) findViewById(R.id.btnZoomIn);
        Button btnZoomOut = (Button) findViewById(R.id.btnZoomOut);
        Button btnFollow = (Button) findViewById(R.id.btnFollow);
        Button btnDdn = (Button) findViewById(R.id.btnDdn);

        btnZoomIn.setOnClickListener(ZoomInListener);
        btnZoomOut.setOnClickListener(ZoomOutListener);
        btnFollow.setOnClickListener(FollowListener);
        btnDdn.setOnClickListener(DayDustNightListener);

        LocationManager locationManager = (LocationManager)
        	this.getSystemService(Context.LOCATION_SERVICE);
        mapView.SetLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
        		0, 0, LocationChangeListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        		0, 0, LocationChangeListener);
        
        setBrightMode(dayDuskNight);
    }

	public void onFocusChange (View v, boolean hasFocus)
	{
		if ( ! hasFocus)
		{
			mapView.StorePreferences();
		}
	}

	public void UpdateFollowDisplay(boolean bIsFollowing)
	{
		Button btnFollow = (Button) findViewById(R.id.btnFollow);
		if (bIsFollowing)
			btnFollow.setVisibility(View.GONE);
		else
			btnFollow.setVisibility(View.VISIBLE);
	}

	private View.OnClickListener ZoomInListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			mapView.ZoomIn();
		}
	};
	private View.OnClickListener ZoomOutListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			mapView.ZoomOut();
		}
	};

	private View.OnClickListener FollowListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			mapView.SetFollowPosition(true);
		}
	};
	
	private View.OnClickListener DayDustNightListener = new View.OnClickListener() 
	{
		public void onClick(View v)
		{
			dayDuskNight ++;
			if (dayDuskNight>=3)
				dayDuskNight = 0;
			
			setBrightMode(dayDuskNight);
		}
	};
	
	private void setBrightMode(int level) {
		FrameLayout ddnMask = (FrameLayout) findViewById(R.id.ddnMask);
		if (level==0)
		{
			ddnMask.setBackgroundResource(R.color.day);
			WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
	    	layoutParams.screenBrightness = (float) 1.0;
	    	getWindow().setAttributes(layoutParams);
			//Log.i(TAG, "setting to day");
		}
		
		if (level==1)
		{
			ddnMask.setBackgroundResource(R.color.day);
			WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
	    	layoutParams.screenBrightness = (float) 0.1;
	    	getWindow().setAttributes(layoutParams);
			//Log.i(TAG, "setting to dusk");
		}
		
		if (level==2)
		{
			ddnMask.setBackgroundResource(R.color.night);
			WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
	    	layoutParams.screenBrightness = (float) 0.02;
	    	getWindow().setAttributes(layoutParams);
			//Log.i(TAG, "setting to night");
		}
		
	}
	
	private LocationListener LocationChangeListener = new LocationListener()
	{
		
		public void onLocationChanged(Location location)
		{
			mapView.SetLocation(location);
			TextView hud_sog = (TextView) findViewById(R.id.sog);
			hud_sog.setText(String.valueOf((double)Math.round(location.getSpeed()*1.94 * 100) / 100 )+"kts | ");
			TextView hud_cog = (TextView) findViewById(R.id.cog);
			hud_cog.setText(String.valueOf(location.getBearing())+"° ");
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}

		public void onProviderEnabled(String provider)
		{
		}

		public void onProviderDisabled(String provider)
		{
		}
	};
	
	

	private void ShowAbout()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("About AOCPN");
		builder.setIcon(R.drawable.follow);
		builder.setMessage("AOCPN is part of the OpenCPN.org project\n\n" +
					       "Credits:\n" +
						   "Lead Developer - Will Kamp\n" +
						   "MatrixMariner.com - manimaul@gmail.com\n\n" +
						   "Special Thanks:\n" +
						   "OSMDroid\n" +
						   "OpenCPN - Dave Register\n" +
						   "GDAL - Frank Warmerdam\n" +
						   "GDAL Tiler - Klokan Petr Přidal\n" +
						   "Tiler-Tools - Vadim Shlyakhov\n" +
						   "GEMF - Allen Budden\n");
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
			case R.id.settings:
				Log.d(TAG, "Showing Settings");
				startActivity(new Intent(this, SettingsDialog.class));
				return true;

			case R.id.quit:
				Log.d(TAG, "Finishing");
				finish();
				return true;
				
			case R.id.about:
				Log.d(TAG, "Showing about dialog");
				ShowAbout();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
