// Modified by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.File;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RegionActivity extends ListActivity {
    private Context context;
    private SQLiteDatabase regiondb;
    private ProgressDialog progressDialog;
    private String deleteRegion;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.context = this.getBaseContext();
        super.onCreate(savedInstanceState);
        
        regiondb = (new RegionDbHelper(this)).getWritableDatabase();
        
        progressDialog = new ProgressDialog(this);
        this.setListAdapter(new RegionArrayAdapter(this, new RegionList(context, regiondb).getList()));
        
        ListView listView = getListView();
        listView.setTextFilterEnabled(true);
        
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String name = (String) ((TextView) view.findViewById(R.id.regionname)).getText();
                TextView statusTv = (TextView) view.findViewById(R.id.regionstatus);
                String status = (String) statusTv.getText();
                
                if ( !(status.equals("not installed")) )
                    ConfirmDelete(name);
                return true;
            }
        });
        
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String region = (String) ((TextView) view.findViewById(R.id.regionname)).getText();
                TextView statusTv = (TextView) view.findViewById(R.id.regionstatus);
                String status = (String) statusTv.getText();
                ChartDataStore dataStore = new ChartDataStore(regiondb);
                int regionBytes = dataStore.GetRegionBytes(region);
                int availBytes = dataStore.SdAvailableBytes()-1048576; //leave 1 megabyte 
                if (regionBytes>availBytes)
                    SpaceWarn(regionBytes, availBytes);
                else if ( !(status.equals("installed")) ) {
                    ConfirmDownload(region, regionBytes);
                }
            }
        });
        
    }
    
    @Override
    public void onPause() {
        regiondb.close();
        super.onPause();
    }
    
    private void StartDownload(final String region, String regionMegaBytes) {
        progressDialog.setMessage( String.format("Downloading %s (%sMB)...\n" +
                "Please be patient. This may take a few minutes.\n\n" +
                "Use your back button to cancel.\n",
                region, regionMegaBytes) );
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        final RegionDownload regDl = new RegionDownload(context, regiondb, region, progressDialog, RegionActivity.this);
        regDl.execute(); //regiondb will be closed
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                regDl.cancel(true);
                Toast.makeText(context, region+" download canceled!", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void ConfirmDownload(final String region, int regionBytes) {
        final String regionMegaBytes = String.valueOf( regionBytes / 1048576 );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( String.format("Download %s ?", region) );
        builder.setMessage( String.format("%s (%sMB) will be downloaded.\n" +
                "You may want to connect to wifi  before proceding.\n" +
                "Please be patient. This may take a few minutes.", region, regionMegaBytes));
        deleteRegion = region;
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                RegionActivity.this.StartDownload(region, regionMegaBytes);
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
    }
    
    private void ConfirmDelete(String region) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( String.format("Delete %s ?", region));
        builder.setMessage( String.format("%s will be completely removed.", region));
        deleteRegion = region;
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                File deleteMe = new File(Environment.getExternalStorageDirectory()+"/mxmariner/"+deleteRegion+".gemf");
                deleteMe.delete();
                String sql1 = "UPDATE regions SET installeddate='0' WHERE name='%s';";
                String sql2 = "DELETE from charts where region='%s';";
                regiondb.execSQL( String.format(sql1, deleteRegion) );
                regiondb.execSQL( String.format(sql2, deleteRegion) );
                RegionActivity.this.Restart();
                return;
            }
        });
        
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteRegion = null;
                return;
            }
        });
        
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void SpaceWarn(int regionBytes, int availBytes) {
        String availMegaBytes = String.valueOf( availBytes / 1048576 );
        String regionMegaBytes = String.valueOf( regionBytes / 1048576 );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( "Not enough space!" );
        builder.setMessage( String.format("Your external storage only has %sMB available. \n" +
                "This region is %sMB in size.", availMegaBytes, regionMegaBytes));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public void Restart() {
        regiondb.close();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    
}