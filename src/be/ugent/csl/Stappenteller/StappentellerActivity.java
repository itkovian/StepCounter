package be.ugent.csl.Stappenteller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class StappentellerActivity extends Activity {
    
	private final String accellLogFileName = "accelDataLog";
	
	private Button closeButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        this.closeButton = (Button)this.findViewById(R.id.close);
        this.closeButton.setOnTouchListener(new OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent e) {
            finish();
            return true;
          }
        });
        
       	Intent i = new Intent(this, AccellMeterService.class);
       	i.putExtra("AccellLogFileName", accellLogFileName);
       	startService(i);	   
        
    }
    
    public void onDestroy() {
    	
    	/* take down the service */
    	stopService(new Intent(this, AccellMeterService.class));
    	
    	
    	super.onDestroy();
    }
    
    
 
	    /*
	    	super.setText( "Current timestamp = " + event.timestamp + "\n"
	    			     + "Current accurecy = " + event.accuracy + "\n"
	    			     + "Actual sensor X-value([0]) = " + event.values[0] + "\n"
	    			     + "Actual sensor Y-value([1]) = " + event.values[1] + "\n"
	    			     + "Actual sensor Z-value([2]) = " + event.values[2] + "\n"
	    			     + "\n"
	    			     + "Acceleration X-value = " + linear_acceleration[0] + "\n"
	    			     + "Acceleration Y-value = " + linear_acceleration[1] + "\n"
	    			     + "Acceleration Z-value = " + linear_acceleration[2] + "\n"
	    			     );
	    	*/
        
    
}