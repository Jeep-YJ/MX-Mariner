// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.StatFs;

public class ChartDataStore {
    
    //====================
    // Fields
    //====================
    
    private SQLiteDatabase regiondb;
    //TODO: use region to only show selected region outlines
    
    //====================
    // Constructors
    //====================
    
    public ChartDataStore(SQLiteDatabase regiondb) {
        this.regiondb = regiondb;
    }
    
    //====================
    // Methods
    //====================
    
    protected ArrayList<String> getOutlines(String region) {
        ArrayList<String> routes = new ArrayList<String>();
        Cursor cursor = regiondb.query("charts", null, "region='"+region+"'", null, null, null, null);
        cursor.moveToFirst();
        int column = cursor.getColumnIndex("outline");
        while (!cursor.isAfterLast()) {
            routes.add(cursor.getString(column));
            cursor.moveToNext();
        }
        cursor.close();
        return routes;
    }
    
    protected int GetRegionBytes(String region) {
        Cursor cursor = regiondb.query("regions", null, "name='"+region+"'", null, null, null, null);
        cursor.moveToFirst();
        int column = cursor.getColumnIndex("size");
        int bytes = cursor.getInt(column);
        cursor.close();
        return bytes;
    }
    
    protected int SdAvailableBytes() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double availableBytes = (double)stat.getAvailableBlocks() *(double)stat.getBlockSize();
        return (int) availableBytes;
    }
    
}
