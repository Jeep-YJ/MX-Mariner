// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GEMFFile;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MapActivity extends Activity {
    
    //====================
    // Constants
    //====================
    
    
    private static final int gemfLayer = 0;
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
    protected int dayDuskNight; //0-day, 1-dusk, 2-night
    private IArchiveFile[] myArchives = new IArchiveFile[1];
    private MxmBitmapTileSourceBase mBitmapTileSourceBase;
    private MapTileModuleProviderBase[] myProviders = new MapTileModuleProviderBase[1];
    private MapTileProviderArray myGemfTileProvider;
    private TilesOverlay myGemfOverlay;
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor;
    private GemfCollection gemfCollection;
    private int start_lat; //geopoint
    private int start_lon; //geopoint
    private int start_zoom;
    protected String region;
    private String regiondir = Environment.getExternalStorageDirectory()+"/mxmariner/";
    private ChartOutlines chartOutlines = new ChartOutlines();
    private Button btnZoomIn;
    private Button btnZoomOut;
    private Button btnFollow;
    private boolean warning;
    
    //====================
    // Methods
    //====================
    
    private void SetPreferences(SharedPreferences preferences) {
        prefs = preferences;
        editor = prefs.edit();
        start_lat = prefs.getInt("Latitude", 0);
        start_lon = prefs.getInt("Longitude", 0);
        start_zoom = prefs.getInt("Zoom", 3);
        warning = prefs.getBoolean("Warning", true);
        //set to day mode always when app starts for screens where night mode is blank
        if (!warning) 
            dayDuskNight = prefs.getInt("DDN", 0);
        else
            dayDuskNight = 0; //
    }
    
    protected void StorePreferences(Boolean warning) {
        //SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("Warning", warning);
        editor.putInt("Latitude", mapView.getMapCenter().getLatitudeE6());
        editor.putInt("Longitude", mapView.getMapCenter().getLongitudeE6());
        editor.putInt("Zoom", mapView.getZoomLevel());
        editor.putInt("DDN", dayDuskNight);
        editor.commit();
    }

    private View.OnClickListener ZoomInListener = new View.OnClickListener() {
        public void onClick(View v) {
            //is there a better way?...zoom during follow is screwed up otherwise
            if (mLocationOverlay.isFollowLocationEnabled()) {
                mLocationOverlay.disableFollowLocation();
                mapController.zoomIn();
            } else
                mapController.zoomIn();
        }
    };
    
    private View.OnClickListener ZoomOutListener = new View.OnClickListener() {
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
    
    private View.OnClickListener FollowListener = new View.OnClickListener() {
        public void onClick(View v)
        {
            if (!mLocationOverlay.isFollowLocationEnabled())
                mLocationOverlay.enableFollowLocation();
        }
    };
    
    private void gemfOverlay(String gemfLocation) {
        File location = new File(gemfLocation+".gemf");
        try {
            //get zoom levels
            GEMFFile gemfile = new GEMFFile(location);
            int maxZoom = (Integer) gemfile.getZoomLevels().toArray()[0];
            int minZoom = (Integer) gemfile.getZoomLevels().toArray()[gemfile.getZoomLevels().size()-1];
            //set up overlay
            //myArchives = new IArchiveFile[1];
            myArchives[0] = GEMFFileArchive.getGEMFFileArchive(location);
            mBitmapTileSourceBase = new MxmBitmapTileSourceBase("test", null, minZoom, maxZoom, 256, ".png");            
            //myProviders = new MapTileModuleProviderBase[1];
            myProviders[0] = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(this), mBitmapTileSourceBase, myArchives);
            myGemfTileProvider = new MapTileProviderArray(mBitmapTileSourceBase, null, myProviders);
            myGemfOverlay = new TilesOverlay(myGemfTileProvider, this);
            myGemfOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            mapView.getOverlays().add(gemfLayer, myGemfOverlay);
        } catch (FileNotFoundException e) {
            Log.e(tag, e.getMessage());
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
        }
    }
    
    protected void toggleChartLayer() {
        if (!prefs.getBoolean("UseChartOverlay", true)) {
            mapView.getOverlays().remove(gemfLayer);
            Toast.makeText(this, "Charts Layer: Off", Toast.LENGTH_SHORT).show();
        } else {
            region = prefs.getString("PrefChartLocation", "None");
            gemfOverlay(regiondir + region);
            Toast.makeText(this, "Charts Layer: On", Toast.LENGTH_SHORT).show();
        }
    }
    
    protected void changeChartRegion(String newRegion) {
        editor.putString("PrefChartLocation", newRegion);
        editor.commit();
        mapView.getOverlays().clear();
        addMapLayers();
    }
    
    protected void setBrightMode() {
        FrameLayout ddnMask = (FrameLayout) findViewById(R.id.ddnMask);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        
        switch (dayDuskNight) {
            case 0:
                ddnMask.setBackgroundResource(R.color.day);
                float day = prefs.getFloat("DayBright", (float) 1.0);
                layoutParams.screenBrightness = day;
                getWindow().setAttributes(layoutParams);
                break;
                
            case 1:
                ddnMask.setBackgroundResource(R.color.day);
                float dusk = prefs.getFloat("DuskBright", (float) 0.1);
                layoutParams.screenBrightness = dusk;
                getWindow().setAttributes(layoutParams);
                //Toast.makeText(this, "Dusk Mode", Toast.LENGTH_SHORT).show();
                break;
                
            case 2:
                ddnMask.setBackgroundResource(R.color.night);
                float night = prefs.getFloat("NightBright", (float) 0.05);
                layoutParams.screenBrightness = night;
                getWindow().setAttributes(layoutParams);
                //Toast.makeText(this, "Night Mode", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    protected void setBaseMap() {
        //TODO: 
    	if (BingMapTileSource.getBingKey().length() == 0) {
            BingMapTileSource.retrieveBingKey(this);
        }
        BingMapTileSource bmts = new BingMapTileSource(null);
        if (!TileSourceFactory.containsTileSource(bmts.name())) {
            TileSourceFactory.addTileSource(bmts);
        }
        
        String satellite = this.getResources().getStringArray(R.array.base_maps)[0];
        String style =prefs.getString("BaseMapSetting", satellite);
        
        if (style.equals(satellite))
            bmts.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
        else
            bmts.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
        
        ITileSource tileSource = TileSourceFactory.getTileSource(bmts.name());
        mapView.setTileSource(tileSource);
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
        SetPreferences(PreferenceManager.getDefaultSharedPreferences(this)); //initial position, zoom
        
        mapView = (MapView) findViewById(R.id.mapview);
        mActivity = this;
        mResourceProxy = new DefaultResourceProxyImpl(this);
        
        setBaseMap();
        
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(start_zoom);
        mapController.setCenter(new GeoPoint(start_lat, start_lon));
        
        
        //location overlay setup
        mLocationOverlay = new MxmMyLocationOverlay(this, mapView, mActivity, mResourceProxy);
        mLocationOverlay.enableMyLocation();
        
        //scale-bar overlay setup
        mScaleBarOverlay = new ScaleBarOverlay(this);
        mScaleBarOverlay.setScaleBarOffset(70, 56);
        mScaleBarOverlay.setLineWidth((float) 3.0);
        mScaleBarOverlay.setTextSize((float) 25.0);
        mScaleBarOverlay.setNautical();
        
        //buttons
        btnZoomIn = (Button) findViewById(R.id.btnZoomIn);
        btnZoomOut = (Button) findViewById(R.id.btnZoomOut);
        btnFollow = (Button) findViewById(R.id.btnFollow);
        btnZoomIn.setOnClickListener(ZoomInListener);
        btnZoomOut.setOnClickListener(ZoomOutListener);
        btnFollow.setOnClickListener(FollowListener);

        //settings
        mapView.setKeepScreenOn(true);
        //setBrightMode(dayDuskNight);
        
        if (warning)
            SettingsDialog.ShowWarning(this);
        
        //TODO: this needs to be moved to an async task with progress bar
        //look for gemf files that have installeddate of 0 in database
        //execute cached sql/dat for missing files if it exists
//        SQLiteDatabase regiondb = (new RegionDbHelper(this)).getWritableDatabase();
//        String[] zeroDate = new ChartDataStore(regiondb).GetUninstalledRegions();
//        GemfCollection gemfCollection = new GemfCollection();
//        String[] missing = GemfCollection.lstUnion(gemfCollection.getRegionList(), zeroDate);
//        for (int i=0; i<missing.length; i++) {
//            try {
//                for (String line:ReadFile.readLines(missing[i]))
//                    regiondb.execSQL(line);
//            } catch (IOException e) {
//                Log.e(tag, e.getMessage());
//            }  
//        }
//        regiondb.close();
    }
    
    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        Log.i(tag, "Orientation changed... restarting activity");
        StorePreferences(false);
        //there may be a better way to fix out of memory error but this does seem to work
        //http://groups.google.com/group/osmdroid/browse_thread/thread/d6918e3e46c40504/30e9bf54eb1e5e83?show_docid=30e9bf54eb1e5e83&pli=1
        Intent intent = getIntent();
        int pid = android.os.Process.myPid();
        finish();
        startActivity(intent);
        android.os.Process.killProcess(pid);
    }
    
    @Override
    public void onPause() {
        StorePreferences(false);
        mapView.getOverlays().clear();
        super.onPause();
    }
    
    protected void addMapLayers() {
        //overlay selected region
        region = prefs.getString("PrefChartLocation", "None");
        if (prefs.getBoolean("UseChartOverlay", true)) {
            gemfOverlay(regiondir + region);
            Toast.makeText(this, "Using chart region: "+region, Toast.LENGTH_SHORT).show();
        }
        
        //chart outlines
       chartOutlines.clearPaths();
       if (prefs.getBoolean("OutlinePref", true)){
           
           SQLiteDatabase regiondb = (new RegionDbHelper(this)).getReadableDatabase();
           ChartDataStore datastore = new ChartDataStore(regiondb);
           for (String coordinates : datastore.getOutlines(region)){
               //Log.i(tag, coordinates);
               //pink noaa chart color Color.rgb(219, 73, 150)
               chartOutlines.addPathOverlay(Color.rgb(69, 172, 137), mResourceProxy, coordinates);
           }
           regiondb.close();
           
           LinkedList<Overlay> collection = new LinkedList<Overlay>();
           for (Object path : chartOutlines.getPaths()) {
               collection.add( (Overlay) path);
           }
           mapView.getOverlays().addAll(collection);
       }
       
       //vessel and scalebar
       mapView.getOverlays().add(mLocationOverlay);
       mapView.getOverlays().add(mScaleBarOverlay);
    }
    
    @Override
    public void onResume() {
        addMapLayers();
       
       //setup buttons according to preferences
       LinearLayout llb = (LinearLayout) this.findViewById(R.id.linearLayout_buttons);
       llb.removeView(btnZoomIn);
       llb.removeView(btnZoomOut);
       llb.removeView(btnFollow);
       if (prefs.getBoolean("ZoomBtnPref", true)) {
           llb.addView(btnZoomIn);
           llb.addView(btnZoomOut);
           llb.addView(btnFollow);
       } else
           llb.addView(btnFollow);
    
       //ddn mode
       setBrightMode();
       
       super.onResume();
       gemfCollection = new GemfCollection();
    }
    
    //android menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
//    //android menu items dynamic changes
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem item = (MenuItem) menu.findItem(R.id.ddn);
//        switch (dayDuskNight) {
//        case 0:
//            item.setTitle("Dusk Mode");
//            break;    
//        case 1:
//            item.setTitle("Night Mode");
//            break;    
//        case 2:
//            item.setTitle("Day Mode");
//            break;
//        }
//        return true;
//    }
    
    //andriod menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) 
        {
            case R.id.mapmode:
                Log.d(tag, "Changin map mode");
                DisplayMode displayMode = new DisplayMode(this, gemfCollection.getFileList());
                displayMode.setTitle("Map Display Mode");
                displayMode.setCanceledOnTouchOutside(false);
                displayMode.setCancelable(false);
                displayMode.show();
                //ddnChange();
                return true;
                
            case R.id.settings:
                Log.d(tag, "Showing Settings");
                SQLiteDatabase regiondb = (new RegionDbHelper(this)).getWritableDatabase();
                new RegionUpdateCheck(regiondb, this).execute();
                startActivity(new Intent(this, SettingsDialog.class));
                return true;
            
            case R.id.quit:
                Log.d(tag, "Finishing");
                StorePreferences(true);
                fullexit();
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}   