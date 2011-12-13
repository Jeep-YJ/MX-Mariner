// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner.marks;

import java.util.ArrayList;

import mx.mariner.R;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class WaypointMarkerIcons {
    /*
     * Note: this can easily be modified to load drawables from sdcard for custom markers
     * since waypoint markers are stored in database by name
     * 
     */
    private final ArrayList<Drawable> drawables = new ArrayList<Drawable>();
    private Context context;
    
    private final String[] markerNames = {"Mark 1", "Mark 2", "Mark 3", "Anchor",
            "Fish 1", "Fish 2", "Fish 3", "Shark",
            "Camp", "Dive", "Wreck", "Plane",
            "Sand", "Kelp", "Rock", "Food",
            "Service", "Fuel 1", "Fuel 2", "Fuel 3"};
    
    private int[] markerRids;
    
    public WaypointMarkerIcons(Context ctx) {
        context = ctx;
        if (isLargeScreen(ctx)) {
            markerRids = new int[] { R.drawable.mark1, R.drawable.mark2, R.drawable.mark3, R.drawable.anchor, 
                  R.drawable.fish1, R.drawable.fish2, R.drawable.fish3, R.drawable.shark,
                  R.drawable.camp, R.drawable.dive, R.drawable.wreck, R.drawable.plane,
                  R.drawable.sand, R.drawable.kelp, R.drawable.rock, R.drawable.food,
                  R.drawable.service, R.drawable.fuel1, R.drawable.fuel2, R.drawable.fuel3} ;
        } else {
            markerRids = new int[] { R.drawable.mark1small, R.drawable.mark2small, R.drawable.mark3small, R.drawable.anchorsmall, 
                    R.drawable.fish1small, R.drawable.fish2small, R.drawable.fish3small, R.drawable.sharksmall,
                    R.drawable.campsmall, R.drawable.divesmall, R.drawable.wrecksmall, R.drawable.planesmall,
                    R.drawable.sandsmall, R.drawable.kelpsmall, R.drawable.rocksmall, R.drawable.foodsmall,
                    R.drawable.servicesmall, R.drawable.fuel1small, R.drawable.fuel2small, R.drawable.fuel3small} ;
        }
        
        for (int id:markerRids) {
            drawables.add(context.getResources().getDrawable(id));
        }
    }
    
    public Drawable findDrawableByName(String markerName) {
        for (int position = 0; position < markerNames.length; position++){
            if (markerNames[position].equals(markerName))
                return context.getResources().getDrawable(markerRids[position]);
        }
        return context.getResources().getDrawable(markerRids[0]); //default icon
    }
    
    public int findPositoinByName(String markerName) {
        for (int position = 0; position < markerNames.length; position++){
            if (markerNames[position].equals(markerName))
                return position;
        }
        return 0;
    }
    
    public ArrayList<Drawable> getDrawables() {
        return drawables;
    }
    
    public String[] getNames() {
        return markerNames;
    }
    
    public int[] getRids() {
        return markerRids;
    }
    
    public static boolean isLargeScreen(Context ctx) {
        int mindim = 600;
        int width = ctx.getResources().getDisplayMetrics().widthPixels;
        int height = ctx.getResources().getDisplayMetrics().heightPixels;
        if (width < mindim || height < mindim) {
            return false;
        }
        return true;
    }

}
