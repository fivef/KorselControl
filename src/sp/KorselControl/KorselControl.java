
package sp.KorselControl;

import java.text.DecimalFormat;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tokaracamara.android.verticalslidevar.VerticalProgressBar;
import com.tokaracamara.android.verticalslidevar.VerticalProgressBarTopDown;

/**
 * This is the program's main activity.
 * 
 * It controls the UI elements, registers the event listeners and manages application life-cycle
 */
public class KorselControl extends Activity {

	/**
	 * Tag for Logging
	 */
    public static final String TAG = "KorselControl";
    
    /**
     * Determines if debugging is enabled
     */
    public static final boolean D = true;
    
    /**
     * Intent request code for Device Discovery
     */
    private static final int REQUEST_CONNECT_DEVICE = 1;
    
    /**
     * Intent request code for enabling Bluetooth
     */
    private static final int REQUEST_ENABLE_BT = 3;

    
    /**
     * Default maximum pitch value (forward, backward tilt)
     */
    private static final int defaultmaxPitch = 6;
    
    /**
     * Default maximum roll value (left, right tilt
     */
    private static final int defaultmaxRoll = 6;
    
    /**
     * Default threshold value (used to decrease death spot)
     */
    private static final int defaultThreshold = 125;
    
    
    /**
     * State of the automatic mode
     */
    private boolean automaticState = false;
    
    /**
     * SensorManager needed for accelerometer initialization
     */
    private SensorManager mSensorManager;
    
    /**
     * Accelerometer Listener
     */
    private AccelerometerListener acc;
    
    /**
     * Class used for communication with korsel
     */
    private KorselBluetoothSerial btKorsel;
    
    //main views
    private ImageView startButtonView;
    private ImageView connectButtonView;
    private ImageView settingsButtonView;
    private ImageView autoButtonView;
    private ImageView auxButtonView;
    private ImageView debugButtonView;

    //debug views
    private TextView xValueTextView;
    private TextView yValueTextView;
    private TextView zValueTextView;
    private TextView rollTextView;
    private TextView pitchTextView;
    private TextView sensorTextView;
    
    /**
     * Settings Dialog
     */
    private Dialog mySettingsDialog;
    
    /**
     * Debug Dialog
     */
    private Dialog myDebugDialog;
    
    /**
     * Listener for trim button
     */
    TouchListener myTouchListener;
    
    /**
     * Listener for all other Buttons
     */
    ClickListener myClickListener;
    
    /**
     * Acceleration vector
     */
    Vector3d accVec;
      
   
    /**
     * Rotation Matrix used to rotate accelerometer frame
     */
    Matrix3d rotationMatrix;
    
    /**
     * Is the accelerometer active?
     * Is set true when start button is pressed
     * is set false if start button is released
     */
    boolean accActive;
    
    /**
     * Threshold (used to reduce death point caused by motors for pitch)
     */
    int threshold;
    
    /**
     * Maximum roll (used for steering sensitivity)
     */
    int maxRoll; 
    
    /**
     * Maximum pitch (used for steering sensitivity)
     */
    int maxPitch;
    
    /**
     * Variable for DecimalFormat definition
     */
    private DecimalFormat format;
   
/**
 * AccelerometerListener
 * 
 * Handles accelerometer events
 */
    private class AccelerometerListener implements SensorEventListener
    {

        public AccelerometerListener(Context context) {

        }
        
