package mx.mariner.tracks;

import java.util.ArrayList;

import org.osmdroid.contributor.util.RecordedGeoPoint;
import org.osmdroid.util.GeoPoint;

import android.location.Location;

/*
 * should we allow multiple tracks? yes... start with just one though
 * need minimum distance
 */

public class TrackRecorder {
    private final int trackPointDistance = 185; //place track points at this interval in meters 185m = .1nmi = 607ft
    private final ArrayList<GeoPoint> trackRecords = new ArrayList<GeoPoint>();

    public ArrayList<GeoPoint> getRecordedTrackGeoPoints() {
        return this.trackRecords;
    }

    public void add(final Location aLocation, final int aNumSatellites) {
        this.trackRecords
                .add(new RecordedGeoPoint((int) (aLocation.getLatitude() * 1E6), (int) (aLocation
                        .getLongitude() * 1E6), System.currentTimeMillis(), aNumSatellites));
    }

    public void add(final GeoPoint aGeoPoint) {
        if (trackRecords.isEmpty()) {
            trackRecords.add(aGeoPoint);
        } else {
            //TODO: make sure the distance between is greather thatn trackPointDistance
            trackRecords.get(trackRecords.size() -1 ); //last point in track
            this.trackRecords.add(aGeoPoint);
        }
        
    }
    
}

