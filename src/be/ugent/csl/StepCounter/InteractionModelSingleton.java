package be.ugent.csl.StepCounter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.os.Environment;
import android.util.Log;

/*
 * This class is the central storage of all settings that the StepCounterActivity has to keep track of.
 * This means that the various Services and Activities can be killed and restarted, but this will always
 * keep track of what the user selected, and will keep the logging file open, etc.
 * 
 *  Eventually this could/should become a real (non-static) MVC class with real listeners...
 *  
 * @author Bart Coppens
 */
public class InteractionModelSingleton {
	/* Various :-) */
	public final static String TAG = "be.ugent.csl.StepCounter.StepCounterActivity";
	private final String accellLogFileName = "accelDataLog";

	private static InteractionModelSingleton instance = new InteractionModelSingleton();

	public static InteractionModelSingleton get() {
		return instance;
	}
	
	/* Log sensor values to the file */
	private boolean logging = false;

	public boolean isLogging() {
		return logging;
	}
	public void setLogging(boolean l) {
		logging = l;
		logString("Changed logging data to " + logging);
	}
	
	private BufferedWriter accellLog = null;
	// We can close the file while the activity is active, and reopen it afterwards to _append_ instead of overwrite
	private boolean shouldAppend = false;

	private boolean openedFile() {
		return accellLog != null;
	}
	
	// Try to ensure that the file is opened, returns success
	private boolean openFile() {
		if (openedFile())
			return true;
       	File externalStorage = Environment.getExternalStorageDirectory();
       	
       	try {
       		accellLog = new BufferedWriter(new FileWriter(new File(externalStorage, accellLogFileName), shouldAppend));
       		shouldAppend = true;
       		return true;
       	}
       	catch(IOException e) {
       		// TOO BAD, service will not log to a file.
       		Log.e(TAG, "IOException when opening a writer to " + accellLogFileName, e);
       	}
       	return false;
	}

	public void closeFile() {
		if ( openedFile() ) {
			try {
				accellLog.flush();
				accellLog.close();
			}
			catch(IOException e) {
				Log.e(TAG, "IOException when flushing the writer", e);
			}
		}
		accellLog = null;
	}

	/* Logging the measurements */
	
    // Log a message to the Log, and potentially to the log file if writing to it is enabled
    // If force is true, write anyway (for logging messages, mainly)
    private void log(long timestamp, float[] rawValues, double[] linear, String message, boolean force) {
    	// TODO: StringBuilder?
    	String logString = timestamp
    					+ ":" + rawValues[0]
    					+ ":" + rawValues[1]
			    	    + ":" + rawValues[2]
			    	    + ":" + linear[0]
			    	    + ":" + linear[1]
			    	    + ":" + linear[2]
			    	    + ":" + message
			    	    + "\n";

    	//Log.i(TAG, "SENSORVALUES: " + logString);
    	
    	if (!openFile())
    		return;
    	
    	if (logging || force) {
    		try {
    			accellLog.write(logString);
    		} catch (IOException e) {
    			Log.e(TAG, "Cannot write to the log for storing the sensor values", e);
    		}
    	}
    }

	public void log(long timestamp, float[] rawValues, double[] linear) {
    	log(timestamp, rawValues, linear, "", false);
    }
    
    public void logString(String string) {
    	log(Calendar.getInstance().getTimeInMillis(),
    			new float[]  { 0, 0, 0 },
    			new double[] { 0, 0, 0 },
    			string, true);
    }

    
    /* Sample rate */
    private int rate = 0;
    private String rateToString(int rate) {
    	switch(rate) {
    		case 0: return "FASTEST";
			case 1: return "GAME";
			case 2: return "UI";
			case 3: return "NORMAL";
		}
    	return "UNKNOWN!";
    }
    public String rateAsString() {
    	return rateToString(rate);
    }
    public void setRate(int rate) {
    	this.rate = rate;
    }
    
    private AccellMeterService accellMeterService = null;
    public void setService(AccellMeterService s) {
    	accellMeterService = s;
    	accellMeterService.setAccuracy(rate);
    }
}
