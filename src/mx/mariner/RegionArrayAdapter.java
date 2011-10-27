package mx.mariner;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RegionArrayAdapter extends ArrayAdapter<Region> {
	private List<Region> regions = new ArrayList<Region>();
	private ImageView regionIcon;
	private TextView regionName;
	private TextView regionDesc;
	private TextView regionSize;
	private TextView regionStat;
	private Context context;
	//private static final String tag = "MXM";
	
	public RegionArrayAdapter(Context context, List<Region> objects) {
		super(context, R.layout.region_list_item, objects);
		this.context = context;
		this.regions = objects;
	}
	
	public int getCount() {
		return this.regions.size();
	}
	
	public Region getItem(int index) {
		return this.regions.get(index);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row==null) {
			//row inflation
			//Log.d(tag, "Starting Row Inflation ...");
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.region_list_item, parent, false);
			//Log.d(tag, "Successfully inflated row");
		}
		
		//get item
		Region region = getItem(position);
		
		//get reference to views
		regionIcon = (ImageView) row.findViewById(R.id.regionicon);
		regionName = (TextView) row.findViewById(R.id.regionname);
		regionDesc = (TextView) row.findViewById(R.id.regiondesc);
		regionSize = (TextView) row.findViewById(R.id.regionsize);
		regionStat = (TextView) row.findViewById(R.id.regionstatus);
		
		//set icon and text values
		int iconResourceId = context.getResources().getIdentifier(region.icon, "drawable", context.getPackageName());
		regionIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), iconResourceId));
		regionName.setText(region.name);
		regionDesc.setText(region.desc);
		String megabytes = String.valueOf(region.bytes/1048576)+"MB";
		regionSize.setText(megabytes);
		switch ( region.status.hashCode() ) {
			case 29046650: //"installed".hashCode();
			{
				regionStat.setTextColor(Color.GREEN);
				break;
			}
			case -723658131: //"not installed".hashCode();
			{
				regionStat.setTextColor(Color.RED);
				break;
			}
			case -1709555662: //"update available".hashCode();
			{
				regionStat.setTextColor(Color.YELLOW);
				break;
			}
		}
			
		regionStat.setText(region.status);
		
		return row;
	}

}
