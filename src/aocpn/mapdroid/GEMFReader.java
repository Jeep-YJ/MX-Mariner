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
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

class GEMFRange
{
	int zoom;
	int xmin;
	int xmax;
	int ymin;
	int ymax;
	int source_index;
	long offset;
};


class GEMFReader implements ReaderInterface
{
	private static final String TAG = "GEMF";
	private String loadedGEMFFile = new String();
	private RandomAccessFile gemf_file = null;
	private List<GEMFRange> lstRangeData = new ArrayList<GEMFRange>();
	private List<Long> lstFileSizes = new ArrayList<Long>();
	private List<String> lstFileNames = new ArrayList<String>();
	private static final boolean DebugOn = false;
	private HashMap<Integer,String> hshSources =
		new HashMap<Integer,String>();
	private HashMap<Integer,Boolean> hshSourceStates =
		new HashMap<Integer,Boolean>();

	public void Initialise(String location)
	{
		if ( ! ReadGEMFHeader(location))
		{
			if (DebugOn)
			{
				Log.d(TAG, "Could not read GEMF Header");
				Close();
				return;
			}
		}
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

	public boolean IsOpen()
	{
		if (gemf_file == null)
			return false;
		return true;
	}

	public void Close()
	{
		try
		{
			if (gemf_file != null)
			{
				gemf_file.close();
				gemf_file = null;
			}
		}
		catch (java.io.IOException e)
		{
		}
	}

	public Set<Integer> GetZoomLevels()
	{
		Set<Integer> setZoomLevels = new TreeSet<Integer>();

		for (GEMFRange rs: lstRangeData)
		{
			setZoomLevels.add(rs.zoom);
		}

		return setZoomLevels;
	}

	private boolean ReadGEMFHeader(String location)
	{
		if ( ! location.endsWith(".gemf"))
		{
			return false;
		}
		else if (loadedGEMFFile != location)
		{
			// Load the GEMF Header
			loadedGEMFFile = "";
			lstRangeData.clear();
			hshSources.clear();
			hshSourceStates.clear();
			lstFileSizes.clear();
			lstFileNames.clear();

			Close();

			try
			{
				gemf_file = new RandomAccessFile(location, "r");

				if (DebugOn)
					Log.d(TAG, "Retrieving file sizes");
				File fInfo = new File(location);
				int index = 1;
				while (fInfo.exists())
				{
					lstFileSizes.add(fInfo.length());
					lstFileNames.add(fInfo.getAbsolutePath());
					String nextFile = location + "-" + Integer.toString(index);
					if (DebugOn)
						Log.d(TAG, "Checking " + nextFile);
					fInfo = new File(nextFile);
					index += 1;
				}


				if (DebugOn)
					Log.d(TAG, "Reading GEMF version");
				int gemf_version = gemf_file.readInt();
				if (gemf_version != 4)
				{
					Log.w(TAG, "Wrong GEMF Version:" + gemf_version);
					gemf_file.close();
					return false;
				}

				if (DebugOn)
					Log.d(TAG, "Reading tile size");
				int tilesize = gemf_file.readInt();
				if (tilesize != 256)
				{
					Log.w(TAG, "Wrong tile size " + tilesize);
					gemf_file.close();
					return false;
				}

				if (DebugOn)
					Log.d(TAG, "Reading source list");
				int source_count = gemf_file.readInt();
				for (int i=0;i<source_count;i++)
				{
					if (DebugOn)
						Log.d(TAG, "Reading source " + i);
					int source_index = gemf_file.readInt();
					int source_name_length = gemf_file.readInt();
					byte[] name_data = new byte[source_name_length];
					gemf_file.read(name_data, 0, source_name_length);
					String name = new String(name_data);
					hshSources.put(new Integer(source_index), name);
					hshSourceStates.put(new Integer(source_index), new Boolean(true));
				}

				if (DebugOn)
					Log.d(TAG, "Reading number of ranges");
				int num_ranges = gemf_file.readInt();
				if (DebugOn)
					Log.d(TAG, "Number of ranges:" + num_ranges);

				for (int i=0;i<num_ranges;i++)
				{
					if (DebugOn)
						Log.d(TAG, "Reading range " + i);
					GEMFRange rs = new GEMFRange();
					rs.zoom = gemf_file.readInt();
					rs.xmin = gemf_file.readInt();
					rs.xmax = gemf_file.readInt();
					rs.ymin = gemf_file.readInt();
					rs.ymax = gemf_file.readInt();
					rs.source_index = gemf_file.readInt();
					rs.offset = gemf_file.readLong();
					lstRangeData.add(rs);
				}

				loadedGEMFFile = location;
				Log.i(TAG, "Loaded GEMF Header/Data file successfully");
				return true;
			}
			catch (java.io.FileNotFoundException e)
			{
				Log.w(TAG, "FileNotFound reading GEMF");
				return false;
			}
			catch (java.io.IOException e)
			{
				Log.w(TAG, "IOException reading GEMF");
				return false;
			}
		}
		else
		{
			return true;
		}
	}

	public Bitmap GetImage(int x_index, int y_index, int zoom)
	{
		GEMFRange this_range = new GEMFRange();
		boolean bFoundRange = false;
		final int U32SIZE = 4;
		final int U64SIZE = 8;

		if ( ! IsOpen())
			return null;

		for (GEMFRange rs: lstRangeData)
		{
			if ((zoom == rs.zoom)
					&& (x_index >= rs.xmin)
					&& (x_index <= rs.xmax)
					&& (y_index >= rs.ymin)
					&& (y_index <= rs.ymax)
					&& (hshSourceStates.get(new Integer(rs.source_index))))
			{
				if (DebugOn)
					Log.d(TAG, "(" + x_index + "," + y_index + ") in range "
							+ "((" + rs.xmin + ".." + rs.xmax + "),("
							+ rs.ymin + ".." + rs.ymax + "))");
				this_range = rs;
				bFoundRange = true;
				break;
			}
			else if (DebugOn)
			{
				Log.d(TAG, "(" + x_index + "," + y_index + ") not in range "
						+ "((" + rs.xmin + ".." + rs.xmax + "),("
						+ rs.ymin + ".." + rs.ymax + "))");
			}
		}

		if ( ! bFoundRange)
		{
			if (DebugOn)
				Log.d(TAG, "Couldn't find relevant range");
			return null;
		}

		if (gemf_file == null)
		{
			if (DebugOn)
				Log.d(TAG, "Header/data file not open\n");
			return null;
		}

		int num_y = this_range.ymax + 1 - this_range.ymin;
		x_index = x_index - this_range.xmin;
		y_index = y_index - this_range.ymin;
		long offset = (x_index * num_y) + y_index;
		offset *= (U32SIZE + U64SIZE);
		offset += this_range.offset;


		byte[] data;
		long data_file_offset;
		int data_file_length;
		try
		{
			gemf_file.seek(offset);

			data_file_offset = gemf_file.readLong();
			data_file_length = gemf_file.readInt();

			RandomAccessFile pDataFile = gemf_file;

			int index = 0;
			if (data_file_offset > lstFileSizes.get(0))
			{
				int filelist_count = lstFileSizes.size();
				while ((index < (filelist_count - 1))
						&& (data_file_offset > lstFileSizes.get(index)))
				{
					data_file_offset -= lstFileSizes.get(index);
					index += 1;
				}
				pDataFile = new RandomAccessFile(lstFileNames.get(index), "r");
			}

			if (DebugOn)
				Log.d(TAG, "Reading file " + lstFileNames.get(index) +  " from offset " + data_file_offset + " with length + " + data_file_length);

			data = new byte[data_file_length];
			pDataFile.seek(data_file_offset);
			pDataFile.read(data, 0, data_file_length);

		}
		catch (java.io.IOException e)
		{
			return null;
		}

		return BitmapFactory.decodeByteArray(data, 0, data_file_length);
	}

}
