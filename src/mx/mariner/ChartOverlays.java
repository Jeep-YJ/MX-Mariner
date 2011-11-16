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
    
    public ChartOverlays(MapActivity context) {
        context.mapView.getOverlays().clear();
        
        context.gemfCollection = new GemfCollection();
        String region = context.prefs.getString("PrefChartLocation", "None");
        String regiondir = Environment.getExternalStorageDirectory()+"/mxmariner/";
        String regionPath = regiondir+region+".gemf";
        Log.i(tag, "Loading raster chart archive: " + regionPath );
        File regionFile = new File(regionPath);
        
        try {
            
            if (context.prefs.getBoolean("UseChartOverlay", true)) {
                //get zoom levels
                GEMFFile gemf = new GEMFFile(regionPath);
                int maxZoom = (Integer) gemf.getZoomLevels().toArray()[0];
                int minZoom = (Integer) gemf.getZoomLevels().toArray()[gemf.getZoomLevels().size()-1];
                
                //chart region overlay
                IArchiveFile[] myArchives = new IArchiveFile[1];
                myArchives[0] = GEMFFileArchive.getGEMFFileArchive(regionFile);
                MxmBitmapTileSourceBase mBitmapTileSourceBase = new MxmBitmapTileSourceBase("RasterCharts", null, minZoom, maxZoom, 256, ".png");
                context.myProviders[0] = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(context), mBitmapTileSourceBase, myArchives);
                MapTileProviderArray myGemfTileProvider = new MapTileProviderArray(mBitmapTileSourceBase, null, context.myProviders);
                TilesOverlay myGemfOverlay = new TilesOverlay(myGemfTileProvider, context);
                myGemfOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                context.mapView.getOverlays().add(myGemfOverlay);
            }
            
            //chart outlines
            ChartOutlines chartOutlines = new ChartOutlines();
            chartOutlines.clearPaths();
            
            if (context.prefs.getBoolean("OutlinePref", true)){
                SQLiteDatabase regiondb = (new RegionDbHelper(context)).getReadableDatabase();
                ChartDataStore datastore = new ChartDataStore(regiondb);
                for (String coordinates : datastore.getOutlines(region)){
                    chartOutlines.addPathOverlay(Color.rgb(69, 172, 137), context.mResourceProxy, coordinates);
                }
                regiondb.close();
                LinkedList<Overlay> collection = new LinkedList<Overlay>();
                for (Object path : chartOutlines.getPaths()) {
                    collection.add( (Overlay) path);
                }
                context.mapView.getOverlays().addAll(collection);
            }
            
            } catch (FileNotFoundException e) {
                Log.e(tag, e.getMessage());
            } catch (IOException e) {
                Log.e(tag, e.getMessage());
        }
    
        if (context.prefs.getBoolean("UseChartOverlay", true)) {
            
        }
        
        //location overlay
        context.mapView.getOverlays().add(context.mLocationOverlay);
        
        //scalebar overlay
        context.mapView.getOverlays().add(context.mScaleBarOverlay);
        
        //refresh view delayed so gemf gets fully drawn
        context.mapView.postInvalidateDelayed(500);
        
    }
    
}

//TODO: 
//look for gemf files that have installeddate of 0 in database
//execute cached sql/dat for missing files if it exists
//SQLiteDatabase regiondb = (new RegionDbHelper(this)).getWritableDatabase();
//String[] zeroDate = new ChartDataStore(regiondb).GetUninstalledRegions();
//GemfCollection gemfCollection = new GemfCollection();
//String[] missing = GemfCollection.lstUnion(gemfCollection.getRegionList(), zeroDate);
//for (int i=0; i<missing.length; i++) {
//    try {
//        for (String line:ReadFile.readLines(missing[i]))
//            regiondb.execSQL(line);
//    } catch (IOException e) {
//        Log.e(tag, e.getMessage());
//    }  
//}
//regiondb.close();
