// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

/*
 * This code is very heavily based on the Python code from 
 *
 * http://www.maptiler.org/google-maps-coordinates-tile-bounds-projection/
 *
 * Since this is largely a (different language) copy of that code, here is the
 * original copyright notice from that file:
 *
 * Copyright (c) 2008 Klokan Petr Pridal. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package aocpn.mapdroid;

import android.graphics.Rect;
import android.graphics.PointF;
import android.util.Log;

class Mercator
{
	private static final double m_OriginShift = 2 * Math.PI * 6378137 / 2.0;
	private static final boolean DebugOn = false;
	private static final String TAG = "Mercator";

    static LatLon MetresToLatLon(Metres metres)
	{
		if (DebugOn)
			Log.d(TAG, "Converting Metres (" + metres.mx + "," + metres.my + ") to LatLon");
		LatLon result = new LatLon();
		result.longitude = (metres.mx / m_OriginShift) * 180.0;
		result.latitude = (metres.my / m_OriginShift) * 180.0;
		result.latitude  = (180.0 / Math.PI) * (2.0 * Math.atan(Math.exp(result.latitude * Math.PI / 180.0)) - (Math.PI / 2.0));
		if (DebugOn)
			Log.d(TAG, "LatLon: (" + result.latitude + "," + result.longitude + ")");
		return result;
	}

	static Metres LatLonToMetres(LatLon latlon)
	{
		if (DebugOn)
			Log.d(TAG, "Converting LatLon (" + latlon.latitude + "," + latlon.longitude + ") to Metres");
		Metres result = new Metres();

		result.mx = (latlon.longitude * m_OriginShift) / 180.0;
		result.my = Math.log(Math.tan((90.0 + latlon.latitude) * Math.PI / 360.0)) / (Math.PI / 180.0);
		result.my *= m_OriginShift / 180.0;

		if (DebugOn)
			Log.d(TAG, "Metres: (" + result.mx + "," + result.my + ")");

		return result;
	}

	static double Resolution(int zoom, int tilesize)
	{
		double init_resolution = 2 * Math.PI * 6378137 / tilesize;
		return init_resolution / Math.pow(2.0, zoom);
	}

	static Pixels MetresToPixels(Metres metres, int zoom, int tilesize)
	{
		if (DebugOn)
			Log.d(TAG, "Converting Metres (" + metres.mx + "," + metres.my + ") to Pixels at Zoom Level " + zoom);
		Pixels result = new Pixels();

		double res = Resolution(zoom, tilesize);

		result.px = (metres.mx + m_OriginShift) / res;
		result.py = (metres.my + m_OriginShift) / res;

		if (DebugOn)
			Log.d(TAG, "Pixels: (" + result.px + "," + result.py + ")");

		return result;
	}

	static Metres PixelsToMetres(Pixels pixels, int zoom, int tilesize)
	{
		if (DebugOn)
			Log.d(TAG, "Converting Pixels (" + pixels.px + "," + pixels.py + ") to Metres at Zoom Level " + zoom);
		Metres result = new Metres();
		double res = Resolution(zoom, tilesize);

		result.mx = (pixels.px * res) - m_OriginShift;
		result.my = (pixels.py * res) - m_OriginShift;

		if (DebugOn)
			Log.d(TAG, "Metres: (" + result.mx + "," + result.my + ")");

		return result;
	}

	static Tile PixelsToTMSTile(Pixels pixels, int tilesize)
	{
		if (DebugOn)
			Log.d(TAG, "Converting Pixels (" + pixels.px + "," + pixels.py + ") to TMS Tile");
		Tile t = new Tile();

		double tmsX = pixels.px / (1.0*tilesize);
		double tmsY = pixels.py / (1.0*tilesize);

		t.indexX = (int) (Math.ceil(tmsX) - 1);
		t.indexY = (int) (Math.ceil(tmsY) - 1);

		double prX = 1.0 * t.indexX * tilesize;
		double prY = 1.0 * t.indexY * tilesize;

		t.posX = (int) Math.floor(pixels.px - prX);
		// Not sure about this:
		t.posY = (int) Math.floor(tilesize + prY - pixels.py);

		if (DebugOn)
			Log.d(TAG, "TMS Tile: (" + t.indexX + "," + t.indexY + ";" + t.posX + "," + t.posY + ")");

		return t;
	}

	static Tile TMSTileToOSMTile(Tile t, int zoom)
	{
		if (DebugOn)
			Log.d(TAG, "Converting TMS Tile (" + t.indexX + "," + t.indexY + ";" + t.posX + "," + t.posY + ") to OSM Tile");
		Tile result = new Tile();

		result.indexX = t.indexX;
		result.indexY = ((int) Math.pow(2,zoom)-1) - t.indexY;

		result.posX = t.posX;
		result.posY = t.posY;

		if (DebugOn)
			Log.d(TAG, "OSM Tile: (" + result.indexX + "," + result.indexY + ";" + result.posX + "," + result.posY + ")");

		return result;
	}

	static Tile PixelsToOSMTile(Pixels pixels, int zoom, int tilesize)
	{
		return TMSTileToOSMTile(PixelsToTMSTile(pixels, tilesize), zoom);
	}

	static Tile LatLonToOSMTile(LatLon latlon, int zoom, int tilesize)
	{
		return PixelsToOSMTile(LatLonToPixels(latlon, zoom, tilesize), zoom, tilesize);
	}

	static Pixels LatLonToPixels(LatLon latlon, int zoom, int tilesize)
	{
		return MetresToPixels(LatLonToMetres(latlon), zoom, tilesize);
	}

	static LatLon PixelsToLatLon(Pixels pixels, int zoom, int tilesize)
	{
		return MetresToLatLon(PixelsToMetres(pixels, zoom, tilesize));
	}

	static LatLon WindowPosToLatLon(PointF p, LatLon pageCentre, Rect winSize, int zoom, int tilesize)
	{
		return PixelsToLatLon(WindowPosToPixels(p, pageCentre, winSize, zoom, tilesize), zoom, tilesize);
	}

	static PointF LatLonToWindowPos(LatLon l, LatLon pageCentre, Rect winSize, int zoom, int tilesize)
	{
		return PixelsToWindowPos(LatLonToPixels(l, zoom, tilesize), pageCentre, winSize, zoom, tilesize);
	}

	static Pixels WindowPosToPixels(PointF p, LatLon pageCentre, Rect winSize, int zoom, int tilesize)
	{
		if (DebugOn)
			Log.d(TAG, "Converting Window position (" + p.x + "," + p.y + ") to pixels");
		PointF centre = new PointF((float) (winSize.width() / 2.0), (float) (winSize.height() / 2.0));

		Pixels pixels = LatLonToPixels(pageCentre, zoom, tilesize);

		// Convert to point to the top left corner of the window
		pixels.px -= centre.x;
		pixels.py -= centre.y;
		pixels.px += p.x;
		pixels.py += p.y;

		if (DebugOn)
			Log.d(TAG, "Pixels: (" + pixels.px + "," + pixels.py + ")");

		return pixels;
	}

	static PointF PixelsToWindowPos(Pixels p, LatLon pageCentre, Rect winSize, int zoom, int tilesize)
	{
		if (DebugOn)
			Log.d(TAG, "Converting Pixels (" + p.px + "," + p.py + ") to window position");
		Pixels p_centre = LatLonToPixels(pageCentre, zoom, tilesize);
		PointF centre = new PointF((float) (winSize.width() / 2.0), (float) (winSize.height() / 2.0));

		Pixels p_offset = new Pixels();
		p_offset.px = p_centre.px - p.px;
		p_offset.py = p_centre.py - p.py;

		PointF pt = new PointF((float) (centre.x - p_offset.px), (float) (centre.y + p_offset.py));

		if (DebugOn)
			Log.d(TAG, "Window position: (" + pt.x + "," + pt.y + ")");

		return pt;
	}
}
