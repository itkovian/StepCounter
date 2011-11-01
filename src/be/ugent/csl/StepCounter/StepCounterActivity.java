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
	private Button closeButton;
	private Button logButton;
	private Button clearButton;
	
	/* checkboxes */
    private CheckBox logData;
    
	/* seekbar for the logging rate */
	private SeekBar rateMultiplier;
	
	/* text fields */
	private TextView sampleRateText;
	private TextView logLinesText;
	
	/* message input field */
	private EditText data;
	
	/* ================================================================== */
	/* Updater for the line count field showing how many lines 
	 * were found in the trace file.
	 */
	UpdateLogTask logTask;
	
	/* 
	 * Inner private class. You do not need to change anything here.
	 */
	private class UpdateLogTask extends AsyncTask<Void, Integer, Void> {
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
	 * called by the UpdateLogTask.onProgressUpdate().
	 */
	public void updateLogCount() {
		logLinesText.setText(Integer.toString(InteractionModelSingleton.get().logFileLines()));			
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
        /* buttons */
        /* the closing button */
        closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnTouchListener(new OnTouchListener() {
          public boolean onTouch(View v, MotionEvent e) {
            finish();
            return true;
          }
        });
       	/* The log button */
       	logButton = (Button) findViewById(R.id.addMessageButton);
       	logButton.setOnClickListener(new OnClickListener() {
       		@Override
       		public void onClick(View v) {
       			InteractionModelSingleton.get().logString(data.getText().toString());
       			updateLogCount();
       		}
       	});
       	/* The clear-the-trace-file button */
       	clearButton = (Button) findViewById(R.id.clear);
       	clearButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				InteractionModelSingleton.get().clearFile();
				updateLogCount();
			}
		});

        /* ============================================================== */
       	/* Checkboxes */
       	logData = (CheckBox) findViewById(R.id.logData);
       	logData.setChecked(InteractionModelSingleton.get().isLogging());
       	logData.setOnCheckedChangeListener(new OnCheckedChangeListener() {
       		@Override
       		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
       			InteractionModelSingleton.get().setLogging(isChecked);
       		}
       	});

        /* ============================================================== */
        /* Text Views */
        sampleRateText = (TextView) findViewById(R.id.sampleRateText);
        logLinesText = (TextView) findViewById(R.id.linesCount);
       	
        /* ============================================================== */
        /* Input field for the message */
        data = (EditText) findViewById(R.id.messageData);
        
        /* ============================================================== */
        /* seekbar */
        rateMultiplier = (SeekBar)this.findViewById(R.id.rateMultiplier);
        rateMultiplier.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				InteractionModelSingleton.get().setRate(progress);
				sampleRateText.setText(InteractionModelSingleton.get().rateAsString());
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub	
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub	
			}
        	
        	
        });
        rateMultiplier.setProgress(InteractionModelSingleton.get().getRate());

      	/* This starts the update thread to asynchronously update the
       	 * UI fields when changes occur. You need not touch this code. 
       	 */
       	logTask = new UpdateLogTask();
       	logTask.execute();
    }

	public void onDestroy() {
		logTask.cancel(true);
		InteractionModelSingleton.get().closeFile();    
    	super.onDestroy();
    }   
}
