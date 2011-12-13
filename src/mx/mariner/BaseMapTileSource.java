package mx.mariner;

import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;

public class BaseMapTileSource extends BingMapTileSource {
    private int zoomMax = 17; //anything over causes scale to bounce and drawing on screen problems
    
    public BaseMapTileSource(String aLocale) {
        super(aLocale);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public int getMaximumZoomLevel() {
        return zoomMax;
    }
    
    public void setMaximumZoomLevel(int level) {
        zoomMax = level;
    }

}
