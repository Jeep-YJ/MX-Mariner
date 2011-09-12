// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package aocpn.mapdroid;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MapView extends View
{
	private int m_TileSize;
	private final String TAG = "MV";

	private final int m_MinZoom = 1;
	private final int m_MaxZoom = 20;
	
	public final double start_lat = 47.620000; //64.4985
	public final double start_lon = -122.200000; //-165.4315

	private LatLon m_PageCentre;
	private boolean m_zoomOut = true; //false... zooming in
	private boolean m_zoomAction = false;
	private boolean m_tilesAvailable = false;
	private int m_ZoomLevel;
	private Set<Integer> m_setZoomLevels = new TreeSet<Integer>();
	private PointF m_MotionStartPoint = new PointF();
	private Pixels m_MotionStartCentre = new Pixels();
	private boolean m_bMoving = false;

	private static final boolean DebugOn = false;
	private Location userLocation = null;

	private static final int LOCATION_INNER_RADIUS = 6;
	private static final int LOCATION_MID_RADIUS = 9;
	private static final int LOCATION_OUTER_RADIUS = 10;

	private Paint accuracyFillPaint;
	private Paint accuracyStrokePaint;

	private Paint locationInnerPaint;
	private Paint locationMidPaint;
	private Paint locationOuterPaint;

	private ReaderInterface imageReader = null;

	private boolean m_bFollowPosition = true;

	SharedPreferences prefs;
	
	public MapView(Context context, AttributeSet attr)
	{
		super(context, attr);
		Initialise();
	}

	public MapView(Context context)
	{
        super(context);
        Initialise();
	}

	private MapDroidActivity parent = null;

	public void SelectSource(int source)
	{
		if (imageReader != null)
		{
			imageReader.SelectSource(source);
			invalidate();
		}
	}

	public void AcceptAnySource(int source)
	{
		if (imageReader != null)
		{
			imageReader.AcceptAnySource();
			invalidate();
		}
	}

	public void EnableSource(int source)
	{
		if (imageReader != null)
		{
			imageReader.EnableSource(source);
			invalidate();
		}
	}

	public void DisableSource(int source)
	{
		if (imageReader != null)
		{
			imageReader.DisableSource(source);
			invalidate();
		}
	}


	public HashMap<Integer,String> GetSourceList()
	{
		if (imageReader != null)
			return imageReader.GetSourceList();
		else
			return new HashMap<Integer,String>();
	}

	public HashMap<Integer,Boolean> GetSourceStates()
	{
		if (imageReader != null)
			return imageReader.GetSourceStates();
		else
			return new HashMap<Integer,Boolean>();
	}

	void SetParent(MapDroidActivity p)
	{
		parent = p;
	}

	void SetPreferences(SharedPreferences preferences)
	{
		prefs = preferences;
		SetMapLocation(prefs.getString("PrefMapLocation", this.getContext().getString(R.string.chart_dir) ));
		SetFollowPosition(prefs.getBoolean("Follow Position", true));
		Location location = new Location("Preferences");
		location.setLatitude((double) prefs.getFloat("Latitude", (float) start_lat));
		location.setLongitude((double) prefs.getFloat("Longitude", (float) start_lon));
		SetLocation(location);
		//TODO: 
		//int zoom = prefs.getInt("Zoom", -1);
		int zoom = 9;
		if (zoom != -1)
		{
			SetZoomLevel(zoom);
		}
	}

	void StorePreferences()
	{
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("Follow Position", m_bFollowPosition);
		editor.putFloat("Latitude", (float) m_PageCentre.latitude);
		editor.putFloat("Longitude", (float) m_PageCentre.longitude);
		editor.putInt("Zoom", m_ZoomLevel);
		editor.commit();
	}

	void Initialise()
	{
		accuracyFillPaint = new Paint();
		accuracyFillPaint.setARGB(100, 140, 140, 220);
		accuracyFillPaint.setStyle(Paint.Style.FILL);
		accuracyStrokePaint = new Paint();
		accuracyStrokePaint.setARGB(255, 140, 140, 220);
		accuracyStrokePaint.setStyle(Paint.Style.STROKE);
		locationOuterPaint = new Paint();
		locationOuterPaint.setARGB(255, 0, 0, 0);
		locationOuterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		locationMidPaint = new Paint();
		locationMidPaint.setARGB(255, 190, 190, 190);
		locationMidPaint.setStyle(Paint.Style.FILL);
		locationInnerPaint = new Paint();
		locationInnerPaint.setARGB(255, 79, 206, 240);
		locationInnerPaint.setStyle(Paint.Style.FILL);

		SetTileSize(256);
		m_PageCentre = new LatLon();
        m_PageCentre.latitude = start_lat;
        m_PageCentre.longitude = start_lon;
        setOnTouchListener(TouchListener);

    }

    public void SetLocation(Location location)
	{
		if (location == null)
			return;

		userLocation = location;

		if (m_bFollowPosition)
		{
			m_PageCentre.latitude = location.getLatitude();
			m_PageCentre.longitude = location.getLongitude();
			invalidate();
		}
	}

	public void SetFollowPosition(boolean bFollow)
	{
		m_bFollowPosition = bFollow;
		if ((m_bFollowPosition) && (userLocation != null))
		{
			m_PageCentre.latitude = userLocation.getLatitude();
			m_PageCentre.longitude = userLocation.getLongitude();
			invalidate();
		}

		if (parent != null)
		{
			parent.UpdateFollowDisplay(m_bFollowPosition);
		}
	}
	
	private OnTouchListener TouchListener = new OnTouchListener()
	{
		public boolean onTouch(View v, MotionEvent event)
		{
			if (DebugOn)
				Log.d(TAG, "Touch event: Action = " + event.getAction()
						+ "; Pos: (" + event.getX() + "," + event.getY() + ") ; " + event.toString());
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					if (DebugOn)
						Log.d(TAG, "Press event");
					OnPressEvent(event);
					break;
				case MotionEvent.ACTION_MOVE:
					if (DebugOn)
						Log.d(TAG, "Move event");
					OnMoveEvent(event);
					break;
				case MotionEvent.ACTION_UP:
					if (DebugOn)
						Log.d(TAG, "Release event");
					OnReleaseEvent(event);
					break;
				case MotionEvent.ACTION_CANCEL:
					if (DebugOn)
						Log.d(TAG, "Cancel event");
					break;
			}
			return true; // Have consumed the event
		}

	};

	private void OnPressEvent(MotionEvent event)
	{
		m_MotionStartPoint = new PointF(event.getX(), event.getY());
		m_MotionStartCentre = Mercator.LatLonToPixels(m_PageCentre,
				m_ZoomLevel, m_TileSize);
		m_bMoving = true;
	}
	private void OnMoveEvent(MotionEvent event)
	{
		if ( ! m_bMoving)
		{
			return;
		}
		SetFollowPosition(false);
		PointF pos = new PointF(event.getX(), event.getY());
		Pixels pc = new Pixels();
		pc.px = m_MotionStartCentre.px;
		pc.py = m_MotionStartCentre.py;
		pc.px += m_MotionStartPoint.x - pos.x;
		pc.py += pos.y - m_MotionStartPoint.y;
		m_PageCentre = Mercator.PixelsToLatLon(pc, m_ZoomLevel, m_TileSize);
		invalidate();
	}
	private void OnReleaseEvent(MotionEvent event)
	{
		m_bMoving = false;
	}
	
	public void ZoomIn()
	{
		m_zoomOut = false;
		m_zoomAction = true;
		int existingZoom = m_ZoomLevel;
		for (int newZoom = existingZoom+1;newZoom<=m_MaxZoom;newZoom++)
		{
			//see if there are any tiles to show if not skip to 
			if (m_setZoomLevels.contains(newZoom))
			{
				// Use this zoom level
				m_ZoomLevel = newZoom;
				if (DebugOn)
					Log.d(TAG, "Using zoom level " + newZoom);
				break;
			}
		}
		invalidate();
	}

	public void ZoomOut()
	{
		m_zoomOut = true;
		m_zoomAction = true;
		int existingZoom = m_ZoomLevel;
		for (int newZoom = existingZoom-1;newZoom>=m_MinZoom;newZoom--)
		{
			if (m_setZoomLevels.contains(newZoom))
			{
				// Use this zoom level
				m_ZoomLevel = newZoom;
				if (DebugOn)
					Log.d(TAG, "Using zoom level " + newZoom);
				break;
			}
		}
		invalidate();
	}

	Tile FindTileContaining(LatLon l, int zoom)
	{
		return Mercator.LatLonToOSMTile(l, zoom, m_TileSize);
	}

	void SetTileSize(int tilesize)
	{
		m_TileSize = tilesize;
	}

	void SetMapLocation(String location)
	{
		Set<Integer> setZoomLevels;

		// Close any open file handles
		if (imageReader != null)
		{
			imageReader.Close();
			imageReader = null;
		}

		File mapFile = new File(location);

		if ( ! mapFile.exists())
		{
			if (DebugOn)
				Log.d(TAG, "Could not find map store location: " + mapFile.getAbsolutePath());
			return;
		}

		if (mapFile.isDirectory())
		{
			imageReader = new MaverickReader();
		}
		else
		{
			// Presumably a GEMF file
			if (DebugOn)
				Log.d(TAG, "Not a directory: GEMF?");
			imageReader = new GEMFReader();
		}

		imageReader.Initialise(location);
		if ( ! imageReader.IsOpen())
		{
			Log.w(TAG, "Could not open location");
			return;
		}

		setZoomLevels = imageReader.GetZoomLevels();
		if (setZoomLevels.size() == 0)
		{
			if (DebugOn)
				Log.d(TAG, "No zoom levels found");
			m_ZoomLevel = 0xFFFF;
			return;
		}

		if ( ! setZoomLevels.contains(m_ZoomLevel))
		{
			Integer z = (Integer) setZoomLevels.toArray()[0];
			if (DebugOn)
				Log.d(TAG, "Changing zoom level to " + z);
			SetZoomLevel(z);
		}

		m_setZoomLevels = setZoomLevels;

		invalidate();
	}

	void SetZoomLevel(int zoom)
	{
		if (DebugOn)
			Log.d(TAG, "SetZoomLevel: " + zoom);
		if ((zoom >= m_MinZoom) && (zoom < m_MaxZoom))
		{
			if (DebugOn)
				Log.d(TAG, "Valid zoom level");
			m_ZoomLevel = zoom;
		}
		else if (DebugOn)
		{
			m_zoomAction = false;
			Log.d(TAG, "Invalid zoom level");
		}
	}

	List<Bitmap> GetImage(int x_index, int y_index)
	{
		List<Bitmap> lstImages = new ArrayList<Bitmap>();
		if (DebugOn)
			Log.d(TAG, "GetImage(" + x_index + "," + y_index + ")");

		Bitmap img = null;

		img = GetImage(x_index, y_index, m_ZoomLevel);

		if (img != null)
		{
			if (DebugOn)
				Log.d(TAG, "Valid single image");
			lstImages.add(img);
			return lstImages;
		}

		if (DebugOn)
			Log.d(TAG, "NULL Image");
		
		return lstImages;
	}

	Bitmap GetImage(int x_index, int y_index, int zoom)
	{
		Bitmap img = null;
		if (DebugOn)
			Log.d(TAG, "GetImage(x,y,z)");

		if (imageReader != null)
		{
			img = imageReader.GetImage(x_index, y_index, zoom);
		}

		return img;
	}

	private void DrawBitmap(Canvas canvas, Bitmap img, PointF corner)
	{
		if (img == null)
			return;
		canvas.drawBitmap(img, corner.x, corner.y, null);
		m_tilesAvailable = true;
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		m_tilesAvailable = false;
		super.onDraw(canvas);

		if (DebugOn)
			Log.d(TAG, "onDraw Starting");

		List<Bitmap> lstImages;
		
		Log.i(TAG, "");

		int tilesize = m_TileSize;

		if ((m_ZoomLevel < m_MinZoom) || (m_ZoomLevel > m_MaxZoom))
		{
			Log.w(TAG, "Invalid zoom level");
			return;
		}

		Tile t = Mercator.LatLonToOSMTile(m_PageCentre, m_ZoomLevel, m_TileSize);

		Rect winSize = new Rect();
		getDrawingRect(winSize); // Is this right?

		PointF centre = new PointF((float) (winSize.width()/2.0),
				(float) (winSize.height()/2.0));
		PointF centre_tile_corner = new PointF(
				centre.x - t.posX,
				centre.y - t.posY);
		PointF start_tile_corner = new PointF(centre_tile_corner.x,
				centre_tile_corner.y);
		int start_x_index = t.indexX;
		int start_y_index = t.indexY;

		if (DebugOn)
			Log.d(TAG, "Centre tile is " + t.indexX + "," + t.indexY);

		while (start_tile_corner.x > 0.0)
		{
			start_tile_corner.x -= tilesize;
			start_x_index--;
		}
		while (start_tile_corner.y > 0.0)
		{
			start_tile_corner.y -= tilesize;
			start_y_index--;
		}

		PointF tile_corner = new PointF(start_tile_corner.x,
				start_tile_corner.y);
		int x_index = start_x_index;
		int y_index = start_y_index;

		while (tile_corner.y < winSize.height())
		{
			while (tile_corner.x < winSize.width())
			{
				lstImages = GetImage(x_index, y_index);
				if (lstImages.size() == 1)
				{
					if (DebugOn)
						Log.d(TAG, "Got one image");
					DrawBitmap(canvas, lstImages.get(0), tile_corner);
				}
				else if (lstImages.size() == 4)
				{
					if (DebugOn)
						Log.d(TAG, "Got four images");
					PointF subtile_corner = new PointF(tile_corner.x, tile_corner.y);
					if (DebugOn)
						Log.d(TAG, "  - Index 0 at (" + subtile_corner.x + "," + subtile_corner.y + ")");
					DrawBitmap(canvas, lstImages.get(0), subtile_corner);
					subtile_corner.y = (tile_corner.y + (m_TileSize/2));
					if (DebugOn)
						Log.d(TAG, "  - Index 1 at (" + subtile_corner.x + "," + subtile_corner.y + ")");
					DrawBitmap(canvas, lstImages.get(1), subtile_corner);
					subtile_corner.x = (tile_corner.x + (m_TileSize/2));
					subtile_corner.y = (tile_corner.y);
					if (DebugOn)
						Log.d(TAG, "  - Index 2 at (" + subtile_corner.x + "," + subtile_corner.y + ")");
					DrawBitmap(canvas, lstImages.get(2), subtile_corner);
					subtile_corner.y = (tile_corner.y + (m_TileSize/2));
					if (DebugOn)
						Log.d(TAG, "  - Index 3 at (" + subtile_corner.x + "," + subtile_corner.y + ")");
					DrawBitmap(canvas, lstImages.get(3), subtile_corner);
				}
				else
				{
					if (DebugOn)
						Log.d(TAG, "Got something else (probably null): " + lstImages.size());
				}

				tile_corner.x += tilesize;
				x_index++;
			}
			tile_corner.y += tilesize;
			tile_corner.x = start_tile_corner.x;
			x_index = start_x_index;
			y_index++;
			if (!m_tilesAvailable && m_zoomAction) //skip past zoom levels with no charts
			{
				if (m_zoomOut)
				{
					m_ZoomLevel -= 1;
					SetZoomLevel(m_ZoomLevel);
					onDraw(canvas);
				}
				else
				{
					m_ZoomLevel += 1;
					SetZoomLevel(m_ZoomLevel);
					onDraw(canvas);
				}
			}
			else
				m_zoomAction = false;
		}

		// Now draw the user location
		if (userLocation == null)
			return;
		LatLon l = new LatLon();
		l.latitude = userLocation.getLatitude();
		l.longitude = userLocation.getLongitude();
		Metres m = Mercator.LatLonToMetres(l);
		Pixels loc_p = Mercator.MetresToPixels(m, m_ZoomLevel, m_TileSize);
		m.mx += userLocation.getAccuracy();
		Pixels acc_p = Mercator.MetresToPixels(m, m_ZoomLevel, m_TileSize);
		float accuracy_radius = (float) (acc_p.px - loc_p.px);
		PointF loc_pos = Mercator.PixelsToWindowPos(loc_p,
				m_PageCentre, winSize, m_ZoomLevel, m_TileSize);

		canvas.drawCircle(loc_pos.x, loc_pos.y, accuracy_radius, accuracyFillPaint);
		canvas.drawCircle(loc_pos.x, loc_pos.y, accuracy_radius, accuracyStrokePaint);

		canvas.drawCircle(loc_pos.x, loc_pos.y, LOCATION_OUTER_RADIUS, locationOuterPaint);
		canvas.drawCircle(loc_pos.x, loc_pos.y, LOCATION_MID_RADIUS, locationMidPaint);
		canvas.drawCircle(loc_pos.x, loc_pos.y, LOCATION_INNER_RADIUS, locationInnerPaint);

	}
}
