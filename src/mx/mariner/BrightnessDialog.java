package mx.mariner;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class BrightnessDialog extends Dialog {
    private SeekBar.OnSeekBarChangeListener onSeekListen;
    private TextView brightLevelTxt;
    private SeekBar seekBar;
    private float brightness;
    private float defValue;
    private SharedPreferences prefs;
    private String prefKey;

    public BrightnessDialog(Context context, Float defValue, final int factor) {
        super(context);
        this.setContentView(R.layout.brightnessdialog);
        prefs = PreferenceManager.getDefaultSharedPreferences(context); 
        this.defValue = defValue;
        brightLevelTxt = (TextView) findViewById(R.id.brightLevel);
        brightLevelTxt.setText(String.valueOf(brightness*100));
        seekBar = (SeekBar) findViewById(R.id.brightSeekBar);
        seekBar.setMax(5*factor);
        
        onSeekListen = new SeekBar.OnSeekBarChangeListener() {
            
            public void onStopTrackingTouch(SeekBar seekBar) {              
            }
            
            public void onStartTrackingTouch(SeekBar seekBar) {             
            }
            
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float level = (float)progress/100;
                //change the brightness text value
                if (progress >= (1.0*factor)) {
                    brightLevelTxt.setText(String.valueOf(progress));
                    ChangeBrightness(level);
                }
            }
        };
        seekBar.setOnSeekBarChangeListener(onSeekListen);
        Button saveButton = (Button) findViewById(R.id.buttonok);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Save();
            }
        });
        Button cancelButton = (Button) findViewById(R.id.buttoncancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Cancel();
            }
        });
    }

    public void ChangeBrightness(float level) {
        //change this activity screen brightness 0.0 to 1.0
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = level;
        getWindow().setAttributes(layoutParams);
        brightness = level;        
    }
    
    public void Save() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(prefKey, brightness);
        editor.commit();
        this.dismiss();
    }
    
    public void Cancel() {
        this.dismiss();
    }
    
    public void show(String key) {
        prefKey = key;
        ChangeBrightness( prefs.getFloat(prefKey, defValue) );
        int progress = (int) (brightness*100);
        seekBar.setProgress(progress);
        brightLevelTxt.setText(String.valueOf( progress ) );
        super.show();
    }

}
