// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GEMFFile;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class MapActivity extends Activity {
	
	//====================
    // Constants
	//====================
	
	private static final String tag = "MXM";
	
	//====================
    // Fields
	//====================
	
    private MapController mapController;
    private MapView mapView;
    private Activity mActivity;
    private MxmMyLocationOverlay mLocationOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private ResourceProxy mResourceProxy;
    private int dayDuskNight; //0-day, 1-dusk, 2-night
    private IArchiveFile[] myArchives;
	private MxmBitmapTileSourceBase mBitmapTileSourceBase;
	private MapTileModuleProviderBase[] myProviders;
	private MapTileProviderArray myGemfTileProvider;
	private TilesOverlay myGemfOverlay;
	private SharedPreferences prefs;
	private int start_lat; //geopoint
	private int start_lon; //geopoint
	private int start_zoom;
	private String gemf_file;
	
	//=============================
    // Methods & Getters & Setters
	//=============================
	
	void SetPreferences(SharedPreferences preferences)
	{
		prefs = preferences;
		start_lat = prefs.getInt("Latitude", 0);
		start_lon = prefs.getInt("Longitude", 0);
		start_zoom = prefs.getInt("Zoom", 3);
		dayDuskNight = prefs.getInt("DDN", 0);
		//gemf_file overlay set in onResume
	}
	
	void StorePreferences()
	{
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("Latitude", mapView.getMapCenter().getLatitudeE6());
		editor.putInt("Longitude", mapView.getMapCenter().getLongitudeE6());
		editor.putInt("Zoom", mapView.getZoomLevel());
		editor.putInt("DDN", dayDuskNight);
		editor.commit();
	}

    private View.OnClickListener ZoomInListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			//is there a better way?...zoom during follow is screwed up otherwise
			if (mLocationOverlay.isFollowLocationEnabled()) {
				mLocationOverlay.disableFollowLocation();
				mapController.zoomIn();
			} else
				mapController.zoomIn();
		}
	};
	private View.OnClickListener ZoomOutListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			//is there a better way?...zoom during follow is screwed up otherwise
			if (mLocationOverlay.isFollowLocationEnabled()) {
				mLocationOverlay.disableFollowLocation();
				mapController.zoomOut();
			} else
				mapController.zoomOut();
		}
	};
	
	private View.OnClickListener FollowListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			if (!mLocationOverlay.isFollowLocationEnabled())
				mLocationOverlay.enableFollowLocation();
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
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		switch (level) {
			case 0:
				ddnMask.setBackgroundResource(R.color.day);
		    	layoutParams.screenBrightness = (float) 1.0;
		    	getWindow().setAttributes(layoutParams);
				//Log.i(TAG, "setting to day");
		    	break;
		    	
			case 1:
				ddnMask.setBackgroundResource(R.color.day);
		    	layoutParams.screenBrightness = (float) 0.1;
		    	getWindow().setAttributes(layoutParams);
				//Log.i(TAG, "setting to dusk");
		    	break;
		    	
			case 2:
				ddnMask.setBackgroundResource(R.color.night);
		    	layoutParams.screenBrightness = (float) 0.01;
		    	getWindow().setAttributes(layoutParams);
				//Log.i(TAG, "setting to night");
		    	break;
		}
		
	}
		
	private void ShowAbout()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("About MX Mariner");
		builder.setIcon(R.drawable.follow);
		builder.setMessage("Website:\n" +
						   "MXMariner.com\n\n" +
					       "Credits:\n" +
						   "Lead Developer - Will Kamp\n" +
						   "MatrixMariner.com - manimaul@gmail.com\n\n" +
						   "Special Thanks:\n" +
						   "OSMDroid - Nicolas Gramlich\n" +
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
    
    private void gemfOverlay(String gemfLocation) {
    	File location = new File(gemfLocation);
    	try {
    		//get zoom levels
    		GEMFFile gemfile = new GEMFFile(location);
    		int maxZoom = (Integer) gemfile.getZoomLevels().toArray()[0];
    		int minZoom = (Integer) gemfile.getZoomLevels().toArray()[gemfile.getZoomLevels().size()-1];
    		//set up overlay
    		myArchives = new IArchiveFile[1];
    		myArchives[0] = GEMFFileArchive.getGEMFFileArchive(location);
    		mBitmapTileSourceBase = new MxmBitmapTileSourceBase("test", null, minZoom, maxZoom, 256, ".png");
    		myProviders = new MapTileModuleProviderBase[1];
    		myProviders[0] = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(getApplicationContext()), mBitmapTileSourceBase, myArchives);
    		myGemfTileProvider = new MapTileProviderArray(mBitmapTileSourceBase, null, myProviders);
    		myGemfOverlay = new TilesOverlay(myGemfTileProvider, getApplicationContext());
    		myGemfOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
    		mapView.getOverlays().add(myGemfOverlay);   		
		} catch (FileNotFoundException e) {
			Log.e(tag, e.getMessage());
		} catch (IOException e) {
			Log.e(tag, e.getMessage());
		}
    }
    
    public void fullexit() {
    	int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
    }

	//====================
    // SuperClass Methods
	//====================
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SetPreferences(PreferenceManager.getDefaultSharedPreferences(getBaseContext())); //initial position, zoom, gemf-file
        mapView = (MapView) findViewById(R.id.mapview);
        mActivity = (Activity) this;
        mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(start_zoom);
        mapController.setCenter(new GeoPoint(start_lat, start_lon));
        
        //location overlay setup
    	mLocationOverlay = new MxmMyLocationOverlay(getBaseContext(), mapView, mActivity, mResourceProxy);
    	mLocationOverlay.enableMyLocation();
    	
    	//scale-bar overlay setup
    	mScaleBarOverlay = new ScaleBarOverlay(this);
    	mScaleBarOverlay.setScaleBarOffset(70, 56);
    	mScaleBarOverlay.setLineWidth((float) 3.0);
    	mScaleBarOverlay.setTextSize((float) 25.0);
    	mScaleBarOverlay.setNautical();
    	
    	//buttons
    	Button btnZoomIn = (Button) findViewById(R.id.btnZoomIn);
        Button btnZoomOut = (Button) findViewById(R.id.btnZoomOut);
        Button btnFollow = (Button) findViewById(R.id.btnFollow);
        Button btnDdn = (Button) findViewById(R.id.btnDdn);
        btnZoomIn.setOnClickListener(ZoomInListener);
        btnZoomOut.setOnClickListener(ZoomOutListener);
        btnFollow.setOnClickListener(FollowListener);
        btnDdn.setOnClickListener(DayDustNightListener);
        
        //settings
        mapView.setKeepScreenOn(true);        
        setBrightMode(dayDuskNight);
    }
    
    public void onPause() {
    	StorePreferences();
    	mapView.getOverlays().clear();
    	//mapView.getOverlays().remove(myGemfOverlay);
    	super.onPause();
    }
    
    public void onResume() {
    	gemf_file = "/sdcard/mxmariner/" + prefs.getString("PrefChartLocation", "");
    	gemfOverlay(gemf_file);
    	mapView.getOverlays().add(mLocationOverlay);
    	mapView.getOverlays().add(mScaleBarOverlay);
    	super.onResume();
    }
    
    //android menu button
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
    
    //andriod menu button items
    @Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
			case R.id.settings:
				Log.d(tag, "Showing Settings");
				startActivity(new Intent(this, SettingsDialog.class));
				return true;
			
			case R.id.quit:
				Log.d(tag, "Finishing");
				StorePreferences();
				fullexit();
				return true;
				
			case R.id.about:
				Log.d(tag, "Showing about dialog");
				ShowAbout();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
    
}   