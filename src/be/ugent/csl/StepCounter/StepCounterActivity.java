package be.ugent.csl.StepCounter;

import be.ugent.csl.StepCounter.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/*
 * @author Andy Georges
 */
public class StepCounterActivity extends Activity {
	/* UI items */
	private Button closeButton;
	private Button logButton;
	private SeekBar rateMultiplier;
	private TextView sampleRateText;
	private Spinner filterSpinner;
	
	/* Service interaction */
	private boolean accellMeterServiceBound = false;

	private ServiceConnection accellMeterServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			InteractionModelSingleton.get().setService(
					((AccellMeterService.LocalBinder)service).getService());
			Log.i(InteractionModelSingleton.TAG, "Service connection established");
		}
		
		public void onServiceDisconnected(ComponentName className) {
			InteractionModelSingleton.get().setService(null);			
			Log.i(InteractionModelSingleton.TAG, "Service connection removed");
		}
		
	};
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
        
        /* ============================================================== */
        /* Text Views */
        sampleRateText = (TextView) findViewById(R.id.sampleRateText);
        
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
        
        /* ============================================================== */
        /* Drop down menu */
        /*
        filterSpinner = (Spinner) findViewById(R.id.filterList);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this, R.array.filter_array, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener () {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				String value = parent.getItemAtPosition(pos).toString();
				StepDetection detector = null;
				Log.i(TAG, "Filter spinner had this selected: " + parent.getItemAtPosition(pos).toString());
				if(!value.equals("No detection")) {
					try {
						detector = (StepDetection) Class.forName("be.ugent.csl.StepCounter."+value).newInstance();
					} catch (IllegalAccessException e) {
						Log.e(TAG, "Not allowed to for filter selection " + value, e);
					} catch (InstantiationException e) {
						Log.e(TAG, "Cannot instantiate for filter selection " + value, e);
					} catch (ClassNotFoundException e) {
						Log.e(TAG, "Cannot find class for filter selection " + value, e);
					}
				}
				accellMeterService.setFilter(detector);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub	
			}
        	
        });
        */
        
        
       	Intent i = new Intent(this, AccellMeterService.class);
       	/* first we bind to the service to be able to talk to it through the local binding */
       	accellMeterServiceBound = bindService(i, accellMeterServiceConnection, BIND_AUTO_CREATE);
       	Log.i(InteractionModelSingleton.TAG, "AccelMeterService bound");
       	
       	/* we also need to start the service explicitly, with the same intent to make sure
       	 * the service keeps running even when the activity loses focus. 
       	 */
       	startService(i);	  
       	Log.i(InteractionModelSingleton.TAG, "AccellMeterService started");
        
       	/* The log button */
       	logButton = (Button) findViewById(R.id.addMessageButton);
       	
       	final EditText data = (EditText) findViewById(R.id.messageData);

       	logButton.setOnClickListener(new OnClickListener() {
       		@Override
       		public void onClick(View v) {
       			InteractionModelSingleton.get().logString(data.getText().toString());
       		}
       	});

       	
       	CheckBox logData = (CheckBox) findViewById(R.id.logData);
       	logData.setChecked(InteractionModelSingleton.get().isLogging());
       	logData.setOnCheckedChangeListener(new OnCheckedChangeListener() {
       		@Override
       		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
       			InteractionModelSingleton.get().setLogging(isChecked);
       		}
       	});

    }
    
    public void onDestroy() {
    	/* take down the service */
    	if(accellMeterServiceBound) {
    		unbindService(accellMeterServiceConnection);
    	}
    	stopService(new Intent(this, AccellMeterService.class));
    	super.onDestroy();
    }
        
    
}