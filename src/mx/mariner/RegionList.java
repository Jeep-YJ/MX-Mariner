package mx.mariner;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RegionList {
	
	private List<Region> list;
	private Cursor cursor;
	
	public RegionList(Context context, SQLiteDatabase manifest) {
		list = new ArrayList<Region>();
		
		cursor = manifest.query("regions", null, null, null, null, null, null);
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
			list.add( new Region(cursor.getString(icon), cursor.getString(name), cursor.getString(desc), 
					cursor.getInt(bytes), findStatus(cursor.getInt(installeddate), cursor.getInt(latestdate))) );
			cursor.moveToNext();
		}
		cursor.close();
	}
	
	private String findStatus(int idate, int ldate) {
		if (idate == 0) {
			return "not installed";
		} else if (ldate < idate) {
			return "update available";
		} else {
			return "installed";
		}
	}
	
	public List<Region> getList() {
		return list;
	}
	
}
