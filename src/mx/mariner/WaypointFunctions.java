package mx.mariner;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;

public class WaypointFunctions {
    
    public static void addWayPointsToOverlay(Context context, ItemizedIconOverlay<OverlayItem> waypointOverlay, SQLiteDatabase featuresDb) {
        Cursor cursor = featuresDb.query("waypoints", null, null, null, null, null, null);
        cursor.moveToFirst();
        final int columname = cursor.getColumnIndex("name");
        final int columdesc = cursor.getColumnIndex("description");
        final int latitude = cursor.getColumnIndex("latitude");
        final int longitude = cursor.getColumnIndex("longitude");
        final int iconindex = cursor.getColumnIndex("iconindex");
        while (!cursor.isAfterLast()) {
            final GeoPoint point = new GeoPoint(cursor.getInt(latitude), cursor.getInt(longitude));
            final OverlayItem overlayItem = new OverlayItem(cursor.getString(columname), cursor.getString(columdesc), point);
            Drawable marker = context.getResources().getDrawable(NewWaypointDialog.iconIds[cursor.getInt(iconindex)]);
            overlayItem.setMarker(marker);
            waypointOverlay.addItem(overlayItem);
            cursor.moveToNext();
        }
        cursor.close();
    }
    
    public static boolean isWayPoint(SQLiteDatabase featuresDb, int latitude, int longitude) {
        Cursor cursor = featuresDb.query("waypoints", new String[] {"latitude", "longitude"}, null, null, null, null, null);
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

}