        public void onSensorChanged(SensorEvent event) {

        	accVec = new Vector3d(event.values[0],event.values[1],event.values[2]);

        	xValueTextView.setText("X: " + format.format(accVec.x)); 
        	yValueTextView.setText("Y: " + format.format(accVec.y)); 
        	zValueTextView.setText("Z: " + format.format(accVec.z)); 

		    if(accActive){
		    
		    	rotationMatrix.transform(accVec);
		    	
		    	//double totAcc = Math.sqrt((accVecA.x*accVecA.x)+(accVecA.y*accVecA.y)+(accVecA.z*accVecA.z));
		    	 
		         
		        //Rotation left or right = Roll (negative = left)
		        double roll = accVec.y;
		         
		        //Rotation forward or backward = Pitch(negative = backward)
		        double pitch = -accVec.x;

		        //normalize Roll
		        double normalizedRoll = 0;
	     
		        if(Math.abs(roll) < maxRoll){
		        	normalizedRoll = roll / maxRoll;
		        }else{
		        	if(roll > 0){
		        		normalizedRoll = 1;
		        	}else{
		        		normalizedRoll = -1;
		        	}
		        }
		        
		        //normalize Pitch
		        double normalizedPitch = 0;
			     
		        if(Math.abs(pitch) < maxPitch){
		        	normalizedPitch = pitch / maxPitch;
		        }else{
		        	if(pitch > 0){
		        		normalizedPitch = 1;
		        	}else{
		        		normalizedPitch = -1;
		        	}
		        }

		    	xValueTextView.setText("X: " + format.format(accVec.x)); 
	        	yValueTextView.setText("Y: " + format.format(accVec.y)); 
	        	zValueTextView.setText("Z: " + format.format(accVec.z)); 

	        	int motorLspeed = 0;
	        	int motorRspeed = 0;

	        	//forward
		        if(normalizedPitch >=0){
		        	//right
		        	if(normalizedRoll >= 0){
			        	motorLspeed = (int) (normalizedPitch * threshold);
			        	motorRspeed = (int) ((normalizedPitch - normalizedRoll) * threshold);
		        	}
		        	
		        	//left
		        	if(normalizedRoll < 0){
		        		motorLspeed = (int) ((normalizedPitch + normalizedRoll) * threshold);
			        	motorRspeed = (int) (normalizedPitch * threshold);
			        	
		        	}
		        	
		        	motorLspeed = motorLspeed + (255 - threshold);
			        motorRspeed = motorRspeed + (255 - threshold);
		        }
		        
		    	//backward
		        if(normalizedPitch < 0){
		        	//right
		        	if(normalizedRoll >= 0){
			        	motorLspeed = (int) (normalizedPitch * threshold);
			        	motorRspeed = (int) ((normalizedPitch + normalizedRoll) * threshold);
		        	}
		        	
		        	//left
		        	if(normalizedRoll < 0){
		        		motorLspeed = (int) ((normalizedPitch - normalizedRoll) * threshold);
			        	motorRspeed = (int) (normalizedPitch * threshold);
			        	
		        	}

		        	 motorLspeed = motorLspeed - (255 - threshold);
				     motorRspeed = motorRspeed - (255 - threshold);
		        }

	        	rollTextView.setText("L: " + motorLspeed); 
			    pitchTextView.setText("R: " + motorRspeed);
	        	
	        	updatedDebug(motorLspeed, motorRspeed);
	        	
	        	//TODO check in btKorsel if connected to prevent freeze/error
	        	
	        	if(btKorsel.isConnected()){
	        		
	        		btKorsel.setLeftMotorSpeed(motorLspeed);
	        		btKorsel.setRightMotorSpeed(motorRspeed);
	        	}else{
	        		
	        		connectButtonView.setImageResource(R.drawable.buttons_01);
	        	}
        	}
		
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
    
    /**
     * ClickListener
     * 
     * Handles events of standard buttons
     */
    private class ClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {

			//settings OK Button
			if(v.getId() == R.id.settingsOK){

					closeSettingsDialog();
			}
			
			//settings default Button
			if(v.getId() == R.id.settingsDefault){

					setDefaultSettings();
			}
		} 	
    }

    /**
     * TouchListener
     * 
     * Handles events of all image buttons
     */
    private class TouchListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub

			
			
			//Trim Button
			if(v.getId() == R.id.trimButton){
		
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					
					startButtonView.setImageResource(R.drawable.button_pressed);
					

					Vector3d normalAcceleration = new Vector3d(0,0,10);
        			
        			
        			double pitchAngle = accVec.angle(normalAcceleration);
        			
        			if(accVec.x > 0){
        				pitchAngle = pitchAngle - pitchAngle * 2;
        			}

        			rotationMatrix = new Matrix3d();
        			
        			rotationMatrix.setIdentity();
        			
        			rotationMatrix.rotY(pitchAngle);
					
					accActive = true;
					
					return true;
				}
				
				if(event.getAction() == MotionEvent.ACTION_UP){
					
					startButtonView.setImageResource(R.drawable.button);
					
					rotationMatrix.setIdentity();
					
					if(btKorsel.isConnected()){
		        		btKorsel.setLeftMotorSpeed(0);
		        		btKorsel.setRightMotorSpeed(0);
		        	}
					
					accActive = false;
					
					return true;
				}

			}
			
			//Connect Button
            if(v.getId() == R.id.connectButton){
                    
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                            
                		connectButtonView.setImageResource(R.drawable.buttons_pressed_01);
                        return true;
                    }
                    
