// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.io.File;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataStore {
	
	//====================
	// Fields
	//====================
	
	private SQLiteDatabase dataStore;
	
	//====================
    // Constructors
	//====================
	
	public DataStore(String fname) {
		File dbfile = new File(fname); 
		dataStore = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
	}
	
	//====================
    // Methods
	//====================
	
	public ArrayList<String> getOutlines() {
		ArrayList<String> routes = new ArrayList<String>();
		Cursor cursor = dataStore.query("charts", null, null, null, null, null, null);
		cursor.moveToFirst();
		int column = cursor.getColumnIndex("outline");
		while (!cursor.isAfterLast()) {
			routes.add(cursor.getString(column));
			cursor.moveToNext();
		}
		cursor.close();
		return routes;
	}
	
	public void Close(){
		dataStore.close();
	}
	
}
