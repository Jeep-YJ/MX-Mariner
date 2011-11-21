package mx.mariner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GEMFFile;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

public class ChartOverlays {
    
    //====================
    // Constants
    //====================
    
    private static final String tag = "MXM";
    
    //====================
    // Constructor
    //====================
    
    public ChartOverlays(MapActivity mapActivity) {
        mapActivity.mapView.getOverlays().clear();
        
        mapActivity.gemfCollection = new GemfCollection();
        String region = mapActivity.prefs.getString("PrefChartLocation", "None");
        String regiondir = Environment.getExternalStorageDirectory()+"/mxmariner/";
        String regionPath = regiondir+region+".gemf";
        Log.i(tag, "Loading raster chart archive: " + regionPath );
        
        File regionFile = new File(regionPath);
        
        if (!regionFile.exists() && mapActivity.gemfCollection.getFileList().length>0) {
            Log.i(tag, "Prefered region does not exist, using first one found");
            region = mapActivity.gemfCollection.getFileList()[0];
            regionPath = regiondir+region+".gemf";
            regionFile = new File(regionPath);
        }
        
        try {
            
            if (mapActivity.prefs.getBoolean("UseChartOverlay", true)) {
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
                TilesOverlay myGemfOverlay = new TilesOverlay(myGemfTileProvider, mapActivity);
                myGemfOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                mapActivity.mapView.getOverlays().add(myGemfOverlay);
            }
            
            //chart outlines
            ChartOutlines chartOutlines = new ChartOutlines();
            chartOutlines.clearPaths();
            
            if (mapActivity.prefs.getBoolean("OutlinePref", true)){
                SQLiteDatabase regiondb = (new RegionDbHelper(mapActivity)).getReadableDatabase();
                ChartDataStore datastore = new ChartDataStore(regiondb);
                for (String coordinates : datastore.getOutlines(region)){
                    chartOutlines.addPathOverlay(Color.rgb(69, 172, 137), mapActivity.mResourceProxy, coordinates);
                }
                regiondb.close();
                LinkedList<Overlay> collection = new LinkedList<Overlay>();
                for (Object path : chartOutlines.getPaths()) {
                    collection.add( (Overlay) path);
                }
                mapActivity.mapView.getOverlays().addAll(collection);
            }
            
            } catch (FileNotFoundException e) {
                Log.e(tag, e.getMessage());
            } catch (IOException e) {
                Log.e(tag, e.getMessage());
        }
    
        if (mapActivity.prefs.getBoolean("UseChartOverlay", true)) {
            
        }
        
        //location overlay
        mapActivity.mapView.getOverlays().add(mapActivity.mLocationOverlay);
        
        //scalebar overlay
        mapActivity.mapView.getOverlays().add(mapActivity.mScaleBarOverlay);
        
        //refresh view delayed so gemf gets fully drawn
        mapActivity.mapView.postInvalidateDelayed(750);
        
    }
    
}