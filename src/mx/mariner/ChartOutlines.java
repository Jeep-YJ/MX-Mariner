package mx.mariner;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;


public class ChartOutlines {
	
	//====================
	// Fields
	//====================
	
	private ArrayList<Object> outlinePathOverlays;
	
	//====================
    // Constructors
	//====================
	
	public ChartOutlines() {
		outlinePathOverlays = new ArrayList<Object>();
	}
	
	//====================
    // Methods
	//====================
	
	public void addPathOverlay(int color, ResourceProxy mResourceProxy, String coordinates) {
		PathOverlay path = new PathOverlay(color, mResourceProxy);
		
		String[] coord = coordinates.split(":");
		for (int i=0; i<coord.length; i++) {
			String lat = coord[i].split(",")[0];
			String lon = coord[i].split(",")[1];
			path.addPoint(new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon)));
		}
		
		outlinePathOverlays.add(path);
	}
	
	public ArrayList<Object> getPaths() {
		return outlinePathOverlays;
	}
	
	public void clearPaths() {
		outlinePathOverlays.clear();
	}

}
