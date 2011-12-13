// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details

package mx.mariner;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MeasureOverlay extends Overlay {
    private final int MODE_ARBITRARY = 0;
    private final int MODE_RELATIVE = 1; //default mode is relative to position or no where
    private int mode = 1;
    private boolean drawMeasurements = false;
    private Paint txtLinePaint = new Paint();
    private Paint txtLnBgPaint = new Paint();
    private Paint cursorPaint = new Paint();
    private final Rect mBounds = new Rect();
    private final int strokeWidth = 2;
    private final int targetWidth = 4;
    private final int textSize = 24;
    private MapActivity mapActivity;
    
    private Location originLocation = null;
    private Location targetLocation = new Location("Target");
    
    //====================
    // Constructors
    //====================

    public MeasureOverlay(final MapActivity mapActivity) {
        this(mapActivity, new DefaultResourceProxyImpl(mapActivity));
    }
    
    public MeasureOverlay(final MapActivity mapActivity, final ResourceProxy pResourceProxy) {
        super(pResourceProxy);
        this.mapActivity = mapActivity;

        txtLinePaint.setColor(mapActivity.getResources().getColor(R.color.greenglo));
        txtLinePaint.setAntiAlias(true);
        txtLinePaint.setStyle(Style.FILL);
        txtLinePaint.setAlpha(255);
        txtLinePaint.setTextSize(textSize);
        txtLinePaint.setStrokeWidth(strokeWidth);
        
        txtLnBgPaint.setColor(mapActivity.getResources().getColor(R.color.smokey));
        txtLnBgPaint.setAntiAlias(true);
        txtLnBgPaint.setStrokeWidth(strokeWidth*3);
        
        cursorPaint.setColor(mapActivity.getResources().getColor(R.color.smokey));
        cursorPaint.setAntiAlias(true);
        cursorPaint.setStrokeWidth(strokeWidth);
        
    }
    
    //====================
    // SuperClass Methods
    //====================
    
    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        
        if (shadow)
            return;
        
        if (mapView.isAnimating())
            return;
        
        //screen rectangle boundaries
        mBounds.set(mapView.getScreenRect(mBounds));
        final int centerX = (int) mBounds.exactCenterX();
        final int centerY = (int) mBounds.exactCenterY();
        
        //cursor cross-hairs
        canvas.drawCircle(centerX, centerY, strokeWidth, cursorPaint);
        canvas.drawLine(centerX+10, centerY, centerX+30, centerY, cursorPaint);
        canvas.drawLine(centerX-30, centerY, centerX-10, centerY, cursorPaint);
        canvas.drawLine(centerX, centerY+10,centerX, centerY+30, cursorPaint);
        canvas.drawLine(centerX, centerY-10,centerX, centerY-30, cursorPaint);
        
        if (mode == MODE_RELATIVE && mapActivity.mLocation != null) {
            originLocation = mapActivity.mLocation;
        }
        
        if (drawMeasurements && originLocation != null) {
            
            String distance;
            String bearing;
            
            final Projection projection = mapView.getProjection();
            final Point originPoint = projection.toMapPixels(new GeoPoint(originLocation), null);
            
            IGeoPoint point = mapView.getMapCenter();
            final float latitude = point.getLatitudeE6() / 1000000f;
            final float longitude = point.getLongitudeE6() / 1000000f;
            targetLocation.setLatitude(latitude);
            targetLocation.setLongitude(longitude);
            
            final float meters = originLocation.distanceTo(targetLocation);
            
            if (meters > 185.2f) {
                //distance in nautical miles if .1nm or more
                final float nm = (float) Math.round((meters / 1852.0f) * 100) / 100; //two digit precision
                distance = String.valueOf(nm) + " nm";
            } else {
                //distance in feet if less than .1nm
                final float ft = (float) Math.round((meters / 0.3048f) * 10) / 10; //single digit precision
                distance = String.valueOf(ft) + " ft";
            }
            
            
            float degrees = originLocation.bearingTo(targetLocation);
            
            if (degrees < 0) {
                degrees += 360;
            }
            final int deg = Math.round(degrees);
            bearing = String.valueOf(deg) + "\u00B0 T";
            

            canvas.drawCircle(centerX, centerY, targetWidth+strokeWidth, txtLnBgPaint);
            canvas.drawLine(originPoint.x, originPoint.y, centerX, centerY, txtLnBgPaint);
            canvas.drawCircle(centerX, centerY, targetWidth, txtLinePaint);
            canvas.drawLine(originPoint.x, originPoint.y, centerX, centerY, txtLinePaint);
            
            if (mode == MODE_RELATIVE){
                drawInfoBoxEastWest(canvas, latitude, longitude, distance, bearing, centerX, centerY);
            } else {
                drawInfoBoxNorthSouth(canvas, latitude, longitude, distance, bearing, centerX, centerY);
            }
            
            
        } else if (drawMeasurements) {
            IGeoPoint point = mapView.getMapCenter();
            final float latitude = point.getLatitudeE6() / 1000000F;
            final float longitude = point.getLongitudeE6() / 1000000F;
            
            canvas.drawCircle(centerX, centerY, targetWidth+strokeWidth, txtLnBgPaint);
            canvas.drawCircle(centerX, centerY, targetWidth, txtLinePaint);
            
            if (mode == MODE_RELATIVE) {
                drawInfoBoxEastWest(canvas, latitude, longitude, null, null, centerX, centerY);
            } else {
                drawInfoBoxNorthSouth(canvas, latitude, longitude, null, null, centerX, centerY);
            }
            
        }
    }
    
    //====================
    // Methods
    //====================
    
    private void drawInfoBoxEastWest(Canvas canvas, float latitude, float longitude, String distance, String bearing, int centerX, int centerY) {
        ArrayList<String> messages = new ArrayList<String>();
        
        //messages.add("Cursor");
        if (latitude < 0) {
            messages.add(String.valueOf(-latitude)+ "\u00B0 S");
        } else {
            messages.add(String.valueOf(latitude)+ "\u00B0 N");
        }
        if (longitude < 0) {
            messages.add(String.valueOf(-longitude)+ "\u00B0 W");
        } else {
            messages.add(String.valueOf(longitude)+ "\u00B0 E");
        }
        if ( distance!= null && bearing !=null) {
            messages.add(distance);
            messages.add(bearing);
        }
        
        final int width = (int) txtLinePaint.measureText(StringArrayListMaxLength(messages));
        final int left;
        
        if (originLocation != null) {
          //draw box west of center
            if (originLocation.getLongitude() > longitude) {
                left = mBounds.left + (mBounds.right-centerX)/2 - width/2;
            }
            //draw box east of center
            else {
                left = mBounds.right - (mBounds.right-centerX)/2 - width/2;
            }
        } else {
            left = mBounds.right - (mBounds.right-centerX)/2 - width/2;
        }
        
        final int right = left + width;
        final int height = (int) txtLinePaint.getTextSize() * messages.size();
        final int top = centerY - height/2;
        int bottom = top + height;
        canvas.drawRect(left-10, top, right+10, bottom+10 , txtLnBgPaint);
        
        int i=1;
        for (String message:messages) {
            canvas.drawText(message, left, top + txtLinePaint.getTextSize()*i, txtLinePaint);
            i++;
        }
    }
    
    private void drawInfoBoxNorthSouth(Canvas canvas, float latitude, float longitude, String distance, String bearing, int centerX, int centerY) {
        ArrayList<String> messages = new ArrayList<String>();
        
        //messages.add("Cursor");
        if (latitude < 0) {
            messages.add(String.valueOf(-latitude)+ "\u00B0 S");
        } else {
            messages.add(String.valueOf(latitude)+ "\u00B0 N");
        }
        if (longitude < 0) {
            messages.add(String.valueOf(-longitude)+ "\u00B0 W");
        } else {
            messages.add(String.valueOf(longitude)+ "\u00B0 E");
        }
        if ( distance!= null && bearing !=null) {
            messages.add(distance);
            messages.add(bearing);
        }
        
        final int width = (int) txtLinePaint.measureText(StringArrayListMaxLength(messages));
        final int top;
        
        final int left = mBounds.left + (mBounds.right-centerX) - (width/2);
        final int right = left + width;
        final int height = (int) txtLinePaint.getTextSize() * messages.size();
        
        //draw box north of center
        if (originLocation.getLatitude() < latitude) {
            top = mBounds.top - (mBounds.top-centerY)/2;
        }
        //draw box south of center
        else {
            top = mBounds.bottom + (mBounds.top-centerY)/2 - height;
        }
        //final int top = centerY - height/2;
        int bottom = top + height;
        canvas.drawRect(left-10, top, right+10, bottom+10 , txtLnBgPaint);
        
        int i=1;
        for (String message:messages) {
            canvas.drawText(message, left, top + txtLinePaint.getTextSize()*i, txtLinePaint);
            i++;
        }
    }
    
    public String StringArrayListMaxLength (ArrayList<String> array) {
        int max = 0;
        String longestString = null;
        for (String item:array) {
            if (item.length() > max) {
                max = item.length();
                longestString = item;
            }
        }
        return longestString;
    }
    
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            drawMeasurements = true;
        }
        
        if (event.getAction() == MotionEvent.ACTION_UP  && mode == MODE_RELATIVE) {
            drawMeasurements = false;
            mapView.invalidate();
        }
        return super.onTouchEvent(event, mapView);
    }
    
    public void disable() {
        setEnabled(false);
    }

    public void enable() {
        setEnabled(true);
    }
    
    protected void setModeArbitrary() {
        mode = MODE_ARBITRARY;
        drawMeasurements = true; //always draw in this mode
        //change paint to blue
        txtLinePaint.setColor(mapActivity.getResources().getColor(R.color.blueglo));
        
        //set origin point to current map center
        originLocation = new Location("Origin");
        IGeoPoint point = mapActivity.mapView.getMapCenter();
        final float latitude = point.getLatitudeE6() / 1000000F;
        final float longitude = point.getLongitudeE6() / 1000000F;
        originLocation.setLatitude(latitude);
        originLocation.setLongitude(longitude);
        
        //turn off follow and extra tool buttons
        mapActivity.mLocationOverlay.disableFollowLocation();
        //mapActivity.btnFollow.setVisibility(View.GONE);
        mapActivity.setExtraMenuButtonsEnabled(false);
        mapActivity.setFollowButtonEnabled(false);
        
        //add button for returning to normal mode
        //final Button setButton = new Button(mapActivity);
        final Button setButton = (Button) mapActivity.findViewById(R.id.done);
        setButton.setVisibility(View.VISIBLE);
        setButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                setButton.setVisibility(View.GONE);
                setModeRelative();
            }
        });
        
    }
    
    protected void setModeRelative() {
        mode = MODE_RELATIVE;
        drawMeasurements = false; //dont draw until next touch event
        //change paint to green
        txtLinePaint.setColor(mapActivity.getResources().getColor(R.color.greenglo));
        
        GeoPoint originPoint = new GeoPoint(originLocation);
        
        //reset originLocation
        originLocation = mapActivity.mLocation;
        
        mapActivity.setExtraMenuButtonsEnabled(true);
        mapActivity.setFollowButtonEnabled(true);

        mapActivity.mapController.animateTo(originPoint);
    }

}
