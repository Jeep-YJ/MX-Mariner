package mx.mariner;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RegionActivity extends ListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  
	  //String[] countries = getResources().getStringArray(R.array.regions_array);
	  //setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, countries));
	  this.setListAdapter(new RegionArrayAdapter(this, new RegionList().getList()));
	  
	  ListView listView = getListView();
	  listView.setTextFilterEnabled(true);

	  listView.setOnItemClickListener(new OnItemClickListener() {
		  public void onItemClick(AdapterView<?> parent, View view,
				  int position, long id) {
	      TextView vw = (TextView) view.findViewById(R.id.regionname);
		  String name = (String) vw.getText();
	      Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
	    }
	  });
	}
}