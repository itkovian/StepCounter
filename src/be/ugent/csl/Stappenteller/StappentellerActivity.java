package be.ugent.csl.Stappenteller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

public class StappentellerActivity extends Activity {
    
	private SensorManager mSensorManager;
	private PView mPView;
	private File externalStorage;
	private BufferedWriter acceleroCache;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
       	mPView = new PView(this);
       	externalStorage = Environment.getExternalStorageDirectory();
       	try {
       		acceleroCache = new BufferedWriter(new FileWriter(externalStorage + File.pathSeparator + "stappentellertestdata"));
       	}
       	catch(IOException e) {
       		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("MyException Occured");
            dialog.setMessage(e.getMessage());
            dialog.setNeutralButton("Cool", null);
            dialog.create().show();    		
       	}
        	   
        setContentView(mPView);
    }
    
    public void onDestroy() {
    	
    	// flush the buffered file and close it
    	try {
			acceleroCache.flush();
			acceleroCache.close();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    
    class PView extends TextView implements SensorEventListener {
    
    	private final Sensor mAccellSensor;
    	private double [] gravity = new double [3];
    
	    public PView(Context context) {
	    	super(context);
	    	
	    	mAccellSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    	mSensorManager.registerListener(this, mAccellSensor, SensorManager.SENSOR_DELAY_NORMAL);
	    	
		}

		public void onSensorChanged(SensorEvent event) {
	    	if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
	    		return;
	    	}
	    	
	    	// the sensor has three values, acceleration in X, Y, Z
	    	// directions. 
	    	
	    	// high-pass filter (canceling the baseline gravity value)
	    	final double alpha = 0.8;
	    	
	    	gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
	        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
	        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

	        double [] linear_acceleration = new double[3];
	        linear_acceleration[0] = event.values[0] - gravity[0];
	        linear_acceleration[1] = event.values[1] - gravity[1];
	        linear_acceleration[2] = event.values[2] - gravity[2];
	    	
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
	    	
	    	try {
				acceleroCache.write( event.timestamp 
						           + ":" + event.values[0]
				    			   + ":" + event.values[1]
				    	    	   + ":" + event.values[2]
				    	           + ":" + linear_acceleration[0]
				    	           + ":" + linear_acceleration[0]
				    	           + ":" + linear_acceleration[0]
				    	    	   );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	
	    }
		
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        
    }
}