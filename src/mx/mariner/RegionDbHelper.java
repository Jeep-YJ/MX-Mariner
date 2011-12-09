// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RegionDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "regions.s3db";
    //private static final String tag = "MXM";

    protected Context context;
    
    public RegionDbHelper(Context context) {
            super(context, DB_NAME, null, 1);
            this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Auto-generated method stub
    }
    
    private void createDatabase(SQLiteDatabase db) {
        db.execSQL(context.getString(R.string.sql_regions_table));
        db.execSQL(context.getString(R.string.sql_charts_table));
              
        //these arrays are all the same length
        String[] names = context.getResources().getStringArray(R.array.region_names);
        String[] descs = context.getResources().getStringArray(R.array.region_descriptions);
        String[] images = context.getResources().getStringArray(R.array.region_icons);
        int[] dates = context.getResources().getIntArray(R.array.region_dates);
        int[] bytes = context.getResources().getIntArray(R.array.region_bytes);
          
        /* TABLE regions
            name          TEXT,
            description   TEXT,
            image         TEXT,
            size          INT,
            installeddate INT,
            latestdate    INT 
        */
          
        for (int i=0; i<names.length; i++) {
            String sql = "insert into regions values (" + 
                "\'"+names[i]+"\',"+
                "\'"+descs[i]+"\',"+
                "\'"+images[i]+"\',"+
                "\'"+String.valueOf(bytes[i])+"\',"+
                "\'"+"0"+"\',"+
                "\'"+String.valueOf(dates[i])+"\')";
        //Log.i(tag, sql);
        db.execSQL(sql);
      }    
    }
    
}
