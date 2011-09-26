package be.ugent.csl.Stappenteller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
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
       	
       	boolean mExternalStorageAvailable = false;
       	boolean mExternalStorageWriteable = false;
       	String state = Environment.getExternalStorageState();

       	if (Environment.MEDIA_MOUNTED.equals(state)) {
       	    // We can read and write the media
       	    mExternalStorageAvailable = mExternalStorageWriteable = true;
       	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
       	    // We can only read the media
       	    mExternalStorageAvailable = true;
       	    mExternalStorageWriteable = false;
       	} else {
       	    // Something else is wrong. It may be one of many other states, but all we need
       	    //  to know is we can neither read nor write
       	    mExternalStorageAvailable = mExternalStorageWriteable = false;
       	}
       	
       	externalStorage = Environment.getExternalStorageDirectory();
       	File t = new File(externalStorage,"acceldata");
       	try {
       		acceleroCache = new BufferedWriter(new FileWriter(t));
       	}
       	catch(IOException e) {
       		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("IOException Occured");
            dialog.setMessage( "mExternalStorageAvailable = " + mExternalStorageAvailable + "\n"
            		         + "mExternalStorageWriteable = " + mExternalStorageWriteable + "\n"
            		         + e.getMessage());
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
    		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("IOException Occured");
            dialog.setMessage(e.getMessage());
            dialog.setNeutralButton("Cool", null);
            dialog.create().show();    	
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
				    	    	   + "\n");
			} catch (IOException e) {
				// TODO
			}
	    	
	
	    }
		
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        
    }
}