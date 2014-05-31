package sp.KorselControl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingsDialog extends Activity {

	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Setup the window
	        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	        setContentView(R.layout.device_list);
	        
	 }
	 
	   @Override
	    protected void onDestroy() {
	        super.onDestroy();


	    }


	    //Click Listener
	    private OnClickListener clickListener = new OnClickListener() {
	        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
	              
	            //if user selected no device
	            String noDevices = getResources().getText(R.string.none_paired).toString();

	            // Get the device MAC address, which is the last 17 chars in the View
	            String info = ((TextView) v).getText().toString();
	            
	            
	            if(info.contains(noDevices)){
	            	setResult(Activity.RESULT_CANCELED);
	            	finish();
	            }
	            
	            String address = info.substring(info.length() - 17);

	          

	           
	        }

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				  // Create the result Intent and include the MAC address
	            Intent intent = new Intent();
	            intent.putExtra(EXTRA_MAX_ROLL, address);
	            intent.putExtra(EXTRA_MAX_PITCH, address);
	            intent.putExtra(EXTRA_MAX_ROLL, address);
	            
	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();
				
			}
	    };
}
