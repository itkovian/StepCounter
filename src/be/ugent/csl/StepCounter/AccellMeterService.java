package be.ugent.csl.StepCounter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/*
 * @author Andy Georges
 * 
 */

public class AccellMeterService extends Service implements SensorEventListener  {
	
	/* Communication with the using Activity */
    private LocalBinder accellBinder = new LocalBinder();
    	
    /* Various */
	private final String TAG = "be.ugent.csl.StepCounter.AccellMeterService";
	
	/* Sensor shizzle */
	private SensorManager mSensorManager;
	private Sensor mAccellSensor;
	
	/* Sensor data structures */
	private double [] gravity = new double[3]; 
	
	/* Global fugly shizzle */
	private boolean started = false;
	private boolean registered = false;
	
	/* Step detection */
	private StepDetection detector = null;

	
	@Override 
	public void onCreate() {
	
		super.onCreate();
		
		/* get the sensor manager over here */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccellSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       	
		Log.i(TAG, "AccellMeterService started");
	}
	
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    	synchronized(this) {
    		if(started) {
    			return START_STICKY;
    		}       	

    		registered = mSensorManager.registerListener(this, mAccellSensor, SensorManager.SENSOR_DELAY_FASTEST);
        	started = true;
    	}
    	return START_STICKY;
    }
    
    
    @Override 
    public IBinder onBind(Intent intent) {
		return accellBinder;
    }
    
    public class LocalBinder extends Binder {
    	AccellMeterService getService() {
    		return AccellMeterService.this;
    	}
    }
    
    @Override
    public void onDestroy() {
    	mSensorManager.unregisterListener(this, mAccellSensor);
    	Toast.makeText(this, "AccellMeterService destroyed", Toast.LENGTH_LONG).show();
    	
    	InteractionModelSingleton.get().closeFile();
    	
    	super.onDestroy();

    	Log.i(TAG, "AccellMeterService destroyed");
    }
    
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	// TODO Auto-generated method stub
    	//Log.i(TAG, "The accuracy for " + sensor + "was set to " + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {
    	if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
    		return;
    	}

    	// the sensor has three values, acceleration in X, Y, Z
    	// directions. 

    	// high-pass filter (canceling the baseline gravity value)
    	final double alpha = 0.8;

      // g_n = a g_{n-1} + (1-a) x_n
    	gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
    	gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
    	gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

    	double [] linear_acceleration = new double[3];
      // la_n = x_n - g_n = x_n - a g_{n-1} + (a - 1) x_n = a ( x_n - g_{n-1})
    	linear_acceleration[0] = event.values[0] - gravity[0];
    	linear_acceleration[1] = event.values[1] - gravity[1];
    	linear_acceleration[2] = event.values[2] - gravity[2];

    	//if(detector != null) {
    	//	detector.addData(event.timestamp, linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);
    	//}

    	InteractionModelSingleton.get().log(
    			event.timestamp,
    			Calendar.getInstance().getTimeInMillis(), // TODO event.timestamp
    			event.values, linear_acceleration
    	);
	}

    
    public float getResolution() {
    	return mAccellSensor.getResolution();
    }
    
    public void setAccuracy(int accuracy) {
    	/* unregister first, otherwise the listener keeps receiving data at the highest set rate */
    	synchronized(this) {
    		if(registered) {
    			mSensorManager.unregisterListener(this);
    			registered = false;
    		}
    	   	if(mSensorManager.registerListener(this, mAccellSensor, accuracy)) {
    	   		Log.i(TAG, "Sensor accuracy changed to " + accuracy);
    	   		registered = true;
    	   	}
    	}
    }
    	 
    public void setFilter(StepDetection filter) {
    	detector = filter;
    }

}
