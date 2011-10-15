package mx.mariner;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
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
	private Context context;
	private static final String tag = "MXM";
	
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
			Log.d(tag, "Starting Row Inflation ...");
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.region_list_item, parent, false);
			Log.d(tag, "Successfully inflated row");
		}
		
		//get item
		Region region = getItem(position);
		
		//get reference to ImageView
		regionIcon = (ImageView) row.findViewById(R.id.regionicon);
		
		//get reference to TextView
		regionName = (TextView) row.findViewById(R.id.regionname);
		
		//get reference to TextView for description
		regionDesc = (TextView) row.findViewById(R.id.regiondesc);
		
		//set name & description
		regionName.setText(region.name);
		regionDesc.setText(region.description);
		
		//set icon
		
		regionIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), region.iconResourceId));
		
		return row;
	}

}
