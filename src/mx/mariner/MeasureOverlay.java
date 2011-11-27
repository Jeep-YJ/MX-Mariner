package mx.mariner;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.LocationListenerProxy;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;

public class MeasureOverlay extends Overlay implements LocationListener {
    private LocationManager mLocationManager;
    private LocationListenerProxy mLocationListener;
    private Paint textPaint = new Paint();
    private int screenWidth;
    private int screenHeight;
    private Paint paint = new Paint();
    private final Rect mBounds = new Rect();
    protected final Bitmap target;
    private int offsetX;
    private int offsetY;
    private boolean render = false;
    private Location mLocation;
    private Location targetLocation = new Location("Target");
    
    //====================
    // Constructors
    //====================

    public MeasureOverlay(final Activity activity) {
        this(activity, new DefaultResourceProxyImpl(activity));
    }
    
    public MeasureOverlay(final Activity activity, final ResourceProxy pResourceProxy) {
        super(pResourceProxy);
        mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListenerProxy(mLocationManager);
        mLocationListener.startListening(this, 0, 0.0f);
        screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        target = BitmapFactory.decodeResource( activity.getResources(), R.drawable.target);
        offsetX = (int) ((screenWidth / 2)-(target.getWidth() / 2 - .5f));
        offsetY = (int) ((screenHeight / 2)-(target.getHeight() / 2 - .5f));

        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Style.FILL);
        textPaint.setAlpha(255);
        textPaint.setTextSize(24);
        
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
        
        if (render) {
            
            
            mBounds.set(mapView.getScreenRect(mBounds));
            mBounds.offset(offsetX, offsetY);
            mBounds.set(mBounds.left, mBounds.top, mBounds.left + target.getWidth(),
                    mBounds.top + target.getHeight());
            canvas.drawBitmap(target, null, mBounds, paint);
            
            float tx = mBounds.exactCenterX() + target.getWidth();
            float ty = mBounds.exactCenterY();
            
            String distance;
            String bearing;
            
            if (mLocation != null) {
                IGeoPoint point = mapView.getMapCenter();
                float latitude = point.getLatitudeE6() / 1000000F;
                float longitude = point.getLongitudeE6() / 1000000F;
                targetLocation.setLatitude(latitude);
                targetLocation.setLongitude(longitude);
                
                float meters = mLocation.distanceTo(targetLocation);
                if (meters > 185.2f) {
                    //distance in nautical miles if .1nm or more
                    distance = String.valueOf(meters/1852.0f);
                    canvas.drawText(distance+" nm", tx, ty, textPaint);
                } else {
                    //distance in feet
                    distance = String.valueOf(meters/0.3048f);
                    canvas.drawText(distance+" ft", tx, ty, textPaint);
                }
                
                float degrees = mLocation.bearingTo(targetLocation);
                if (degrees < 0) {
                    degrees += 360;
                }
                bearing = String.valueOf(degrees);
                canvas.drawText(bearing+"\u00B0", tx, ty+34, textPaint);
            }
        }
    }
    
    //====================
    // Methods
    //====================
    
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            render = true;
        }
        
        if (event.getAction() == MotionEvent.ACTION_UP) {
            render = false;
            mapView.invalidate();
        }
        return super.onTouchEvent(event, mapView);
    }
    
    public void disableMeasure() {
        setEnabled(false);
    }

    public void enableMeasure() {
        setEnabled(true);
    }

    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub
        
    }
}
