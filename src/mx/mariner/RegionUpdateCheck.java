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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class RegionUpdateCheck extends AsyncTask<String, Integer, String> {
    private static String FILEPATH;
    private static String URL;
    private static String USER;
    private static String PASS;
    
    private static final int TIMEOUT = 20000;
    private static final String tag = "MXM";
    
    private SQLiteDatabase regiondb;
    
    public RegionUpdateCheck(SQLiteDatabase regiondb, Context context) {
        this.regiondb = regiondb;
        FILEPATH = context.getString(R.string.data_path)+"update.sql";
        URL = context.getString(R.string.update_url);
        USER = context.getString(R.string.http_user);
        PASS = context.getString(R.string.http_pass);
    }
    
    @Override
    protected String doInBackground(String... params) {
        getFileFromUrl(FILEPATH, URL);
        try {
            ArrayList<String> sql = ReadFile.readLines(FILEPATH);
            Log.i("MXM", "updating region sql data");
            for (String line:sql)
                regiondb.execSQL(line);
                    
            File file = new File(FILEPATH);
            if (file.delete())
                Log.i(tag, "deleted file:"+FILEPATH);
            
        } catch (IOException e) {
            Log.e("MXM", e.getMessage());
        }
        return null;
    }
    
    protected void onPostExecute(String result){
        regiondb.close();
    }
    
    private boolean getFileFromUrl(String filePath, String urlString) {
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
