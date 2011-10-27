// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ChartDataStore {
    
    //====================
    // Fields
    //====================
    
    private SQLiteDatabase regiondb;
    private String region;
    //TODO: use region to only show selected region outlines
    
    //====================
    // Constructors
    //====================
    
    public ChartDataStore(SQLiteDatabase regiondb, String region) {
        this.regiondb = regiondb;
        this.region = region;
    }
    
    //====================
    // Methods
    //====================
    
    public ArrayList<String> getOutlines() {
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
    
}
