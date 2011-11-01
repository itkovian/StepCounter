package be.ugent.csl.StepCounter;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.IBinder;

/*
 * @author Andy Georges
 * 
 */

public class AccellMeterService extends Service implements SensorEventListener  {
		
	@Override 
	public void onCreate() {
		super.onCreate();
	}
	
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	return START_STICKY;
    }
    
    
    @Override 
    public IBinder onBind(Intent intent) {
		return null;
    }
    
    
    @Override
    public void onDestroy() {	
    	InteractionModelSingleton.get().closeFile();    	
    	super.onDestroy();
    }
    
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
	}

}
