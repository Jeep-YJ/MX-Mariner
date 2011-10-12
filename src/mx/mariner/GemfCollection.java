// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

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
		FilenameFilter gemfilter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		        return name.endsWith(".gemf");
		    }
		}; 
		gemfList = fGemfDir.list(gemfilter);
		
		//set minimum and maximum zooms
		gemfCount = gemfList.length;
		if (debug) {
			Log.i(tag, "Gemf file found in collection: " + gemfCount);
		}
		for (int i=0; i<gemfCount; i++) {
			File location = new File(gemfDir + "/" + gemfList[i]);
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
	
	//====================
    // Methods
	//====================
	
	public String[] getFileList() {
		return gemfList;
	}
	
	public String getDir() {
		return gemfDir;
	}
	
	public int getMinZoom() {
		return minZoom;
	}
	
	public int getMaxZoom() {
		return maxZoom;
	}
	
	public int getGemfCount() {
		return gemfCount;
	}
	
}
