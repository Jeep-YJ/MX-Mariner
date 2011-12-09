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

public class MeasureOverlay extends Overlay {
    private Paint txtLinePaint = new Paint();
    private Paint txtLnBgPaint = new Paint();
    private Paint pen = new Paint();
    private final Rect mBounds = new Rect();
    private final int strokeWidth = 2;
    private final int targetWidth = 4;
    private final int textSize = 24;
    private boolean render = false;
    private MapActivity mapActivity;
    private Location targetLocation = new Location("Target");
    private Point locationPoint = new Point();
    
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
        
        pen.setColor(mapActivity.getResources().getColor(R.color.smokey));
        pen.setAntiAlias(true);
        pen.setStrokeWidth(strokeWidth);
        
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
        
        //target cursor
        mBounds.set(mapView.getScreenRect(mBounds));
        final int centerX = (int) mBounds.exactCenterX();
        final int centerY = (int) mBounds.exactCenterY();
        canvas.drawCircle(centerX, centerY, strokeWidth, pen);
        canvas.drawLine(centerX+10, centerY, centerX+30, centerY, pen);
        canvas.drawLine(centerX-30, centerY, centerX-10, centerY, pen);
        canvas.drawLine(centerX, centerY+10,centerX, centerY+30, pen);
        canvas.drawLine(centerX, centerY-10,centerX, centerY-30, pen);
        
        if (render && mapActivity.mLocation != null) {
            //mBounds.set(mapView.getScreenRect(mBounds));
            
            //final int centerX = (int) mBounds.exactCenterX();
            //final int centerY = (int) mBounds.exactCenterY();
            
            String distance;
            String bearing;

            final Projection projection = mapView.getProjection();
            projection.toMapPixels(new GeoPoint(mapActivity.mLocation), locationPoint);
            
            IGeoPoint point = mapView.getMapCenter();
            final float latitude = point.getLatitudeE6() / 1000000F;
            final float longitude = point.getLongitudeE6() / 1000000F;
            targetLocation.setLatitude(latitude);
            targetLocation.setLongitude(longitude);
            
            final float meters = mapActivity.mLocation.distanceTo(targetLocation);
            
            if (meters > 185.2f) {
                //distance in nautical miles if .1nm or more
                final float nm = (float) Math.round((meters / 1852.0f) * 100) / 100; //two digit precision
                distance = String.valueOf(nm) + " nm";
            } else {
                //distance in feet if less than .1nm
                final float ft = (float) Math.round((meters / 0.3048f) * 10) / 10; //single digit precision
                distance = String.valueOf(ft) + " ft";
            }
            
            
            float degrees = mapActivity.mLocation.bearingTo(targetLocation);
            
            if (degrees < 0) {
                degrees += 360;
            }
            final int deg = Math.round(degrees);
            bearing = String.valueOf(deg) + "\u00B0 T";
            

            canvas.drawCircle(centerX, centerY, targetWidth+strokeWidth, txtLnBgPaint);
            canvas.drawLine(locationPoint.x, locationPoint.y, centerX, centerY, txtLnBgPaint);
            canvas.drawCircle(centerX, centerY, targetWidth, txtLinePaint);
            canvas.drawLine(locationPoint.x, locationPoint.y, centerX, centerY, txtLinePaint);
            
            drawInfoBox(canvas, latitude, longitude, distance, bearing, centerX, centerY);
            
        } else if (render) {
            IGeoPoint point = mapView.getMapCenter();
            final float latitude = point.getLatitudeE6() / 1000000F;
            final float longitude = point.getLongitudeE6() / 1000000F;
            
            //mBounds.set(mapView.getScreenRect(mBounds));
            //final int centerX = (int) mBounds.exactCenterX();
            //final int centerY = (int) mBounds.exactCenterY();
            canvas.drawCircle(centerX, centerY, targetWidth+strokeWidth, txtLnBgPaint);
            canvas.drawCircle(centerX, centerY, targetWidth, txtLinePaint);
            drawInfoBox(canvas, latitude, longitude, null, null, centerX, centerY);
        }
    }
    
    //====================
    // Methods
    //====================
    
    private void drawInfoBox(Canvas canvas, float latitude, float longitude, String distance, String bearing, int centerX, int centerY) {
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
        
        if (mapActivity.mLocation != null) {
          //draw box west of center
            if (mapActivity.mLocation.getLongitude() > longitude) {
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
        //TODO: detect long press here and lock measure overlay on and release on MotionEvent.ACTION_MOVE
        //target circle turns red when locked on instead of green
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            render = true;
        }
        
        if (event.getAction() == MotionEvent.ACTION_UP) {
            render = false;
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

}
