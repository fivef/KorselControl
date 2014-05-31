/*
 * Copyright (C) 2013 Steffen Pfiffner
 * 
 * Licence: GPL v3
 */

package sp.KorselControl;

import android.os.AsyncTask;
public class ReadSensorValueAsyncTask extends AsyncTask<Void, Double, Double> {

	KorselControl korselControl;

	boolean abort = false;

	double sensorState;


	public ReadSensorValueAsyncTask(KorselControl activity) {

		this.korselControl = activity;

	}

	@Override
	protected Double doInBackground(Void... params) {

		while (!abort) {

			try {

				volts = drinkMixer.getAnalogIO().getVoltage(4);

			} catch (EthernetControlException e) {

				System.out.println("ReadInputValues: No Connection");

			} catch (java.lang.StringIndexOutOfBoundsException e) {

				System.out
						.println("ReadInputValues: No Connection (StringIndexOutofBoundsException");
			} catch (java.lang.NullPointerException e) {

				System.out
						.println("ReadInputValues: No Connection (NullPointerException)");
			} catch (java.lang.NumberFormatException e) {

				System.out
						.println("ReadInputValues: No Connection (NumberFormatException)");
			}

			
			

			

				double value = volts;

				
				
				value = value - DrinkMixer.PRESSURE_SENSOR_OFFSET;

				value = value / 10;

				if (value < 0) {
					value = 0.0;
				}
				
				//only start/stop compressor if pressure control is enabled
				if(drinkMixer.isPressureControlEnabled()){
					
					if(value >= drinkMixer.getPressureSetPoint())	{
						
						
							drinkMixer.closeValve(DrinkMixer.COMPRESSOR_PIN);
					
					}
					
					if(value <= drinkMixer.getPressureSetPoint() - DrinkMixer.PRESSURE_SENSOR_HYSTERESIS ){
						
					
							drinkMixer.openValve(DrinkMixer.COMPRESSOR_PIN);
					
					}
					
					
				}
				
				DecimalFormat df = new DecimalFormat("#.####");
				
				String valueString = df.format(value);
				
				System.out.println(valueString);
			
			
			publishProgress(value);

			try {
				Thread.sleep(DrinkMixer.PRESSURE_SENSOR_SAMPLING_PRERIOD);
				
				//TODO: make sleep time adjustable in settings

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}

			if (isCancelled()) {

				return 0.0;
			}

		}

		return 0.0;

	}

	@Override
	protected void onPostExecute(Double result) {

		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Double... values) {

		if (activity.settingsFragment != null
				&& activity.settingsFragment.isVisible()) {

			DecimalFormat df = new DecimalFormat("#.####");
			
			activity.settingsFragment.setPressureValue(df.format(values[0]));
		}

		super.onProgressUpdate(values);
	}

}