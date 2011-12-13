package mx.mariner;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {
    
    private float min, max;
 
    public InputFilterMinMax(float minimum, float maximum) {
        min = minimum;
        max = maximum;
    }
 
    public InputFilterMinMax(String minimum, String maximum) {
        min = Float.parseFloat(minimum);
        max = Float.parseFloat(maximum);
    }
    
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String input = dest.toString() + source.toString();
        //Log.i("MXM", input);
        try {
            float inputf = Float.parseFloat(input);
            if (isInRange(min, max, inputf))
                return null;
        } catch (NumberFormatException nfe) { }     
        return "";
    }
 
    private boolean isInRange(float minimum, float maximum, float input) {
        if (input>=minimum && input<=maximum) {
            return true;
        }
        return false;
        //return maximum > minimum ? input >= minimum && input <= maximum : input >= maximum && input <= minimum;
    }

}

