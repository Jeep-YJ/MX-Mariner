// Copyright (C) 2011 by Will Kamp <manimaul!gmail.com>
// Distributed under the terms of the Simplified BSD Licence.
// See license.txt for details
// Code heavily based on osmdroid http://code.google.com/p/osmdroid/ and https://sites.google.com/site/abudden/android-map-store

package aocpn.mapdroid;

import android.graphics.Bitmap;
import java.util.Set;
import java.util.HashMap;

interface ReaderInterface
{
	void Initialise(String location);
	void Close();
	boolean IsOpen();
	Bitmap GetImage(int x_index, int y_index, int zoom);
	Set<Integer> GetZoomLevels();
	public HashMap<Integer,String> GetSourceList();
	public HashMap<Integer,Boolean> GetSourceStates();
	public void SelectSource(int source);
	public void AcceptAnySource();
	public void EnableSource(int source);
	public void DisableSource(int source);

}
