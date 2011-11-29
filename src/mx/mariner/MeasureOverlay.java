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
    //private LocationManager mLocationManager;
    //private LocationListenerProxy mLocationListener;
    private Paint textPaint = new Paint();
    private Paint textBgPaint = new Paint();
    //private int screenWidth;
    //private int screenHeight;
    private final Rect mBounds = new Rect();
    private final int targetWidth = 7;
    //protected final Bitmap target;
    private boolean render = false;
    private MapActivity mapActivity;
    //private Location mLocation;
    private Location targetLocation = new Location("Target");
    private Point locationPoint = new Point();
    //private final int hudHeight = 30;
    
    //====================
    // Constructors
    //====================

    public MeasureOverlay(final MapActivity mapActivity) {
        this(mapActivity, new DefaultResourceProxyImpl(mapActivity));
    }
    
    public MeasureOverlay(final MapActivity mapActivity, final ResourceProxy pResourceProxy) {
        super(pResourceProxy);
        this.mapActivity = mapActivity;

        textPaint.setColor(mapActivity.getResources().getColor(R.color.hud_txt));
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Style.FILL);
        textPaint.setAlpha(255);
        textPaint.setTextSize(24);
        textPaint.setStrokeWidth(3);
        
        textBgPaint.setColor(mapActivity.getResources().getColor(R.color.hud_bg));
        
//        LinearLayout hud = (LinearLayout) mapActivity.findViewById(R.id.linearLayout_hud);
//        hudHeight = hud.getHeight();
        
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
        
        if (render && mapActivity.mLocation != null) {
            mBounds.set(mapView.getScreenRect(mBounds));
            
            int centerX = (int) mBounds.exactCenterX();
            int centerY = (int) mBounds.exactCenterY();
            canvas.drawCircle(centerX, centerY, targetWidth, textPaint);
            
            String distance;
            String bearing;
            
            //mapActivity.mLocation listener starts listening on MxmMyLocationOverlay.enableMyLocation();
            //if (mapActivity.mLocation != null) {
        	final Projection projection = mapView.getProjection();
            projection.toMapPixels(new GeoPoint(mapActivity.mLocation), locationPoint);
            
            IGeoPoint point = mapView.getMapCenter();
            float latitude = point.getLatitudeE6() / 1000000F;
            float longitude = point.getLongitudeE6() / 1000000F;
            targetLocation.setLatitude(latitude);
            targetLocation.setLongitude(longitude);
            
            float meters = mapActivity.mLocation.distanceTo(targetLocation);
            
            if (meters > 185.2f) {
                //distance in nautical miles if .1nm or more
            	float nm = meters / 1852.0f;
            	nm = (float) Math.round(nm * 100) / 100; //two digit precision
                distance = String.valueOf(nm) + " nm";
            } else {
                //distance in feet if less than .1nm
            	float ft = meters / 0.3048f;
            	ft = (float) Math.round(ft * 10) / 10; //single digit precision
                distance = String.valueOf(ft) + " ft";
            }
            
            
            float degrees = mapActivity.mLocation.bearingTo(targetLocation);
            
            if (degrees < 0) {
                degrees += 360;
            }
            int deg = Math.round(degrees);
            bearing = String.valueOf(deg) + "\u00B0 T";
            
            canvas.drawLine(locationPoint.x, locationPoint.y, centerX, centerY, textPaint);
            
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
            messages.add(distance);
            messages.add(bearing);
            
            int width = (int) textPaint.measureText(StringArrayListMaxLength(messages));
            int left;
            
            //draw box west of center
            if (mapActivity.mLocation.getLongitude() > longitude) {
                left = mBounds.left + (mBounds.right-centerX)/2 - width/2;
            }
            //draw box east of center
            else {
                left = mBounds.right - (mBounds.right-centerX)/2 - width/2;
            }
            int right = left + width;
            int height = (int) textPaint.getTextSize() * messages.size();
            int top = centerY - height/2;
            int bottom = top + height;
            canvas.drawRect(left-10, top, right+10, bottom+10 , textBgPaint);
            
            int i=1;
            for (String message:messages) {
                canvas.drawText(message, left, top + textPaint.getTextSize()*i, textPaint);
                i++;
            }
            
        }
    }
    
    //====================
    // Methods
    //====================
    
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

}
