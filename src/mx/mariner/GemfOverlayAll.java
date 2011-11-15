package mx.mariner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

public class GemfOverlayAll extends AsyncTask<String, Integer, String> {
    
    //====================
    // Constants
    //====================
    
    private static final String tag = "MXM";
    private static final String regiondir = Environment.getExternalStorageDirectory()+"/mxmariner/";
    
    //====================
    // Fields
    //====================
    
    private IArchiveFile[] myArchives;
    private MapTileModuleProviderBase[] myProviders = new MapTileModuleProviderBase[1];
    private TilesOverlay myGemfOverlay;
    private int minZoom;
    private int maxZoom;
    //private ProgressDialog progressDialog;
    private MapActivity context;
    private String[] regions;
    private ChartOutlines chartOutlines = new ChartOutlines();
    private LinkedList<Overlay> collection = new LinkedList<Overlay>();
    
    //====================
    // Constructor
    //====================
    
    public GemfOverlayAll(MapActivity parent) {
        context = parent;
    }
    
    //====================
    // Methods
    //====================
    
    @Override
    public String doInBackground(String... params) {
        GemfCollection gemfCollection = new GemfCollection();
        regions = gemfCollection.getFileList();
        myArchives = new IArchiveFile[regions.length];
        Log.i(tag, "Raster chart archives to load: " + String.valueOf(regions.length) );
        minZoom = gemfCollection.getMinZoom();
        maxZoom = gemfCollection.getMaxZoom();
        
        for (int i=0; i<regions.length; i++) {
            String gemfLocation = regiondir+regions[i]+".gemf";
            Log.i(tag, "Loading raster chart archive: " + gemfLocation );
            File location = new File(gemfLocation);
            try {
                //set up overlay
                myArchives[i] = GEMFFileArchive.getGEMFFileArchive(location);
            } catch (FileNotFoundException e) {
                Log.e(tag, e.getMessage());
            } catch (IOException e) {
                Log.e(tag, e.getMessage());
            }
        }
        MxmBitmapTileSourceBase mBitmapTileSourceBase = new MxmBitmapTileSourceBase("RasterCharts", null, minZoom, maxZoom, 256, ".png");
        myProviders[0] = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(context), mBitmapTileSourceBase, myArchives);
        MapTileProviderArray myGemfTileProvider = new MapTileProviderArray(mBitmapTileSourceBase, null, myProviders);
        myGemfOverlay = new TilesOverlay(myGemfTileProvider, context);
        myGemfOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        context.mapView.getOverlays().add(context.RASTERCHARTLAYER, myGemfOverlay);
        
        //chart outlines
        chartOutlines.clearPaths();
        if (context.prefs.getBoolean("OutlinePref", true)){
            SQLiteDatabase regiondb = (new RegionDbHelper(context)).getReadableDatabase();
            ChartDataStore datastore = new ChartDataStore(regiondb);
            for ( String region:regions ) {
                for (String coordinates : datastore.getOutlines(region)){
                    chartOutlines.addPathOverlay(Color.rgb(69, 172, 137), context.mResourceProxy, coordinates);
                }
            }
            regiondb.close();
            for (Object path : chartOutlines.getPaths()) {
                collection.add( (Overlay) path);
            }
        }
        if (context.prefs.getBoolean("OutlinePref", true)){
            context.mapView.getOverlays().addAll(collection);
        }
        //location
        context.mapView.getOverlays().add(context.mLocationOverlay);
        
        //scalebar
        context.mapView.getOverlays().add(context.mScaleBarOverlay);
        
        long sleep = 4000 - SystemClock.currentThreadTimeMillis();
        if (sleep>0 && context.warningAlert.isShowing()) {
            try {
                Log.i(tag, "Sleeping: " + String.valueOf(sleep));
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                Log.e(tag, e.getMessage());
            }
        }
        
        return null;
    }
    
    @Override
    public void onPostExecute(String result) {
        
        //refresh
        context.mapView.postInvalidate();
        
        //close warning dialog
        if (context.warningAlert.isShowing())
            context.warningAlert.dismiss();
    }
}
