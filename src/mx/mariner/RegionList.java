// Modified by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RegionList {
    
    private List<Region> list;
    private Cursor cursor;
    private GemfCollection gemfCollection = new GemfCollection();
    private String[] gemfs;
    //private static final String tag = "RegionList";
    
    public RegionList(Context context, SQLiteDatabase regiondb) {
        list = new ArrayList<Region>();
        this.gemfs = gemfCollection.getFileList();
        
        cursor = regiondb.query("regions", null, null, null, null, null, null);
        cursor.moveToFirst();
        
        /* TABLE regions
        name          TEXT,
        description   TEXT,
        image         TEXT,
        size          INT,
        installeddate INT,
        latestdate    INT 
        */
        
        int name = cursor.getColumnIndex("name");
        int desc = cursor.getColumnIndex("description");
        int icon = cursor.getColumnIndex("image");
        int bytes = cursor.getColumnIndex("size");
        int installeddate = cursor.getColumnIndex("installeddate");
        int latestdate = cursor.getColumnIndex("latestdate");
        
        while (!cursor.isAfterLast()) {
            String regionName = cursor.getString(name);
            String status = findStatus( cursor.getInt(installeddate), cursor.getInt(latestdate), regionName);
            list.add( new Region(cursor.getString(icon), regionName, cursor.getString(desc), 
                    cursor.getInt(bytes), status) );
            cursor.moveToNext();
        }
        cursor.close();
    }
    
    private String findStatus(int idate, int ldate, String name) {
        String status = "not installed";
        Boolean installed = regionInstalled(name);
        if (installed)
            status = "installed";
        if ( installed && (idate==0) )
            status = "update available";
        if ( (ldate > idate) && !(installed) && (idate!=0) )
            status = "update available";
        return status;
    }
    
    private Boolean regionInstalled(String name) {
        for (int i=0; i<gemfs.length; i++) {
            if (gemfs[i].endsWith(name))
                return true;
        }
        return false;
    }
    
    public List<Region> getList() {
        return list;
    }
    
}
