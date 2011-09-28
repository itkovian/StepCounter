package be.ugent.csl.StepCounter;

import be.ugent.csl.StepCounter.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class StepCounterActivity extends Activity {
    
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
        
    
}