                    if(event.getAction() == MotionEvent.ACTION_UP){
                            
                		connectButtonView.setImageResource(R.drawable.buttons_01);
                		
                		/**TODO start blinking to indicate connection
                		AnimationRoutine animationRoutine = new AnimationRoutine(connectButtonView);
                	       
                		Timer t = new Timer( false );
                		t.schedule( animationRoutine, 1000 );
                		*/
                	

                        btDiscovery();
                        
                        return true;
                    }

            }
            
            //Settings Button
            if(v.getId() == R.id.settingsButton){
                    
                    
                    
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                    	
                    	settingsButtonView.setImageResource(R.drawable.buttons_pressed_04);
                            
                    	return true;
                    }
                    
                    if(event.getAction() == MotionEvent.ACTION_UP){
                            
                    	settingsButtonView.setImageResource(R.drawable.buttons_activated_04);
                    	
                        showSettingsDialog();
                        
                        return true;
                    }

            }
            
            //Auto button
            if(v.getId() == R.id.autoButton){
                    
                    
                    
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                    	
                    	autoButtonView.setImageResource(R.drawable.buttons_pressed_02);
                            
                    	return true;
                    }
                    
                    if(event.getAction() == MotionEvent.ACTION_UP){
                            
                    	
                    	
                    	if(btKorsel.isConnected()){
    		        		if(automaticState == false){
    		        			
    		        			btKorsel.sendEnableAutoCommand();
    		        			autoButtonView.setImageResource(R.drawable.buttons_activated_02);
    		        			automaticState = true;
    		        			
    		        		}else{
    		        			
    		        			btKorsel.sendDisableAutoCommand();
    		        			autoButtonView.setImageResource(R.drawable.buttons_02);		        			
    		        			automaticState = false;
    		        			
    		        		}

    		        	}else{
    		            	Toast.makeText(KorselControl.this, "Not connected", Toast.LENGTH_LONG).show();
    		        	}
                        return true;
                    }
            }
            
            //aux button
            if(v.getId() == R.id.auxButton){
                    
            	
                    
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                    	
                    	auxButtonView.setImageResource(R.drawable.buttons_pressed_03);
                            
                    	return true;
                    }
                    
                    if(event.getAction() == MotionEvent.ACTION_UP){
                            
                    	//auxButtonView.setImageResource(R.drawable.buttons_activated_03);
                    	
                    	if(btKorsel.isConnected()){
    		        		btKorsel.sendAuxCommand();
    		        	}else{
    		            	Toast.makeText(KorselControl.this, "Not connected", Toast.LENGTH_LONG).show();
    		        	}
                        
                        return true;
                    }

            }

			//debug Button
			if(v.getId() == R.id.debugButton){

				   if(event.getAction() == MotionEvent.ACTION_DOWN){
                       
					   debugButtonView.setImageResource(R.drawable.buttons_pressed_05);
                       return true;
				   }
                 
				   if(event.getAction() == MotionEvent.ACTION_UP){

						if(myDebugDialog.isShowing()){
							
							debugButtonView.setImageResource(R.drawable.buttons_05);
							myDebugDialog.dismiss();
							
						}else{
							
							debugButtonView.setImageResource(R.drawable.buttons_activated_05);
							myDebugDialog.show();
						}
                       
                       return true;
               }
			}

			return false;
		}  	
    }


    /**
     * On Activity Create
     * 
     * Setup UI
     * Check if BT is enabled
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        createDebugDialog();
        
        //Decimal format setup
        
        format = new DecimalFormat("###.###");
        
        //init UI and Touch Listener
        	xValueTextView = (TextView) myDebugDialog.findViewById(R.id.xValue);
	        yValueTextView = (TextView) myDebugDialog.findViewById(R.id.yValue);
	        zValueTextView = (TextView) myDebugDialog.findViewById(R.id.zValue);
	        rollTextView = (TextView) myDebugDialog.findViewById(R.id.roll);
	        pitchTextView = (TextView) myDebugDialog.findViewById(R.id.pitch);
	        sensorTextView = (TextView) myDebugDialog.findViewById(R.id.sensor);

	        myTouchListener = new TouchListener();
	        myClickListener = new ClickListener();
	        
	        startButtonView = (ImageView) findViewById(R.id.trimButton);
	        startButtonView.setImageResource(R.drawable.button);  
	        startButtonView.setOnTouchListener(myTouchListener);

	        connectButtonView = (ImageView) findViewById(R.id.connectButton);
	        connectButtonView.setImageResource(R.drawable.buttons_01);  
	        connectButtonView.setOnTouchListener(myTouchListener);
	        
	        settingsButtonView = (ImageView) findViewById(R.id.settingsButton);
	        settingsButtonView.setImageResource(R.drawable.buttons_04);  
	        settingsButtonView.setOnTouchListener(myTouchListener);
	        
	        autoButtonView = (ImageView) findViewById(R.id.autoButton);
	        autoButtonView.setImageResource(R.drawable.buttons_02);  
	        autoButtonView.setOnTouchListener(myTouchListener);
	        
	        auxButtonView = (ImageView) findViewById(R.id.auxButton);
	        auxButtonView.setImageResource(R.drawable.buttons_03);  
	        auxButtonView.setOnTouchListener(myTouchListener);
	        
	        debugButtonView = (ImageView) findViewById(R.id.debugButton);
	        debugButtonView.setImageResource(R.drawable.buttons_05);  
	        debugButtonView.setOnTouchListener(myTouchListener);


        
	    //init BT
	        // Get local Bluetooth adapter
	        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	        // If the adapter is null, then Bluetooth is not supported
	        if (mBluetoothAdapter == null) {
	            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	            finish();
	            return;
	        }
	        
	        // If BT is not on, request that it be enabled.
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        // start discovery
	        }
   
    }

    /**
     * On Start create instance of KorselBluetoothSerial 
     * Create shared Preferences Object
     * Connect to last known BT device
     * Load settings from shared preferences
     * Initialize accelerometer
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart(){
    	super.onStart();
    	
    	//Instantiate Korsel bluetooth class
        btKorsel = new KorselBluetoothSerial(this);
                 
        //Load MAC from settings
        
        SharedPreferences settings = getPreferences(android.content.Context.MODE_PRIVATE);
          
        String btMAC = settings.getString("MAC", "");
        
       // Toast.makeText(this, R.string.bt_reconnect, Toast.LENGTH_LONG).show();
        
        //If MAC is available in settings, connect to it
        if(btMAC != ""){
        	btConnect(btMAC);
        }
        
        //Load pitch roll and threshold from settings (if not available use default values)
        maxPitch = Integer.parseInt(settings.getString("MAXPITCH",String.valueOf(defaultmaxPitch)));
        maxRoll = Integer.parseInt(settings.getString("MAXROLL",String.valueOf(defaultmaxRoll)));    
        threshold = Integer.parseInt(settings.getString("THRESHOLD",String.valueOf(defaultThreshold)));
        
        initAccelerometer();
    	
    }
    
    /**
     * On Activity Stop unregister accelerometer listener and disconnect Korsel.
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(acc);
        btKorsel.disconnect();
        connectButtonView.setImageResource(R.drawable.buttons_01);
        super.onStop();
      
    }
    
    /**
     * Initializes accelerometer
     */
	private void initAccelerometer(){
    	
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
    	
        acc = new AccelerometerListener(this);
        
        mSensorManager.registerListener(acc,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }
    
	/**
	 * start DeviceListActivity to search for BT devices
	 */
    private void btDiscovery(){
    	
    	//Toast.makeText(this, "BT Discovery startet", Toast.LENGTH_LONG).show();
    	
    	 Intent serverIntent = null;
        
         // Launch the DeviceListActivity to see devices and do scan
         serverIntent = new Intent(this, DeviceListActivity.class);
         startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
           
    }
    
    /**
     * Connects to the BT device with the given address and saves the mac for future use
     * 
     * @param address BT MAC Address
     */
    private void btConnect(String address){
    	
    	Toast.makeText(this, R.string.bt_connecting, Toast.LENGTH_LONG).show();
    	
    	 new ConnectBT().execute(address);
    	 
    	//save MAC for next connection
 		SharedPreferences settings = getPreferences(android.content.Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("MAC", address);
 		editor.commit();
    	

    }
    
    /**
     * Creates and shows the Settings Dialog
     */
    private void showSettingsDialog(){
    	

    	mySettingsDialog = new Dialog(this, R.style.TransparentTest);
		mySettingsDialog.setContentView(R.layout.settings_dialog);
	
		Button okButton = (Button) mySettingsDialog.findViewById(R.id.settingsOK);
		okButton.setOnClickListener(myClickListener);
    	
	 	Button defaultButton = (Button) mySettingsDialog.findViewById(R.id.settingsDefault);
    	defaultButton.setOnClickListener(myClickListener);
    	
    	updateSettings();
 	
   		mySettingsDialog.show();
    }
    
    /**
     * Closes the Settings Dialog, applies settings and saves settings for future use
     */
    private void closeSettingsDialog(){
    	
    	SeekBar thresholdBar = (SeekBar) mySettingsDialog.findViewById(R.id.threshold_seekbar);
    		threshold = 255 - thresholdBar.getProgress(); //flip direction
    	
    	SeekBar sensitivityPitchBar = (SeekBar) mySettingsDialog.findViewById(R.id.sensitivity_pitch_seekbar);
    		maxPitch = 10 - sensitivityPitchBar.getProgress(); //flip direction
		
		SeekBar sensitivityRollBar = (SeekBar) mySettingsDialog.findViewById(R.id.sensitivity_roll_seekbar);
			maxRoll = 10 - sensitivityRollBar.getProgress(); //flip direction
			
			
		//save values
 		SharedPreferences settings = getPreferences(android.content.Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("MAXPITCH", String.valueOf(maxPitch));
 		editor.putString("MAXROLL", String.valueOf(maxRoll));
 		editor.putString("THRESHOLD", String.valueOf(threshold));
 		editor.commit();
       
 		//Set Settings Button Image to deactivated
 		settingsButtonView.setImageResource(R.drawable.buttons_04);
 		
		mySettingsDialog.dismiss();
    }
   
   /**
    * Sets Settings to default
    */
   private void setDefaultSettings(){
    	
    	maxRoll = defaultmaxRoll;
    	maxPitch = defaultmaxPitch;
    	threshold = defaultThreshold;
    	
    	updateSettings();

	
    }
   

   /**
    * Updates the Settings Dialog
    */
   private void updateSettings(){
	   
		SeekBar thresholdBar = (SeekBar) mySettingsDialog.findViewById(R.id.threshold_seekbar);
		thresholdBar.setProgress(255 - threshold); //flip direction
		
		SeekBar sensitivityPitchBar = (SeekBar) mySettingsDialog.findViewById(R.id.sensitivity_pitch_seekbar);
		sensitivityPitchBar.setProgress(10 - maxPitch); //flip direction
	
		SeekBar sensitivityRollBar = (SeekBar) mySettingsDialog.findViewById(R.id.sensitivity_roll_seekbar);
		sensitivityRollBar.setProgress(10 - maxRoll); //flip direction
	   
   }
   
   
    void updateSensorInfo(int value){
    	
    	sensorTextView.setText("S: " + format.format(value)); 
    	
    	
    }
    
   /**
    * Creates the Debug Dialog
    */
    private void createDebugDialog(){
    	
    	myDebugDialog = new Dialog(this, R.style.TransparentTest);

    	
    	myDebugDialog.setContentView(R.layout.debug_dialog);
		

        Window window = myDebugDialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.RIGHT);
        window.setGravity(Gravity.BOTTOM);
      
    	
    }

    /**
     * Async Task handling the BT connection establishment and giving UI feedback
     */
    private class ConnectBT extends AsyncTask<String,Void,Boolean> {
        protected Boolean doInBackground(String... address) {
            return btKorsel.ConnectToUrl(address[0]);
        }

        protected void onPostExecute(Boolean result) {
        	
        	Context context = getApplicationContext();
        	if(result){
        		Toast.makeText(context, R.string.bt_connected, Toast.LENGTH_LONG).show();
        		
        		connectButtonView.setImageResource(R.drawable.buttons_activated_01);

	
        	}else{
        		Toast.makeText(context, R.string.bt_unable_to_connect, Toast.LENGTH_LONG).show();
        
        		
        	}
        }


    }
    
    /**
     * updates the Debug Dialog
     */
    private void updatedDebug(int motorLspeed, int motorRspeed){
    	
    	VerticalProgressBar leftForward = (VerticalProgressBar) myDebugDialog.findViewById(R.id.LeftForwardBar);
    	VerticalProgressBarTopDown leftBackward =(VerticalProgressBarTopDown) myDebugDialog.findViewById(R.id.LeftBackwardBar);
    	VerticalProgressBar rightForward = (VerticalProgressBar) myDebugDialog.findViewById(R.id.RightForwardBar);
    	VerticalProgressBarTopDown rightBackward =(VerticalProgressBarTopDown) myDebugDialog.findViewById(R.id.RightBackwardBar);
    	
    	if(motorLspeed >= 0){
    		leftForward.setProgress(motorLspeed);
    		leftBackward.setProgress(0);
    	}else{
    		leftForward.setProgress(0);
    		leftBackward.setProgress(Math.abs(motorLspeed));
    	}
    	
    	if(motorRspeed >= 0){
    		rightForward.setProgress(motorRspeed);
    		rightBackward.setProgress(0);
    	}else{
    		rightForward.setProgress(0);
    		rightBackward.setProgress(Math.abs(motorRspeed));
    	}
    		
    }
    
    /**
     * Handler for find new BT device intent and Enable BT intent
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(KorselControl.D) Log.d(KorselControl.TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                
            	
            	
            	
            	// Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            	
                btConnect(address);
            	
            }
            
            if(resultCode == Activity.RESULT_CANCELED){
            	//Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_LONG).show();
            }
            break;
  
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
            	//btDiscovery();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }
}
