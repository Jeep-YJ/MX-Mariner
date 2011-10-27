// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import android.graphics.Paint;
import android.util.Log;


public class ChartOutlines {
    
    //====================
    // Fields
    //====================
    
    private ArrayList<Object> outlinePathOverlays;
    
    //====================
    // Constructors
    //====================
    
    public ChartOutlines() {
        outlinePathOverlays = new ArrayList<Object>();
    }
    
    //====================
    // Methods
    //====================
    
    public void addPathOverlay(int color, ResourceProxy mResourceProxy, String coordinates) {
        PathOverlay path = new PathOverlay(color, mResourceProxy);
        Paint pPaint = new Paint();
        pPaint.setStrokeWidth(1.0f);
        pPaint.setColor(color);
        pPaint.setStyle(Paint.Style.STROKE);
        path.setPaint(pPaint);
        
        String[] coord = coordinates.split(":");
        for (int i=0; i<coord.length; i++) {
            String[] ll = coord[i].split(",");
            if (ll.length == 2)
                path.addPoint(new GeoPoint(Double.parseDouble(ll[0]), Double.parseDouble(ll[1])));
            else
                Log.i("MXM", coord[i]);
        }
        
        outlinePathOverlays.add(path);
    }
    
    public ArrayList<Object> getPaths() {
        return outlinePathOverlays;
    }
    
    public void clearPaths() {
        outlinePathOverlays.clear();
    }

}
