// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner.marks;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WaypointDbFunctions {
    private final static boolean debug = false;
    private final static String tag = "MXM";
    
/*        ##database structure
 *        CREATE TABLE wpt (
 *        name      TEXT,    ## 0
 *        desc      TEXT,    ## 1
 *        sym       TEXT,    ## 2
 *        latitude  INTEGER, ## 3
 *        longitude INTEGER  ## 4
 *    );
 */
    
    public static void addWayPointsFromDbToOverlay(Context context, ItemizedIconOverlay<OverlayItem> waypointOverlay, 
            SQLiteDatabase featuresDb, WaypointMarkerIcons waypointMarkerIcons) {
        Cursor cursor = featuresDb.query("wpt", null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            final GeoPoint point = new GeoPoint(cursor.getInt(3), cursor.getInt(4));
            final OverlayItem overlayItem = new OverlayItem(cursor.getString(0), cursor.getString(1), point);
            overlayItem.setMarker(waypointMarkerIcons.findDrawableByName(cursor.getString(2)));
            waypointOverlay.addItem(overlayItem);
            cursor.moveToNext();
        }
        cursor.close();
    }
    
    public static boolean isWayPointInDb(SQLiteDatabase featuresDb, int latitude, int longitude) {
        Cursor cursor = featuresDb.query("wpt", new String[] {"latitude", "longitude"}, null, null, null, null, null);
        cursor.moveToFirst();
        boolean result = false;
        while (!cursor.isAfterLast()) {
            if (cursor.getInt(0) == latitude && cursor.getInt(1) == longitude) {
                result = true;
            }
            cursor.moveToNext();
        }
        cursor.close();
        
        return result;
    }
    
    public static ContentValues getWaypointDetailsFromDb(SQLiteDatabase featuresDb, int latitude, int longitude) {
        ContentValues details = new ContentValues(3);
        Cursor cursor = featuresDb.query("wpt", null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getInt(3) == latitude && cursor.getInt(4) == longitude) {
                details.put("name", cursor.getString(0));
                details.put("desc", cursor.getString(1));
                details.put("sym", cursor.getString(2));
            }
            cursor.moveToNext();
        }
        cursor.close();
        return details;
    }
    
    public static void addWayPointToDb(SQLiteDatabase featuresDb, String name, String desc, String sym,
            int latitude, int longitude ) {
        if (debug) {
            Log.i(tag, "Adding waypoint to database name:"+
                    name+" desc:"+desc+" sym: "+sym+" lat:"+
                    String.valueOf(latitude)+" lon:"+String.valueOf(longitude));
        }
        ContentValues values = new ContentValues(4);
        values.put("name", name);
        values.put("desc", desc);
        values.put("sym", sym);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        featuresDb.insert("wpt", null, values);
    }
    
    public static void deleteWaypointFromDb(SQLiteDatabase featuresDb, int latitude, int longitude) {
        if (debug) {
            Log.i(tag, "Deleting waypoint from database at lat:"+
                    String.valueOf(latitude)+" long:"+String.valueOf(longitude));
        }
        String sql = String.format("delete from wpt where latitude is %s and longitude is %s;", latitude, longitude);
        featuresDb.execSQL(sql);
    }

}
