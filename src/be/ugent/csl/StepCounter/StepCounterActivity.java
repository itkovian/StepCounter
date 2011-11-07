package be.ugent.csl.StepCounter;


import be.ugent.csl.StepCounter.R;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


/*
 * @author Andy Georges
 * @author Bart Coppens
 * 
 * This class implements the (only) activity of the application.
 * Here we define the actions that need to be taken when something
 * happens to each of the UI components and to update the various
 * UI fields when stuff happens.
 */
public class StepCounterActivity extends Activity {
	
	/* ======================================================
	/* UI items. You will need to attach these to the corresponding 
	 * item in the main.xml layout you defined. Note that it is
	 * not necessary for these variables to be declared here, they
	 * could also be declared e.g., in the onCreate() method, IF 
	 * they need not be accessed from elsewhere. However, for 
	 * clarity, we chose to declare them as object fields.
	 */
	
	/* buttons */
	private Button quitButton;
	private Button logButton;
	private Button clearButton;
	
	/* checkboxes */
    private CheckBox logDataCheckBox;
    
	/* seekbar for the logging rate */
	private SeekBar rateMultiplierBar;
	
	/* text fields */
	private TextView sampleRateText;
	private TextView traceLinesText;
	private TextView numberOfStepsText;
	
	/* message input field */
	private EditText messageEditText;
	
	/* ================================================================== */
	/* Updater for the line count field showing how many lines 
	 * were found in the trace file. This allows the UI to be updated 
	 * asynchronously, which improves responsiveness and is less 
	 * error-prone.
	 */
	UpdateTraceLineCountTask traceLineCountTask;
	
	/* 
	 * Inner private class. You do not need to change anything here.
	 */
	private class UpdateTraceLineCountTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			while (!isCancelled()) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					Log.e(InteractionModelSingleton.TAG, e.getMessage());
				}
				publishProgress(InteractionModelSingleton.get().logFileLines());
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			updateLogCount();
		}
	}
	

	/* Interaction with the UI. This function updates the value shown in the UI when
	 * called by the UpdateLogTask.onProgressUpdate(). We need to make sure we do not
	 * accidentally change the information displayed by this field if it does not 
	 * exist yet.
	 */
	public void updateLogCount() {
		if(traceLinesText != null) {
			traceLinesText.setText(Integer.toString(InteractionModelSingleton.get().logFileLines()));			
		}
	}
	
	
    /* ====================================================================
     * This function is called when the activity is created.
     * 
     * Here, you need to add code for several things.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* We indicate here that we will be using the layout defined in main.xml
         * which has been translated into R.java.
         */
        setContentView(R.layout.main);
        
        /* ============================================================== */
        /* Opgave 2: a. Geef deze widgets een gepaste waarde door te refereren
         *              naar de juiste ID's uit de layout.
         *           b. Zorg ervoor dat de juiste luisteraars gedefinieerd
         *              worden en dat je ook de passende stappen onderneemt
         *              opdat de UI consistent blijft en het resultaat van 
         *              actie weergegeven wordt. Je kunt hiervoor anonieme 
         *              objecten gebruiken waarvoor je de juiste methoden 
         *              implementeert.
         */
        
        /* buttons */
        /* the closing button */
        quitButton = null; // FIXME
        
       	/* The log button */
       	logButton = null; //FIXME
       	/* The clear-the-trace-file button */
       	clearButton = null; //FIXME

        /* ============================================================== */
       	/* Checkboxes */
       	logDataCheckBox = null; //FIXME

        /* ============================================================== */
        /* Text Views */
        sampleRateText = null; //FIXME
        traceLinesText = null; //FIXME
       	
        /* ============================================================== */
        /* Input field for the message */
        messageEditText = null; //FIXME
        
        /* ============================================================== */
        /* seekbar */
        rateMultiplierBar = null; //FIXME

      	/* This starts the update thread to asynchronously update the
       	 * UI fields when changes occur. You need not touch this code. 
       	 */
       	traceLineCountTask = new UpdateTraceLineCountTask();
       	traceLineCountTask.execute();
    }

	public void onDestroy() {
		traceLineCountTask.cancel(true);
		InteractionModelSingleton.get().closeFile();    
    	super.onDestroy();
    }   
}
