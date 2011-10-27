package mx.mariner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

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
            String sql = readFileAsString(FILEPATH);
            //Log.i("MXM", sql);
            regiondb.execSQL(sql);
        } catch (IOException e) {
            Log.e("MXM", e.getMessage());
        }
        return null;
    }
    
    protected void getFileFromUrl(String filePath, String urlString) {
        try {
            File file = new File(filePath);
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

            int size = conn.getContentLength();
            conn.connect();
            
            
            Log.i(tag, "Http Response code: "+String.valueOf(conn.getResponseCode()));
            Log.i(tag, "Http Response msg: "+conn.getResponseMessage());
            
            InputStream inputStream = conn.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(50);
            int currentByte = 0;
            
            while ((currentByte = bufferedInputStream.read()) != -1) {
                byteArrayBuffer.append((byte) currentByte);
            }
            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArrayBuffer.toByteArray());
            fos.close();
            Log.i(tag, "Downloaded "
                    + String.valueOf(size)
                    + " bytes in "
                    + ((System.currentTimeMillis() - startTime) / 1000.0)
                    + " seconds");
            
        } catch (MalformedURLException e) {
            Log.e(tag, e.getMessage());
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
        }
    }
    
    protected String readFileAsString(String filePath) throws java.io.IOException{
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) try { f.close(); } catch (IOException ignored) { }
        }
        return new String(buffer);
    }

}
