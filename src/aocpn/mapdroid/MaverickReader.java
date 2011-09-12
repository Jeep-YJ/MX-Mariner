// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package aocpn.mapdroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;

class MaverickReader implements ReaderInterface
{
	private static final String TAG = "MAV";
	private static final boolean DebugOn = false;
	private boolean m_bIsOpen = false;
	private File mapFile;
	private HashMap<Integer,String> hshSources =
		new HashMap<Integer,String>();
	private HashMap<Integer,Boolean> hshSourceStates =
		new HashMap<Integer,Boolean>();

	public void Initialise(String location)
	{
		mapFile = new File(location);

		if ( ! mapFile.exists())
		{
			Log.d(TAG, "Map directory does not exist: " + location);
			return;
		}
		else if ( ! mapFile.isDirectory())
		{
			Log.d(TAG, "Map location not a directory: " + location);
			return;
		}

		File[] lstLayers = mapFile.listFiles();
		int index = 0;
		for (File layer: lstLayers)
		{
			if (layer.isDirectory())
			{
				hshSources.put(new Integer(index),
						layer.getName());
				hshSourceStates.put(new Integer(index),
						new Boolean(true));
				index++;
			}
		}

		hshSources.clear();
		hshSourceStates.clear();

		m_bIsOpen = true;

	}

	public boolean IsOpen()
	{
		return m_bIsOpen;
	}

	public void Close()
	{
		m_bIsOpen = false;
	}

	public HashMap<Integer,String> GetSourceList()
	{
		return hshSources;
	}

	public HashMap<Integer,Boolean> GetSourceStates()
	{
		return hshSourceStates;
	}

	public void SelectSource(int source)
	{
		if (hshSourceStates.containsKey(new Integer(source)))
		{
			for (int this_source: hshSourceStates.keySet())
			{
				if (this_source == source)
				{
					hshSourceStates.put(new Integer(this_source),
							new Boolean(true));
				}
				else
				{
					hshSourceStates.put(new Integer(this_source),
							new Boolean(false));
				}

			}
		}
	}

	public void EnableSource(int source)
	{
		if (hshSourceStates.containsKey(new Integer(source)))
		{
			hshSourceStates.put(new Integer(source),
					new Boolean(true));
		}
	}

	public void DisableSource(int source)
	{
		if (hshSourceStates.containsKey(new Integer(source)))
		{
			hshSourceStates.put(new Integer(source),
					new Boolean(false));
		}
	}

	public void AcceptAnySource()
	{
		for (int this_source: hshSourceStates.keySet())
		{
			EnableSource(this_source);
		}
	}


	public Set<Integer> GetZoomLevels()
	{
		Set<Integer> setZoomLevels = new TreeSet<Integer>();

		File[] lstLayers = mapFile.listFiles();

		for (File layer : lstLayers)
		{
			if (DebugOn)
				Log.d(TAG, "Parsing layer " + layer.getName());
			File[] lstEntries = layer.listFiles();
			for (File entry : lstEntries)
			{
				if (DebugOn)
					Log.d(TAG, "Parsing entry " + entry.getName());
				try
				{
					int i = Integer.parseInt(entry.getName());
					setZoomLevels.add(i);
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		return setZoomLevels;
	}

	public Bitmap GetImage(int x_index, int y_index, int zoom)
	{
		String[] lstFileTypes = {"png", "jpg",
			".png.tile", ".jpg.tile"};
		Bitmap img = null;
		if (DebugOn)
			Log.d(TAG, "GetImageFromMaverickDir");

		for (int source: hshSources.keySet())
		{
			File layer = new File(hshSources.get(source));

			if (DebugOn)
				Log.d(TAG, "Layer: " + layer.getName());
			for (String filetype: lstFileTypes)
			{
				if (DebugOn)
					Log.d(TAG, "Filetype: " + filetype);
				File image_file = new File(layer,
						String.format("%d/%d/%d.%s.tile",
							zoom,
							x_index,
							y_index,
							filetype));
				if (DebugOn)
					Log.d(TAG, "File name: " + image_file.getAbsolutePath());
				if (image_file.exists())
				{
						if (DebugOn)
							Log.d(TAG, "File exists");
				}
				else if (DebugOn)
					Log.d(TAG, "File does not exist");
				img = BitmapFactory.decodeFile(image_file.getAbsolutePath());

				if ( img != null)
				{
					if (DebugOn)
						Log.d(TAG, "Valid image");
					break;
				}
			}

			if ( img != null)
			{
				break;
			}
		}

		if (DebugOn)
			Log.d(TAG, "Done GetImageFromMaverickDir");

		return img;

	}
}
