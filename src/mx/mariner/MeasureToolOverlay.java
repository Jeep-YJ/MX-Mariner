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
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MeasureToolOverlay extends Overlay {
    private Paint txtLinePaint = new Paint();
    private Paint txtLnBgPaint = new Paint();
    private Paint pen = new Paint();
    private final Rect mBounds = new Rect();
    private final int strokeWidth = 2;
    private final int targetWidth = 4;
    private final int textSize = 24;
    private MapActivity mapActivity;
    
    private Location originLocation = new Location("Origin");
    private Location targetLocation = new Location("Target");
    private Point originLocationPoint = new Point();
        
    public MeasureToolOverlay(final MapActivity context) {
        this(context, new DefaultResourceProxyImpl(context));
    }
    
    public MeasureToolOverlay(final MapActivity context, final ResourceProxy pResourceProxy) {
        super(pResourceProxy);
        mapActivity = context;
        
        txtLinePaint.setColor(mapActivity.getResources().getColor(R.color.blueglo));
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
        
        String distance;
        String bearing;
        
        //set originLocationPoint
        final Projection projection = mapView.getProjection();
        projection.toMapPixels(new GeoPoint(originLocation), originLocationPoint);
        
        IGeoPoint point = mapView.getMapCenter();
        final float latitude = point.getLatitudeE6() / 1000000F;
        final float longitude = point.getLongitudeE6() / 1000000F;
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
        canvas.drawLine(originLocationPoint.x, originLocationPoint.y, centerX, centerY, txtLnBgPaint);
        canvas.drawCircle(centerX, centerY, targetWidth, txtLinePaint);
        canvas.drawLine(originLocationPoint.x, originLocationPoint.y, centerX, centerY, txtLinePaint);
        
        drawInfoBox(canvas, latitude, longitude, distance, bearing, centerX, centerY);
        
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
    
    public void disable() {
        setEnabled(false);
    }

    public void enable() {
        setEnabled(true);
    }
    
    public void setOriginLocation(IGeoPoint point) {
        final float latitude = point.getLatitudeE6() / 1000000F;
        final float longitude = point.getLongitudeE6() / 1000000F;
        originLocation.setLatitude(latitude);
        originLocation.setLongitude(longitude);
    }
    
    protected void StartMeasure() {
        IGeoPoint point = mapActivity.mapView.getMapCenter();
        setOriginLocation(point);
        mapActivity.measureOverlay.disable();
        this.enable();
        mapActivity.mLocationOverlay.disableFollowLocation();
        mapActivity.btnFollow.setVisibility(View.GONE);
        mapActivity.btnMenu.setVisibility(View.GONE);
        mapActivity.btnMeasure.setVisibility(View.GONE);
        mapActivity.btnTrack.setVisibility(View.GONE);
        mapActivity.btnRoute.setVisibility(View.GONE);
        mapActivity.btnWaypoint.setVisibility(View.GONE);
        final Button setButton = new Button(mapActivity);
        //setButton.setText("Done");
        setButton.setBackgroundDrawable(mapActivity.getResources().getDrawable(R.drawable.donebtn));
        final FrameLayout mainFrame = (FrameLayout) mapActivity.findViewById(R.id.mainframe);
        final RelativeLayout btnFrame = new RelativeLayout(mapActivity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //params.addRule(RelativeLayout.CENTER_IN_PARENT);
        int width = mapActivity.getResources().getDisplayMetrics().widthPixels;
        int height = mapActivity.getResources().getDisplayMetrics().heightPixels;
        int left = (int) (width * 0.666f);
        int top = (int) (height / 2);
        params.setMargins(left, top, 0, 0);
        setButton.setLayoutParams(params);
        btnFrame.addView(setButton);
        mainFrame.addView(btnFrame);
        setButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                StopMeasure();
                mainFrame.removeView(btnFrame);
            }
        });
    }
    
    private void StopMeasure() {
        mapActivity.measureOverlay.enable();
        this.disable();
        mapActivity.btnMenu.setVisibility(View.VISIBLE);
        mapActivity.btnMeasure.setVisibility(View.VISIBLE);
        //mapActivity.btnTrack.setVisibility(View.VISIBLE);
        //mapActivity.btnRoute.setVisibility(View.VISIBLE);
        mapActivity.btnWaypoint.setVisibility(View.VISIBLE);
        mapActivity.btnFollow.setVisibility(View.VISIBLE);
    }
    
}

