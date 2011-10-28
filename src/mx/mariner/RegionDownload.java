// Modified by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class RegionDownload extends AsyncTask<String, Integer, String> {
    private static final int TIMEOUT = 0;
    private static String USER;
    private static String PASS;
    private static final String tag = "MXM";
    
    private String sqlFilePath; //where region sql will be stored
    private String gemfPartPath;  //where region gemf will be stored during download
    private String gemfFilePath;  //where region gemf will be stored
    private String sqlUrl; //where region sql will be fetched from
    private String gemfUrl; //where region
    private Activity parent;
    protected ProgressDialog progressDialog;
    
    private SQLiteDatabase regiondb;
    
    protected RegionDownload(Context context, SQLiteDatabase regiondb, String region, 
             ProgressDialog progressDialog, Activity activity) {
        this.regiondb = regiondb;
        this.sqlFilePath = context.getString(R.string.data_path)+region+".sql";
        this.gemfPartPath = Environment.getExternalStorageDirectory()+"/mxmariner/"+region+".part";
        this.gemfFilePath = Environment.getExternalStorageDirectory()+"/mxmariner/"+region+".gemf";
        this.sqlUrl = context.getString(R.string.region_url)+region+".sql";
        this.gemfUrl = context.getString(R.string.region_url)+region+".gemf";
        this.progressDialog = progressDialog;
        this.parent = activity;
        USER = context.getString(R.string.http_user);
        PASS = context.getString(R.string.http_pass);
        
    }
    
    @Override
    protected void onPreExecute() {
        progressDialog.show();
      }
    
    @Override
    protected String doInBackground(String... params) {
        if (getFileFromUrl(gemfPartPath, gemfUrl, true)) {
            File from = new File(gemfPartPath);
            File to = new File(gemfFilePath);
            from.renameTo(to);

            if (getFileFromUrl(sqlFilePath, sqlUrl, false)) {
                try {
                    ArrayList<String> sql = readLines(sqlFilePath);
                    Log.i("MXM", "installing region chart data");
                    float increment = (float) (5.0/sql.size());
                    float progress = (float) 95.0;
                    for (String line:sql) {
                        progress += increment;
                        publishProgress((int) progress);
                        regiondb.execSQL(line);
                    }
                    
                    File file = new File(sqlFilePath);
                    if (file.delete())
                        Log.i(tag, "deleted file:"+sqlFilePath);
                    
                } catch (IOException e) {
                    Log.e("MXM", e.getMessage());
                }
            }
        }
        return null;
    }
    
    protected void onProgressUpdate(Integer... progress){
        progressDialog.setProgress(progress[0]);
        if (progress[0] == 95)
            progressDialog.setMessage("Installing data");
    }
    
    protected void onPostExecute(String result){
        progressDialog.dismiss();
        regiondb.close();
        
        //restart parent activity
        Intent intent = parent.getIntent();
        parent.finish();
        parent.startActivity(intent);
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
                    publishProgress((int)(total*100/size)-5);
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
    
//    private String readFileAsString(String filePath) throws java.io.IOException{
//        byte[] buffer = new byte[(int) new File(filePath).length()];
//        BufferedInputStream f = null;
//        try {
//            f = new BufferedInputStream(new FileInputStream(filePath));
//            f.read(buffer);
//        } finally {
//            if (f != null) try { f.close(); } catch (IOException ignored) { }
//        }
//        return new String(buffer);
//    }
    
    public ArrayList<String> readLines(String filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        ArrayList<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

}
