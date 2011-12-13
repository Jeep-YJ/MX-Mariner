// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.util.ArrayList;

import mx.mariner.marks.WaypointDbFunctions;
import mx.mariner.marks.WaypointMarkerIcons;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapActivity extends Activity {
    
    //====================
    // Constants
    //====================
    
    protected final int RASTERCHARTLAYER = 0;
    private final String tag = "MXM";
    
    //====================
    // Fields
    //====================

    protected MapController mapController;
    private Orphans orphans;
    protected MapView mapView;
    private Activity mActivity;
    
    protected ResourceProxy mResourceProxy;
    protected int dayDuskNight; //0-day, 1-dusk, 2-night
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor;
    protected GemfCollection gemfCollection;
    protected SQLiteDatabase featuresDb;
    private int start_lat; //geopoint
    private int start_lon; //geopoint
    private int start_zoom;
    private Button btnZoomIn;
    private Button btnZoomOut;
    protected Button btnFollow;
    protected Button btnMenu;
    protected Button btnMeasure;
    protected Button btnTrack;
    protected Button btnRoute;
    protected Button btnWaypoint;
    private boolean menuButtons = false;
    protected boolean warning;
    private BaseMapTileSource baseMapTileSource;
    protected AlertDialog warningAlert;
    private static AlertDialog.Builder warningDialog;
    protected MapTileModuleProviderBase[] myProviders = new MapTileModuleProviderBase[1];
    protected Location mLocation;
    protected ChartOverlays chartOverlays;
    protected LocationOverlay mLocationOverlay;
    protected ScaleBarOverlay mScaleBarOverlay;
    protected MeasureOverlay measureOverlay;
    protected ItemizedIconOverlay<OverlayItem> waypointOverlay;
    protected WaypointMarkerIcons waypointMarkerIcons;

    
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
                btnFollow.setEnabled(true);
                btnFollow.setBackgroundResource(R.drawable.follow);
                measureOverlay.enable();
                //btnFollow.setVisibility(View.VISIBLE);
            } //else {
            //    Toast.makeText(MapActivity.this, "Zoom: "+String.valueOf(mapView.getZoomLevel()), Toast.LENGTH_SHORT).show();
            //}
            mapController.zoomIn();
        }
    };
    
    private View.OnClickListener ZoomOutListener = new View.OnClickListener() {
        public void onClick(View v) {
            //is there a better way?...zoom during follow is screwed up otherwise
            if (mLocationOverlay.isFollowLocationEnabled()) {
                mLocationOverlay.disableFollowLocation();
                btnFollow.setEnabled(true);
                btnFollow.setBackgroundResource(R.drawable.follow);
                measureOverlay.enable();
                //btnFollow.setVisibility(View.VISIBLE);
            } //else {
            //    Toast.makeText(MapActivity.this, "Zoom: "+String.valueOf(mapView.getZoomLevel()), Toast.LENGTH_SHORT).show();
            //}
            mapController.zoomOut();
        }
    };
    
    private View.OnClickListener FollowListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mLocationOverlay.isFollowLocationEnabled())
                mLocationOverlay.enableFollowLocation();
        }
    };
    
    private View.OnClickListener BtnMeasureListener = new View.OnClickListener() {
        
        public void onClick(View v) {
            
            //MapActivity.this.getResources().getDisplayMetrics().widthPixels;
            final int positionx = (int) (MapActivity.this.getResources().getDisplayMetrics().widthPixels / 2);
            final int positiony = (int) (MapActivity.this.getResources().getDisplayMetrics().heightPixels / 2.6);
            
            final Projection projection = mapView.getProjection();
            //relative to top left corner
            final IGeoPoint adjusted = projection.fromPixels(positionx, positiony);
            
            measureOverlay.enable();
            measureOverlay.setModeArbitrary();
            mapController.animateTo(adjusted);
            //mapView.postInvalidateDelayed(500);
        }
    };
    
    private View.OnClickListener BtnTrackListener = new View.OnClickListener() {
        
        public void onClick(View v) {
            // TODO Auto-generated method stub
            
        }
    };
    
    private View.OnClickListener BtnRouteListener = new View.OnClickListener() {
        
        public void onClick(View v) {
            // TODO Auto-generated method stub
            
        }
    };
    
    private View.OnClickListener BtnWaypointListener = new View.OnClickListener() {
        public void onClick(View v) {
            GeoPoint point = (GeoPoint) mapView.getMapCenter();
            if (!WaypointDbFunctions.isWayPointInDb(featuresDb, point.getLatitudeE6(), point.getLongitudeE6())){
                final WaypointDialog nwDlg = new WaypointDialog(MapActivity.this, point, -1);
                nwDlg.show();
            } else {
                Toast.makeText(MapActivity.this, "There already is a waypoint here!", Toast.LENGTH_SHORT).show();
            }
            
        }
    };
    
    private View.OnClickListener BtnMenuListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (menuButtons) {
                menuButtons = false;
                btnMeasure.setVisibility(View.GONE);
                btnTrack.setVisibility(View.GONE);
                btnRoute.setVisibility(View.GONE);
                btnWaypoint.setVisibility(View.GONE);
            } else {
                menuButtons = true;
                btnMeasure.setVisibility(View.VISIBLE);
                //btnTrack.setVisibility(View.VISIBLE);
                //btnRoute.setVisibility(View.VISIBLE);
                btnWaypoint.setVisibility(View.VISIBLE);
            }
        }
    };
    
    public void setFollowButtonEnabled(boolean enabled) {
        btnFollow.setEnabled(enabled);
        if (enabled) {
            btnFollow.setBackgroundResource(R.drawable.follow);
        } else {
            btnFollow.setBackgroundResource(R.drawable.followdisabled);
        }
    }
    
    public void setExtraMenuButtonsEnabled(boolean enabled) {
        btnMenu.setEnabled(enabled);
        btnMeasure.setEnabled(enabled);
        //btnTrack.setEnabled(enabled);
        //btnRoute.setEnabled(enabled);
        //btnWaypoint.setEnabled(enabled);
        if (enabled) {
            btnMenu.setBackgroundResource(R.drawable.menu);
            btnMeasure.setBackgroundResource(R.drawable.measure);
            //btnTrack.setBackgroundResource(R.drawable.route);
            //btnRoute.setBackgroundResource(R.drawable.track);
            //btnWaypoint.setBackgroundResource(R.drawable.waypoint);
        } else {
            btnMenu.setBackgroundResource(R.drawable.menudisabled);
            btnMeasure.setBackgroundResource(R.drawable.measuredisabled);
            //btnTrack.setBackgroundResource(R.drawable.routedisabled);
            //btnRoute.setBackgroundResource(R.drawable.trackdisabled);
            //btnWaypoint.setBackgroundResource(R.drawable.waypointdisabled);
        }
    }
    
    protected void initChartLayer() {
        chartOverlays = new ChartOverlays(this);
        chartOverlays.addAll();
    }
    
    protected void refreshChartLayer() {
        chartOverlays.removeAll();
        chartOverlays.loadRegion();
        chartOverlays.addAll();
    }
    
    protected void setBrightMode() {
        FrameLayout ddnMask = (FrameLayout) findViewById(R.id.ddnMask);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        
        switch (dayDuskNight) {
            case 0:
                ddnMask.setBackgroundResource(R.color.transparent);
                float day = prefs.getFloat("DayBright", (float) 1.0);
                layoutParams.screenBrightness = day;
                getWindow().setAttributes(layoutParams);
                break;
                
            case 1:
                ddnMask.setBackgroundResource(R.color.transparent);
                float dusk = prefs.getFloat("DuskBright", (float) 0.1);
                layoutParams.screenBrightness = dusk;
                getWindow().setAttributes(layoutParams);
                break;
                
            case 2:
                ddnMask.setBackgroundResource(R.color.redtint);
                float night = prefs.getFloat("NightBright", (float) 0.05);
                layoutParams.screenBrightness = night;
                getWindow().setAttributes(layoutParams);
                break;
        }
    }
    
    protected void Restart() {
        Intent intent = getIntent();
        int pid = android.os.Process.myPid();
        finish();
        startActivity(intent);
        android.os.Process.killProcess(pid);
    }
    //====================
    // SuperClass Methods
    //====================
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        warningDialog = new AlertDialog.Builder(this);
        warningDialog.setTitle("Warning");
        warningDialog.setIcon(R.drawable.icon);
        warningDialog.setMessage(getResources().getString(R.string.nav_warning));
        warningDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }); 
        warningAlert = warningDialog.create();
        
        SetPreferences(PreferenceManager.getDefaultSharedPreferences(this)); //initial position, zoom
        
        gemfCollection = new GemfCollection();
        orphans = new Orphans(this, gemfCollection);
        orphans.execute();
        
        mapView = (MapView) findViewById(R.id.mapview);
        mActivity = this;
        mResourceProxy = new DefaultResourceProxyImpl(this);
        
        if (BaseMapTileSource.getBingKey().length() == 0) {
            BaseMapTileSource.retrieveBingKey(this);
        }
        baseMapTileSource = new BaseMapTileSource(null);
        if (!TileSourceFactory.containsTileSource(baseMapTileSource.name())) {
            TileSourceFactory.addTileSource(baseMapTileSource);
        }
        
        
        String defStyle = this.getResources().getString(R.string.default_base_map);
        if (prefs.getString("BaseMapSetting", defStyle).equals(defStyle))
            baseMapTileSource.setStyle(BaseMapTileSource.IMAGERYSET_ROAD);
        else
            baseMapTileSource.setStyle(BaseMapTileSource.IMAGERYSET_AERIALWITHLABELS);
        
        ITileSource tileSource = TileSourceFactory.getTileSource(baseMapTileSource.name());
        mapView.setTileSource(tileSource);
        
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(start_zoom);
        mapController.setCenter(new GeoPoint(start_lat, start_lon));
        
        //location overlay setup
        mLocationOverlay = new LocationOverlay(this, mapView, mActivity, mResourceProxy);
        
        //scale-bar overlay setup
        mScaleBarOverlay = new ScaleBarOverlay(this);
        //mScaleBarOverlay.setBarPaint(pBarPaint)
        mScaleBarOverlay.setScaleBarOffset(10, 50);
        mScaleBarOverlay.setLineWidth((float) 3.0);
        mScaleBarOverlay.setTextSize((float) 25.0);
        mScaleBarOverlay.setNautical();
        
        //mearure overlay setup
        measureOverlay = new MeasureOverlay(this);
        
        //database for waypoints route and tracks
        featuresDb = new FeaturesDbHelper(this).getWritableDatabase();
        
        //waypoint overlay
        final ArrayList<OverlayItem> waypoints = new ArrayList<OverlayItem>();
        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> waypointListener =  new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                final WaypointDialog nwDlg = new WaypointDialog(MapActivity.this, item.getPoint(), index);
                nwDlg.show();
                return true;
            }
            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return false;
            }
        };
        
        waypointMarkerIcons = new WaypointMarkerIcons(this);
        waypointOverlay = new ItemizedIconOverlay<OverlayItem>(waypoints, getResources().getDrawable(waypointMarkerIcons.getRids()[0]), 
                waypointListener , mResourceProxy);
        WaypointDbFunctions.addWayPointsFromDbToOverlay(this, waypointOverlay, featuresDb, waypointMarkerIcons);
        
        //TODO:
        //route overlay
        
        //TODO:
        //track overlay
        
        //control buttons
        btnZoomIn = (Button) findViewById(R.id.btnZoomIn);
        btnZoomOut = (Button) findViewById(R.id.btnZoomOut);
        btnFollow = (Button) findViewById(R.id.btnFollow);
        btnMenu = (Button) findViewById(R.id.menu);
        btnMeasure = (Button) findViewById(R.id.measure);
        btnTrack = (Button) findViewById(R.id.track);
        btnRoute = (Button) findViewById(R.id.route);
        btnWaypoint = (Button) findViewById(R.id.waypoint);
        btnZoomIn.setOnClickListener(ZoomInListener);
        btnZoomOut.setOnClickListener(ZoomOutListener);
        btnFollow.setOnClickListener(FollowListener);
        
        //extra buttons
        btnMenu.setOnClickListener(BtnMenuListener);
        btnMeasure.setOnClickListener(BtnMeasureListener);
        btnTrack.setOnClickListener(BtnTrackListener);
        btnRoute.setOnClickListener(BtnRouteListener);
        btnWaypoint.setOnClickListener(BtnWaypointListener);
        
        //settings
        mapView.setKeepScreenOn(true);
        
        if (warning && !orphans.progressDialog.isShowing())
            warningAlert.show();
    }
    
    @Override
    public void onPause() {
        featuresDb.close();
        mLocationOverlay.disableMyLocation(); //we don't need the gps when paused
        chartOverlays.removeAll();
        StorePreferences(false); //set warning to not show again
        super.onPause();
    }
    
    @Override
    public void onBackPressed() {
        StorePreferences(true); //true sets warning to be shown
        //completely kill this activity
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        return;
    }
    
    @Override
    public void onResume() {
        if (!featuresDb.isOpen())
            featuresDb = new FeaturesDbHelper(this).getWritableDatabase();
        
        //see if the base map style has changed and restart activity if necessary
        String defStyle = this.getResources().getStringArray(R.array.base_maps)[0];
        String requestedStyle;
        if (prefs.getString("BaseMapSetting", defStyle).equals(defStyle)) {
            requestedStyle = BaseMapTileSource.IMAGERYSET_ROAD;
        } else {
            requestedStyle = BaseMapTileSource.IMAGERYSET_AERIALWITHLABELS;
        }
        String setStyle = baseMapTileSource.getStyle();
        if (!requestedStyle.equals(setStyle)) {
            Log.i(tag, "Base map preference changed; restarting activity...");
            Restart();
        }
        
        //see if gemfCollection needs to be refreshed and preferred chart region has changed
        //chartOverlays could be null if application crashed after a region was deleted
        if (prefs.getBoolean("RefreshGemf", false) && chartOverlays != null) {
            Log.i(tag, "GemfCollection refreshed");
            gemfCollection = new GemfCollection();
            editor.putBoolean("RefreshGemf", false); //we don't need to refresh again
            refreshChartLayer();
        } else if ( chartOverlays!=null ) {
            chartOverlays.addAll();
        }
        
        //turn gps on and listen
        mLocationOverlay.enableMyLocation();
        
        //setup buttons according to preferences
        RelativeLayout ctrlBtnLayout = (RelativeLayout) this.findViewById(R.id.control_buttons);
        ctrlBtnLayout.removeView(btnZoomIn);
        ctrlBtnLayout.removeView(btnZoomOut);
        ctrlBtnLayout.removeView(btnFollow);
        if (prefs.getBoolean("ZoomBtnPref", true)) {
            ctrlBtnLayout.addView(btnZoomIn);
            ctrlBtnLayout.addView(btnZoomOut);
            ctrlBtnLayout.addView(btnFollow);
        } else
            ctrlBtnLayout.addView(btnFollow);
       
        //ddn mode
        setBrightMode();
        super.onResume();
    }
    
    //android menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    //andriod menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent settings = new Intent(this, SettingsDialog.class);
        Intent help = new Intent(this, Help.class);
        
        switch (item.getItemId()) {
            case R.id.display:
                String[] regionItems = {"None"};
                if (gemfCollection.getFileList().length > 0)
                    regionItems = gemfCollection.getFileList();
                DisplayMode displayMode = new DisplayMode(this, regionItems);
                displayMode.setTitle( this.getResources().getString(R.string.display) );
                displayMode.setCanceledOnTouchOutside(false);
                displayMode.setCancelable(true);
                displayMode.show();
                return true;
                
            case R.id.settings:
                startActivity(settings);
                return true;
                
            case R.id.help:
                startActivity(help);
                return true;
                
            default:
                return true;
        }
    }
    
}   