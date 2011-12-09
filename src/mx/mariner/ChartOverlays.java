// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GEMFFile;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class ChartOverlays {
    
    //====================
    // Constants
    //====================
    
    private static final String tag = "MXM";
    private MapActivity mapActivity;
    protected boolean outlinesOn = false;
    protected String region = "None";
    private LinkedList<Overlay> outlineCollection = null;
    private TilesOverlay myGemfOverlay = null;
    
    //====================
    // Constructor
    //====================
    
    protected ChartOverlays(MapActivity ctx) {
        mapActivity = ctx;
        loadRegion();
    }
    
    //====================
    // Methods
    //====================
    protected void removeAll() {
        List<Overlay> overlays = mapActivity.mapView.getOverlays();
        overlays.clear();
    }
    
    protected void addAll() {
        List<Overlay> overlays = mapActivity.mapView.getOverlays();
        //raster gemf chart region overlay
        if (mapActivity.prefs.getBoolean("UseChartOverlay", true) && myGemfOverlay!= null && !region.equals("None")) {
            overlays.add(myGemfOverlay);
        }
        
        //outlines overlay
        if (mapActivity.prefs.getBoolean("OutlinePref", true) && outlineCollection != null && !region.equals("None")){
            outlinesOn = true;
            overlays.addAll(outlineCollection);
        }
        
        //waypoint overlay
        overlays.add(mapActivity.waypointOverlay);
        
        //route overlay
        //TODO:
        
        //track overlay
        //TODO:
        
        //location overlay
        overlays.add(mapActivity.mLocationOverlay);
        
        //measure overlay
        overlays.add(mapActivity.measureOverlay);
        
        //arbitrary measure overlay
        overlays.add(mapActivity.measureToolOverlay);
        
        //scalebar overlay
        overlays.add(mapActivity.mScaleBarOverlay);
        
    }
    
    protected void loadRegion() {
        region = mapActivity.prefs.getString("PrefChartLocation", "None");
        String regiondir = Environment.getExternalStorageDirectory()+"/mxmariner/";
        String regionPath = regiondir+region+".gemf";
        Log.i(tag, "Loading raster chart archive: " + regionPath );
        
        File regionFile = new File(regionPath);
        
        if (!regionFile.exists() && !mapActivity.gemfCollection.getFileList()[0].equals("None")) {
            Log.i(tag, "Prefered region does not exist, using first one found");
            region = mapActivity.gemfCollection.getFileList()[0];
            //put this region into preferences
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mapActivity).edit();
            editor.putString("PrefChartLocation", region);
            editor.commit();
            //continue loading
            regionPath = regiondir+region+".gemf";
            regionFile = new File(regionPath);
        }
        
        try {
            //get zoom levels
            GEMFFile gemf = new GEMFFile(regionPath);
            int maxZoom = (Integer) gemf.getZoomLevels().toArray()[0];
            int minZoom = (Integer) gemf.getZoomLevels().toArray()[gemf.getZoomLevels().size()-1];
            
            //chart region overlay
            IArchiveFile[] myArchives = new IArchiveFile[1];
            myArchives[0] = GEMFFileArchive.getGEMFFileArchive(regionFile);
            MxmBitmapTileSourceBase mBitmapTileSourceBase = new MxmBitmapTileSourceBase("RasterCharts", null, minZoom, maxZoom, 256, ".png");
            mapActivity.myProviders[0] = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(mapActivity), mBitmapTileSourceBase, myArchives);
            MapTileProviderArray myGemfTileProvider = new MapTileProviderArray(mBitmapTileSourceBase, null, mapActivity.myProviders);
            myGemfOverlay = new TilesOverlay(myGemfTileProvider, mapActivity);
            myGemfOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            
            //chart outlines
            ChartOutlines chartOutlines = new ChartOutlines();
            chartOutlines.clearPaths();
            
            SQLiteDatabase regiondb = (new RegionDbHelper(mapActivity)).getReadableDatabase();
            ChartDataStore datastore = new ChartDataStore(regiondb);
            for (String coordinates : datastore.getOutlines(region)){
                chartOutlines.addPathOverlay(Color.rgb(69, 172, 137), mapActivity.mResourceProxy, coordinates);
            }
            regiondb.close();
            
            outlineCollection = new LinkedList<Overlay>();
            for (Object path : chartOutlines.getPaths()) {
                outlineCollection.add( (Overlay) path);
            }
            
        } catch (FileNotFoundException e) {
            Log.e(tag, e.getMessage());
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
        }
        //refresh view delayed so gemf gets fully drawn
        mapActivity.mapView.postInvalidateDelayed(1000);
    }
    
}