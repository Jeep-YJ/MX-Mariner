// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.osmdroid.util.GEMFFile;

import android.os.Environment;
import android.util.Log;

public class GemfCollection {
    
    // ================
    // Constants
    // ================
    
    private static boolean debug = false;
    private static String tag = "MXM";
    
    // ================
    // Fields
    // ================
    
    private String gemfDir;
    private String[] gemfList;
    private String[] datList;
    private String[] regionList;
    private File fGemfDir;
    private int minZoom = 100;
    private int maxZoom = 0;
    private int gemfCount = 0;
    
    //====================
    // Constructors
    //====================
    
    public GemfCollection () {
        this(new String(Environment.getExternalStorageDirectory()+"/mxmariner"));    
    }
    
    public GemfCollection (final String dir) {
        //see if sd card directory is available
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //Log.i("MXM", "GemfCollection Directory:"+dir);
            establishCollection(dir);
            
            //set minimum and maximum zooms
            setZoomLevels();
        } else {
            
            Log.e("MXM", "SD Card:"+Environment.getExternalStorageState());
            gemfList = new String[] {"None"};
            datList = new String[] {""};
            regionList = new String[] {""};
        }
        
    }
    
    //====================
    // Methods
    //====================
    
    protected static String[] lstUnion(String[] arg0, String[] arg1) {
        HashSet<String> diff = new HashSet<String>(Arrays.asList(arg0));
        diff.retainAll(Arrays.asList(arg1));
        String[] a = new String[diff.size()];
        return diff.toArray(a);
    }
    
    private String[] filenameFilter(final String extention) {
        //filter directory files
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(extention);
            }
        };
        String[] flst = fGemfDir.list(filter);
        //trim extensions
        for (int i=0; i<flst.length; i++) {
            flst[i] = flst[i].replace(extention, "");
        }
        return flst;
    }
    
    private void establishCollection(String dir) {
        gemfDir = dir;
        fGemfDir = new File(gemfDir);
        
        //create directory if not there
        if (!fGemfDir.isDirectory()) {
            fGemfDir.mkdir();
            if (debug) {
                Log.i(tag, "Creating gemf directory: " + gemfDir);
            }
        }
        
        //get list of files ending in .gemf
        gemfList = filenameFilter(".gemf");
        Arrays.sort(gemfList);
        
        //get list of files ending in .dat
        datList = filenameFilter(".data");
        Arrays.sort(datList);
        
        //make sure .gemf has corresponding .dat file
        regionList = lstUnion(gemfList, datList);
    }
    
    private void setZoomLevels() {
        gemfCount = gemfList.length;
        if (debug) {
            Log.i(tag, "Gemf file found in collection: " + gemfCount);
        }
        for (int i=0; i<gemfCount; i++) {
            File location = new File(gemfDir + "/" + gemfList[i]+".gemf");
            if (debug) {
                Log.i(tag, "Gemf file " + i+1 + " " +gemfList[i]);
            }
            try {
                GEMFFile gemfile = new GEMFFile(location);
                int maxZ = (Integer) gemfile.getZoomLevels().toArray()[0];
                if (debug) {
                    Log.i(tag, "Max zoom " + maxZ);
                }
                if (maxZ>maxZoom)
                    maxZoom = maxZ;
                int minZ = (Integer) gemfile.getZoomLevels().toArray()[gemfile.getZoomLevels().size()-1];
                if (debug) {
                    Log.i(tag, "Min zoom " + minZ);
                }
                if (minZ<minZoom)
                    minZoom = minZ;
            } catch (FileNotFoundException e) {
                Log.e(tag, e.getMessage());
            } catch (IOException e) {
                Log.d(tag, e.getMessage());
            }
        }
        if (minZoom == 100)
                minZoom = maxZoom;
        if (debug) {
            Log.i(tag, "Min zoom in gemf collection: " + minZoom);
            Log.i(tag, "Max zoom in gemf collection: " + maxZoom);
        }
    }
    
    public String[] getFileList() {
        if (gemfList.length == 0)
            return new String[] {"None"};
        else
            return gemfList;
    }
    
    public String[] getRegionList() {
        return regionList;
    }
    
    public String getDir() {
        return gemfDir+"/";
    }
    
    public int getMinZoom() {
        return minZoom;
    }
    
    public int getMaxZoom() {
        return maxZoom;
    }
    
    public int getGemfCount() {
        return gemfList.length;
    }
    
}
