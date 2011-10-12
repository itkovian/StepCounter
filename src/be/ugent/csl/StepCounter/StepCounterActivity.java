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
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/*
 * @author Andy Georges
 */
public class StepCounterActivity extends Activity {
    
	/* Various :-) */
	private final String TAG = "be.ugent.csl.StepCounter.StepCounterActivity";
	private final String accellLogFileName = "accelDataLog";
	
	/* UI items */
	private Button closeButton;
	private SeekBar rateMultiplier;
	private TextView sampleRateText;
	private Spinner filterSpinner;
	
	/* Service interaction */
	private boolean accellMeterServiceBound = false;
	private AccellMeterService accellMeterService;
	private ServiceConnection accellMeterServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			accellMeterService = ((AccellMeterService.LocalBinder)service).getService();
			Log.i(TAG, "Service connection established");
		}
		
		public void onServiceDisconnected(ComponentName className) {
			accellMeterService = null;			
			Log.i(TAG, "Service connection removed");
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
				String text = null;
				switch(progress) {
				case 0: text = "FASTEST"; break;
				case 1: text = "GAME"; break;
				case 2: text = "UI"; break;
				case 3: text = "NORMAL"; break;
				}
				sampleRateText.setText(text);
				// FIXME: there seems to be an error here when tilting the phone.
				accellMeterService.setAccuracy(progress);	
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
        
        
       	Intent i = new Intent(this, AccellMeterService.class);
       	i.putExtra("AccellLogFileName", accellLogFileName);
       	/* first we bind to the service to be able to talk to it through the local binding */
       	accellMeterServiceBound = bindService(i, accellMeterServiceConnection, BIND_AUTO_CREATE);
       	Log.i(TAG, "AccelMeterService bound");
       	
       	/* we also need to start the service explicitly, with the same intent to make sure
       	 * the service keeps running even when the activity loses focus. 
       	 */
       	startService(i);	  
       	Log.i(TAG, "AccellMeterService started");
        
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