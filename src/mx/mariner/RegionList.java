package mx.mariner;

import java.util.ArrayList;
import java.util.List;

public class RegionList {
	
	private final List<Region> list;
	private final int count = 9;
	
	public RegionList() {
		list = new ArrayList<Region>();
		list.add( new Region("NOAA_BSB_REGION_01", 
				"US East, New Jersey to Maine", R.drawable.region01) );
		
		list.add( new Region("NOAA_BSB_REGION_05", 
				"US East, N. Carolina to Delaware", R.drawable.region05) );
		
		list.add( new Region("NOAA_BSB_REGION_07", 
				"US East, Florida to S. Carolina", R.drawable.region07) );
		
		list.add( new Region("NOAA_BSB_REGION_08", 
				"US South East, New Mexico to Florida pan handle", R.drawable.region08) );
		
		list.add( new Region("NOAA_BSB_REGION_09", 
				"US North East, Great Lakes, Minnesota to New York", R.drawable.region09) );
		
		list.add( new Region("NOAA_BSB_REGION_11", 
				"US West, Arizona to California", R.drawable.region11) );
		
		list.add( new Region("NOAA_BSB_REGION_13", 
				"US West, Oregon to Washington", R.drawable.region13) );
		
		list.add( new Region("NOAA_BSB_REGION_14", 
				"US West, Pacific Ocean, Hawaii", R.drawable.region14) );
		
		list.add( new Region("NOAA_BSB_REGION_17", 
				"US West, Pacific Ocean, Alaska", R.drawable.region17) );
	}
	
	public List<Region> getList() {
		return list;
	}
	
	public int getCount() {
		return count;
	}
	
}
