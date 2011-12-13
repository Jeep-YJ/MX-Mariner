// Modified by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;

import mx.mariner.util.ReadFile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class RegionDownload extends AsyncTask<Void, Integer, Void> {
    private static final int TIMEOUT = 0;
    private static String USER;
    private static String PASS;
    private static final String tag = "MXM";
    
    private String sqlFilePath; //where region sql will be stored
    private String gemfPartPath;  //where region gemf will be stored during download
    private String gemfFilePath;  //where region gemf will be stored
    private String sqlUrl; //where region sql will be fetched from
    private String gemfUrl; //where region will be fetched from
    private String region;
    private String regionMegabytes;
    private RegionActivity regionActivity;
    private ProgressDialog progressDialog;
    private SQLiteDatabase regiondb;
    
    protected RegionDownload(SQLiteDatabase regiondb, String region, String regionMegaBytes, RegionActivity regionActivity) {
        this.regiondb = regiondb;
        this.region = region;
        this.regionMegabytes = regionMegaBytes;
        this.sqlFilePath = Environment.getExternalStorageDirectory()+"/mxmariner/"+region+".data";
        this.gemfPartPath = Environment.getExternalStorageDirectory()+"/mxmariner/"+region+".part";
        this.gemfFilePath = Environment.getExternalStorageDirectory()+"/mxmariner/"+region+".gemf";
        this.regionActivity = regionActivity;
        this.sqlUrl = regionActivity.getString(R.string.region_url)+region+".sql";
        this.gemfUrl = regionActivity.getString(R.string.region_url)+region+".gemf";
        USER = regionActivity.getString(R.string.http_user);
        PASS = regionActivity.getString(R.string.http_pass);
        progressDialog = new ProgressDialog(regionActivity);
    }
    
    @Override
    protected void onPreExecute() {
        progressDialog.setMessage( String.format("Downloading %s (%sMB)...\n" +
                "Please be patient. This may take a few minutes.\n\n" +
                "Use your back button to cancel.\n",
                region, regionMegabytes) );
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(110);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                File deleteMe = new File(Environment.getExternalStorageDirectory()+"/mxmariner/"+region+".part");
                if (deleteMe.isFile())
                    deleteMe.delete();
                Toast.makeText(regionActivity, region+" download canceled!", Toast.LENGTH_LONG).show();
                RegionDownload.this.cancel(true);
            }
        });
      }
    
    @Override
    protected Void doInBackground(Void... params) {
        //fetch gemf and data files
        if (getFileFromUrl(gemfPartPath, gemfUrl, true)) {
            File from = new File(gemfPartPath);
            File to = new File(gemfFilePath);
            from.renameTo(to);
            
            //install data file
            if (getFileFromUrl(sqlFilePath, sqlUrl, false)) {
                try {
                    ArrayList<String> sql = ReadFile.readLines(sqlFilePath);
                    Log.i("MXM", "installing region chart data");
                    
                    float increment = (float) (10.0/sql.size());
                    float progress = (float) 100.0;
                    for (String line:sql) {
                        progress += increment;
                        publishProgress((int) progress);
                        regiondb.execSQL(line);
                    }
                    
                } catch (IOException e) {
                    Log.e("MXM", e.getMessage());
                }
            }
        }
        return null;
    }
    
    protected void onProgressUpdate(Integer... progress){
        progressDialog.setProgress(progress[0]);
        if (progress[0]==100) {
            progressDialog.setCancelable(false);
            progressDialog.setMessage( String.format("Installing %s data.", region) );
        }
    }
    
    protected void onPostExecute(Void result){
        progressDialog.dismiss(); //memory will leak w/o this
        regionActivity.Restart();
        //select downloaded region and tell onresume to refresh gemfCollection
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(regionActivity).edit();
        editor.putBoolean("RefreshGemf", true);
        editor.putString("PrefChartLocation", region);
        editor.commit();
    }

    private boolean getFileFromUrl(String filePath, String urlString, boolean publish) {
        try {
            long startTime = System.currentTimeMillis();
            URL url = new URL(urlString);
            Log.i(tag, "Downloading: "+url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-length", "0");
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USER, PASS.toCharArray());
                }
            });

            conn.connect();
            Log.i(tag, "Http Response code: "+String.valueOf(conn.getResponseCode()));
            Log.i(tag, "Http Response msg: "+conn.getResponseMessage());
            
            int size = conn.getContentLength();
            int count = 0;
            Log.i(tag, "File bytes: "+String.valueOf(size));
            
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(filePath);
            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                if (publish)
                    publishProgress((int)(total*100/size));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            Log.i(tag, "Downloaded "
                    + String.valueOf(size)
                    + " bytes in "
                    + ((System.currentTimeMillis() - startTime) / 1000.0)
                    + " seconds");
            
        } catch (MalformedURLException e) {
            Log.e(tag, e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
            return false;
        }
        
        return true;
    }
    


}
