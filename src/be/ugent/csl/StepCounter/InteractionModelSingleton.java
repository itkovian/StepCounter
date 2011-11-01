package be.ugent.csl.StepCounter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

	/* =================================================================
	 * Public and static fields.
	 */
	
	/* TAG serves as a tag in the log files so we can easily see who is 
	 * responsible for a given entry in the log files. 
	 */
	public final static String TAG = "be.ugent.csl.StepCounter.StepCounterActivity";
	
	/* Fixed name of the trace file */
	private final String accellLogFileName = "accelDataLog";

	/* Log sensor values to the file, field is only accessible through
	 * the appropriate getter and setter methods */
	private boolean logging = false;
	
	/* File access. We use a BufferedWriter to reduce the number of
	 * times we need to actually access the file for flushing the data. */
	private File accelFile = null;
	private BufferedWriter accellLog = null;
	private int logLines = -1; // Number of lines in the trace
	private boolean shouldAppend = true; // Always append to the current file, _except_ when we forcefully clear it!
	
	/* =================================================================
	 * This class acts as a singleton, this means that there can only be a single
	 * instance (i.e., object) of the class present inside a JVM execution. In other
	 * words, there is no public constructor available for users to call upon. One
	 * can simply ask the class for the instance that has been created at class 
	 * initialisation time, through the get method.
	 */
	private static InteractionModelSingleton instance = new InteractionModelSingleton();

	public static InteractionModelSingleton get() {
		return instance;
	}
	

	/* =================================================================
	 * Getter and setter for the logging field.
	 */
	public boolean isLogging() {
		return logging;
	}
	public void setLogging(boolean l) {
		logging = l;
		logString("Changed logging data to " + logging);
	}
	

	/* =================================================================
	 * File shizzle methods. 
	 */
	
	/* 
	 * Did we open the file?
	 */
	private boolean openedFile() {
		return accellLog != null;
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
			logLines = -1;
			return;
		}
		
		try {
			BufferedReader r = new BufferedReader(new FileReader(accelFile));
			logLines = 0;
			while (r.readLine() != null)
				logLines++;
			r.close();
		} catch (IOException e) {
			logLines = -1;
		}
	}
	public int logFileLines() {
		return logLines;
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
       		accelFile = new File(externalStorage, accellLogFileName);
       		if (shouldAppend)
       			countLines();
       		else
       			logLines = 0;
       		accellLog = new BufferedWriter(new FileWriter(accelFile, shouldAppend));
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
			logLines = -1;
			try {
				accellLog.flush();
				accellLog.close();
			}
			catch(IOException e) {
				Log.e(TAG, "IOException when flushing the writer", e);
			}
		}
		accellLog = null;
		accelFile = null;
	}

	
	/* ==============================================================
	 * Logging the measurements 
	 */
	
    // Log a message to the Log, and potentially to the log file if writing to it is enabled
    // If force is true, write anyway (for logging messages, mainly)
    private void log(long eventTimestamp, long timestamp, float[] rawValues, String message, boolean force) {
    	// TODO: StringBuilder?
    	String logString = eventTimestamp
    			        + ":" + timestamp
    					+ ":" + rawValues[0]
    					+ ":" + rawValues[1]
			    	    + ":" + rawValues[2]
			    	    + ":" + message
			    	    + "\n";
    	
    	if (!openFile())
    		return;
    	
    	if (logging || force) {
    		try {
    			accellLog.write(logString);
    			logLines++;
    		} catch (IOException e) {
    			Log.e(TAG, "Cannot write to the log for storing the sensor values", e);
    		}
    	}
    }

	public void log(long eventTimestamp, long timestamp, float[] rawValues) {
    	log(eventTimestamp, timestamp, rawValues, "", false);
    }
    
    public void logString(String string) {
    	log(0, Calendar.getInstance().getTimeInMillis(),
    			new float[]  { 0, 0, 0 },
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
    	if (accellMeterService != null) {
    		accellMeterService.setAccuracy(rate);
    	}
    }
    public int getRate() {
    	return rate;
    }

}
