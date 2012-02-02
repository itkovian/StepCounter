package be.ugent.csl.StepCounter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

/*
 * This class is the central storage of all settings that the StepCounterActivity has to keep track of.
 * This means that the various Services and Activities can be killed and restarted, but this will always
 * keep track of what the user selected, and will keep the logging file open, etc.
 * 
 * XXX: Eventually this could/should become a real (non-static) MVC class with real listeners...
 *  
 * @author Bart Coppens
 * @author Andy Georges
 */
public class Util {

	/* =================================================================
	 * Public and static fields.
	 */
	
	/* TAG serves as a tag in the log files so we can easily see who is 
	 * responsible for a given entry in the log files. 
	 */
	public final static String TAG = "be.ugent.firw.irproject.MyStepCounterActivity";
	
	/* Fixed name of the trace file */
	private final String accellTraceFileName = "accelDataLog";

	/* Log sensor values to the file, field is only accessible through
	 * the appropriate getter and setter methods 
	 */
	private boolean tracing = false;
	
	/* File access. We use a BufferedWriter to reduce the number of
	 * times we need to actually access the file for flushing the data. 
	 */
	private File accelFile = null;
	private BufferedWriter accellTrace = null;
	private int traceLines = -1; // Number of lines in the trace
	private boolean shouldAppend = true; // Always append to the current file, _except_ when we forcefully clear it!
	
	/* =================================================================
	 * Step detectors.
	 * Each of these will be called from the trace() method to store and process
	 * the latest data item.
	 */
	private HashMap<String, StepDetection> detectors = new HashMap<String, StepDetection>();
	private StepDetection currentDetector = null;
	
	public StepDetection getCurrentStepDetector() {
		return currentDetector;
	}
	
	public void setStepDetector(String s) {
		StepDetection d = detectors.get(s); 
		if(d != null) {
			currentDetector = d;
		}
	}
	
	/* =================================================================
	 * This class acts as a singleton, this means that there can only be a single
	 * instance (i.e., object) of the class present inside a JVM execution. In other
	 * words, there is no public constructor available for users to call upon. One
	 * can simply ask the class for the instance that has been created at class 
	 * initialisation time, through the get method.
	 */
	private Util() {
		detectors.put("Simple threshold", new SimpleThresholdDetector());
	}
	private static Util instance = new Util();

	public static Util get() {
		return instance;
	}
	

	/* =================================================================
	 * Getter and setter for the logging field.
	 */
	public boolean isTracing() {
		return tracing;
	}
	public void setTracing(boolean l) {
		tracing = l;
		traceString("Changed tracing data to " + tracing);
	}
	

	/* =================================================================
	 * File shizzle methods. 
	 */
	
	/* 
	 * Did we open the file?
	 */
	private boolean openedFile() {
		return accellTrace != null;
	}
	
	/*
	 * Return the number of lines in the logfile.
	 * XXX: this should be synchronized if there is the possibility 
	 * that multiple users will access this. Since we only use it
	 * from our own Activity, and we have a singleton, this should 
	 * not be an issue. 
	 */
	private void countLines() {
		if (accelFile == null) {
			traceLines = -1;
			return;
		}
		
		try {
			BufferedReader r = new BufferedReader(new FileReader(accelFile));
			traceLines = 0;
			while (r.readLine() != null)
				traceLines++;
			r.close();
		} catch (IOException e) {
			traceLines = -1;
		}
	}
	public int logFileLines() {
		return traceLines;
	}
	
	/*
	 * Clear data from the trace file. 
	 */
	public void clearFile() {
		closeFile();
		shouldAppend = false;
		openFile();
	}
	
	/*
	 * Try to ensure that the file is opened, returns success. 
	 * XXX: Same argument as with countLines().
	 */
	private boolean openFile() {
		if (openedFile())
			return true;
       	File externalStorage = Environment.getExternalStorageDirectory();

       	try {
       		accelFile = new File(externalStorage, accellTraceFileName);
       		if (shouldAppend)
       			countLines();
       		else
       			traceLines = 0;
       		accellTrace = new BufferedWriter(new FileWriter(accelFile, shouldAppend));
       		shouldAppend = true;
       		return true;
       	}
       	catch(IOException e) {
       		// TOO BAD, service will not trace to a file.
       		Log.e(TAG, "IOException when opening a writer to " + accellTraceFileName, e);
       	}

       	return false;
	}

	public void closeFile() {
		if ( openedFile() ) {
			traceLines = -1;
			try {
				accellTrace.flush();
				accellTrace.close();
			}
			catch(IOException e) {
				Log.e(TAG, "IOException when flushing the writer", e);
			}
		}
		accellTrace = null;
		accelFile = null;
	}

	
	/* ==============================================================
	 * Logging the measurements 
	 */
	
    // Log a message to the Log, and potentially to the log file if writing to it is enabled
    // If force is true, write anyway (for logging messages, mainly)
    private void trace(long eventTimestamp, long timestamp, float[] rawValues, String message, boolean force) {
    	
    	/* Note that this is not the best way, since we now store 
    	 * the same data multiple times, but it makes understanding 
    	 * what happens a lot easier.
    	 */
    	for(StepDetection detector: detectors.values()) {
    		detector.addData(timestamp, rawValues[0], rawValues[1], rawValues[2]);
    	}
    	
    	// TODO: StringBuilder?
    	String traceString = eventTimestamp
    			           + ":" + timestamp
    					   + ":" + rawValues[0]
    					   + ":" + rawValues[1]
			    	       + ":" + rawValues[2]
			    	       + ":" + message
			    	       + "\n";
    	
    	if (!openFile())
    		return;
    	
    	if (tracing || force) {
    		try {
    			accellTrace.write(traceString);
    			traceLines++;
    		} catch (IOException e) {
    			Log.e(TAG, "Cannot write to the log for storing the sensor values", e);
    		}
    	}
    }

	public void trace(long eventTimestamp, long timestamp, float[] rawValues) {
    	trace(eventTimestamp, timestamp, rawValues, "", false);
    }
    
    public void traceString(String string) {
    	trace(0, 
    	    Calendar.getInstance().getTimeInMillis(),
    		new float[]  { 0, 0, 0 },
    		string, 
    		true);
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
    	int accuracy = 0;
    	switch(rate) {
    	case 0: accuracy = SensorManager.SENSOR_DELAY_FASTEST; break;
    	case 1: accuracy = SensorManager.SENSOR_DELAY_GAME; break;
    	case 2: accuracy = SensorManager.SENSOR_DELAY_NORMAL; break;
    	case 3: accuracy = SensorManager.SENSOR_DELAY_UI; break;
    	}
    	if(accellMeterService != null) {
    		accellMeterService.setAccuracy(accuracy);
    	}
    }
    public int getRate() {
    	return rate;
    }
    private AccellMeterService accellMeterService = null;
    public void setService(AccellMeterService s) {
    	accellMeterService = s;
    	accellMeterService.setAccuracy(rate);
    }
}
