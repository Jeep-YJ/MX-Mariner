// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.IOException;

import mx.mariner.util.ReadFile;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class Orphans extends AsyncTask<Void, String, Void>{
    private MapActivity mapActivity;
    private GemfCollection gemfCollection;
    private final String tag = "MXM";
    protected ProgressDialog progressDialog;
    private String region = "None";
    
    public Orphans(MapActivity ctx, GemfCollection gmfc) {
        mapActivity = ctx;
        gemfCollection = gmfc;
        progressDialog = new ProgressDialog(mapActivity);
        progressDialog.setTitle("Loading Charts");
        progressDialog.setMessage("Searching for new chart region data.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        //look for regions with installeddate of 0 in database that have corresponding <region>.data & <region>.gemf files on sd card
        //reinstall cached data for these orphaned regions
        SQLiteDatabase regiondb = (new RegionDbHelper(mapActivity)).getWritableDatabase();
        String[] zeroDate = new ChartDataStore(regiondb).GetUninstalledRegions(); //list of uninstalled or zero date regions
        //GemfCollection gemfCollection = new GemfCollection();
        String[] regionList = gemfCollection.getRegionList(); //list of <region>.gemf that has corresponding <region>.data file
        String[] orphans = GemfCollection.lstUnion(regionList, zeroDate); //narrow list down so we can re-install data
        for (int i=0; i<orphans.length; i++) {
            this.publishProgress(orphans[i]);
            Log.i(tag, "installing orphaned data : "+orphans[i]);
            
            //clean up any old data
            String sql = "DELETE from charts where region='%s';";
            regiondb.execSQL( String.format(sql, orphans[i]) );
            
            //re-install data from sd card file
            try {
                for (String line:ReadFile.readLines(gemfCollection.getDir()+orphans[i]+".data"))
                    regiondb.execSQL(line);
                region = orphans[i];
            } catch (IOException e) {
                Log.e(tag, e.getMessage());
            }
            Log.i(tag, "finished installing orphaned data : "+orphans[i]);
        }
        regiondb.close();
        return null;
    }
    
    protected void onProgressUpdate(String... progress){
        progressDialog.setMessage("New chart region data found.\nInstalling "+progress[0]);
    }
    
    @Override
    public void onPostExecute(Void result) {
        progressDialog.dismiss();
        
        if (mapActivity.warning)
            mapActivity.warningAlert.show();
        
        //select last region added if "None" is selected and we have a new one
        String selected = mapActivity.prefs.getString("PrefChartLocation", "None");
        if (selected.equals("None")) {
            mapActivity.editor.putString("PrefChartLocation", region);
            mapActivity.editor.commit();
        }
        
        mapActivity.initChartLayer();
        
    }

}
