package be.ugent.csl.Stappenteller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;



public class AccellMeterService extends Service implements SensorEventListener  {
	
	private final String TAG = "be.ugent.csl.Stappenteller.AccellMeterService";
	private SensorManager mSensorManager;
	private Sensor mAccellSensor;
	
	private BufferedWriter accellLog = null;
	private boolean started = false;
	
	private double [] gravity = new double[3]; 
	
	@Override 
	public void onCreate() {
	
		super.onCreate();
		
		/* get the sensor manager over here */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccellSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       	
		Toast.makeText(this, "AccellMeterService created", Toast.LENGTH_LONG).show();
		Log.i(TAG, "AccellMeterService started");
	}
	
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
    	
    	synchronized(this) {
    		if(started) {
    			return START_STICKY;
    		}
    	}
    	
       	File externalStorage = Environment.getExternalStorageDirectory();
       	String accellLogFileName = intent.getStringExtra("AccellLogFileName");
       	
       	Toast.makeText(this, "External storage at " + externalStorage, Toast.LENGTH_LONG).show();
       	
       	try {
       		accellLog = new BufferedWriter(new FileWriter(new File(externalStorage, accellLogFileName)));
       	}
       	catch(IOException e) {
       		// TOO BAD, service will not log to a file.
       		Toast.makeText(this, "IOException caught when opening a writer to " + accellLogFileName, Toast.LENGTH_LONG).show();
       		Log.e(TAG, "IOException when opening a writer to " + accellLogFileName, e);
       	}
       	
       	boolean registered = mSensorManager.registerListener(this, mAccellSensor, SensorManager.SENSOR_DELAY_NORMAL);
    	
    	Toast.makeText(this, "AccellMeterService started: " + registered, Toast.LENGTH_LONG).show();
    	synchronized(this) {
    		started = true;
    	}
    	return START_STICKY;
    
    }
    
    @Override 
    public IBinder onBind(Intent intent) {
		return null;
    }
    
    @Override
    public void onDestroy() {
    	mSensorManager.unregisterListener(this, mAccellSensor);
    	Toast.makeText(this, "AccellMeterService destroyed", Toast.LENGTH_LONG).show();
    	
    	try {
    		accellLog.flush();
    		accellLog.close();
    	}
    	catch(IOException e) {
    		Log.e(TAG, "IOException when flushing the writer", e);
    	}
    	super.onDestroy();

    	Log.i(TAG, "AccellMeterService destroyed");

    }
    
    
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	// TODO Auto-generated method stub

    }

    @Override
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

    	Log.i(TAG, "SENSORVALUES: " + event.timestamp 
					       + ":" + event.values[0]
			    		   + ":" + event.values[1]
			    	       + ":" + event.values[2]
			    	       + ":" + linear_acceleration[0]
			    	       + ":" + linear_acceleration[1]
			    	       + ":" + linear_acceleration[2]);
    	try {
    		accellLog.write( event.timestamp 
					       + ":" + event.values[0]
			    		   + ":" + event.values[1]
			    	       + ":" + event.values[2]
			    	       + ":" + linear_acceleration[0]
			    	       + ":" + linear_acceleration[1]
			    	       + ":" + linear_acceleration[2]
			    	       + "\n");
		} catch (IOException e) {
			//Toast.makeText(this, "IOEXception caught when writing to " + accellLog, Toast.LENGTH_LONG).show();
		}
			
	}
    	     
}