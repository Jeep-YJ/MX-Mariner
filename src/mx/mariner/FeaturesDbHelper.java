// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FeaturesDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "features.s3db";
    protected Context context;
    
    public FeaturesDbHelper(Context context) {
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
        db.execSQL(context.getString(R.string.sql_waypoints_table));
        db.execSQL(context.getString(R.string.sql_routes_table));
        db.execSQL(context.getString(R.string.sql_routepoints_table));
        db.execSQL(context.getString(R.string.sql_tracks_table));
        db.execSQL(context.getString(R.string.sql_trackpoints_table));
    }
    
}
