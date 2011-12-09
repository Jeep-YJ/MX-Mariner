// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
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

    private MapController mapController;
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
    private BingMapTileSource bingMapTileSource;
    protected AlertDialog warningAlert;
    private static AlertDialog.Builder warningDialog;
    protected MapTileModuleProviderBase[] myProviders = new MapTileModuleProviderBase[1];
    protected Location mLocation;
    protected ChartOverlays chartOverlays;
    protected MxMyLocationOverlay mLocationOverlay;
    protected ScaleBarOverlay mScaleBarOverlay;
    protected MeasureOverlay measureOverlay;
    protected MeasureToolOverlay measureToolOverlay;
    //TODO:
    protected ItemizedIconOverlay<OverlayItem> waypointOverlay;
    //protected RoutOverlay routOverlay;
    //protected TrackOverlay trackOverlay;

    
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
            }
            mapController.zoomIn();
        }
    };
    
    private View.OnClickListener ZoomOutListener = new View.OnClickListener() {
        public void onClick(View v) {
            //is there a better way?...zoom during follow is screwed up otherwise
            if (mLocationOverlay.isFollowLocationEnabled()) {
                mLocationOverlay.disableFollowLocation();
            }
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
            measureToolOverlay.StartMeasure();
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
            if (!WaypointFunctions.isWayPoint(featuresDb, point.getLatitudeE6(), point.getLongitudeE6())){
                final NewWaypointDialog nwDlg = new NewWaypointDialog(MapActivity.this);
                nwDlg.show();
            } else {
                Toast.makeText(MapActivity.this, "There already is a waypoint here!", Toast.LENGTH_SHORT).show();
            }
            
        }
    };
    
    private View.OnClickListener BtnMenuListener = new View.OnClickListener() {
        public void onClick(View v) {
            //TODO:
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
        
        if (BingMapTileSource.getBingKey().length() == 0) {
            BingMapTileSource.retrieveBingKey(this);
        }
        bingMapTileSource = new BingMapTileSource(null);
        if (!TileSourceFactory.containsTileSource(bingMapTileSource.name())) {
            TileSourceFactory.addTileSource(bingMapTileSource);
        }
        
        String defStyle = this.getResources().getString(R.string.default_base_map);
        if (prefs.getString("BaseMapSetting", defStyle).equals(defStyle))
            bingMapTileSource.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
        else
            bingMapTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
        
        ITileSource tileSource = TileSourceFactory.getTileSource(bingMapTileSource.name());
        mapView.setTileSource(tileSource);
        
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(start_zoom);
        mapController.setCenter(new GeoPoint(start_lat, start_lon));
        
        //location overlay setup
        mLocationOverlay = new MxMyLocationOverlay(this, mapView, mActivity, mResourceProxy);
        
        //scale-bar overlay setup
        mScaleBarOverlay = new ScaleBarOverlay(this);
        //mScaleBarOverlay.setBarPaint(pBarPaint)
        mScaleBarOverlay.setScaleBarOffset(10, 50);
        mScaleBarOverlay.setLineWidth((float) 3.0);
        mScaleBarOverlay.setTextSize((float) 25.0);
        mScaleBarOverlay.setNautical();
        
        //mearure overlay setup
        measureOverlay = new MeasureOverlay(this);
        
        //arbitrary measure tool overlay setup
        measureToolOverlay = new MeasureToolOverlay(this);
        measureToolOverlay.disable(); //we only enable this when user wants to measure arbitrarily
        
        featuresDb = new FeaturesDbHelper(this).getWritableDatabase();
        
        //waypoint overlay
        final ArrayList<OverlayItem> waypoints = new ArrayList<OverlayItem>();
        //waypoints.add(new OverlayItem("Zero", "Lat Long 0", new GeoPoint(0, 0)));
        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> waypointListener =  new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle( "Delete mark?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        String sql = String.format("delete from waypoints where latitude is %s and longitude is %s;", item.getPoint().getLatitudeE6(),item.getPoint().getLongitudeE6());
                        featuresDb.execSQL(sql);
                        waypointOverlay.removeItem(index);
                        mapView.invalidate();
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
                return true;
            }
            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return false;
            }
        };
        
        waypointOverlay = new ItemizedIconOverlay<OverlayItem>(waypoints, getResources().getDrawable(R.drawable.greenflag), waypointListener , mResourceProxy);
        WaypointFunctions.addWayPointsToOverlay(this, waypointOverlay, featuresDb);
        
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
            requestedStyle = BingMapTileSource.IMAGERYSET_ROAD;
        } else {
            requestedStyle = BingMapTileSource.IMAGERYSET_AERIALWITHLABELS;
        }
        String setStyle = bingMapTileSource.getStyle();
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