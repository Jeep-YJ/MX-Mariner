package mx.mariner;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RegionActivity extends ListActivity {
	private Context context;
	private SQLiteDatabase regiondb;
	private ProgressDialog progressDialog;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.context = this.getBaseContext();
        super.onCreate(savedInstanceState);
        initregiondb();
    	new RegionUpdateCheck(regiondb, context).execute();
    	progressDialog = new ProgressDialog(this);
    	this.setListAdapter(new RegionArrayAdapter(this, new RegionList(context, regiondb).getList()));
  	  
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final String name = (String) ((TextView) view.findViewById(R.id.regionname)).getText();
				TextView statusTv = (TextView) view.findViewById(R.id.regionstatus);
				String status = (String) statusTv.getText();
				
				if (status == "not installed") {
					progressDialog.setMessage("Downloading "+ name);
					progressDialog.setIndeterminate(false);
					progressDialog.setMax(100);
					progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					final RegionDownload regDl = new RegionDownload(context, regiondb, name, progressDialog);
					regDl.execute();
					progressDialog.setCancelable(true);
					progressDialog.setCanceledOnTouchOutside(false);
					progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							regDl.cancel(true);
							Toast.makeText(context, name+" download canceled!", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
    }
    
    private void initregiondb() {
      regiondb = (new RegionDbHelper(this)).getWritableDatabase();
    }
    
}