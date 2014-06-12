/*
 * Copyright (C) 2013 Steffen Pfiffner
 * 
 * Licence: GPL v3
 */

package sp.KorselControl;

import java.io.IOException;
import java.io.InputStream;

import android.os.AsyncTask;
public class ReadSensorValueAsyncTask extends AsyncTask<Void, Integer, Integer> {

	KorselControl korselControl;

	boolean abort = false;

	private InputStream input;
	
	
		
	private int photoSensor = 0;


	public ReadSensorValueAsyncTask(KorselControl activity, InputStream inputStream) {

		this.korselControl = activity;
		
		this.input = inputStream;

	}
	

	/**
	 * @return the photoSensor
	 */
	public int getPhotoSensorValue() {
		return photoSensor;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		
		int readint = 0;

		while (!abort) {

			try {
				readint = input.read();
				
				
			} catch (IOException e) {
				
				System.out.println("\n Error: Connection to Korsel lost. \n");
				//e.printStackTrace();
				abort = true;
				return 0;
			}
		
			//If photo sensor command header received
			
			if(readint == KorselBluetoothSerial.PHOTO_SENSOR_COMMAND){	
				/*
				System.out.println("***Start***\n");
				
				System.out.println("Char: " + (char)readint + "\n");
				System.out.println("Int: " + readint + "\n");
				String binary = Integer.toBinaryString(readint);
				
				System.out.println("Binary: " + binary + "\n");
				*/
				
				try {
					readint = input.read();
				} catch (IOException e) {
					
					System.out.println("\n Error: Connection to Korsel lost. \n");
					abort = true;
					return 0;
				}
				
				if(readint != '\n'){
			
				
					photoSensor = readint;
						
					System.out.println("Photosensor: " + photoSensor + "\n");
					
					
				}
				
				//System.out.println("***Stop***\n");
			
			
			}else{
				
				if(readint != -1){
				System.out.println("***Start***\n");
				System.out.println("unknown command\n");
				System.out.println("Char: " + (char)readint + "\n");
				System.out.println("Int: " + readint + "\n");
				String binary = Integer.toBinaryString(readint);
				
				System.out.println("Binary: " + binary + "\n");
				System.out.println("***Stop***\n");
				}
			}
	
	      
			
			
			publishProgress(photoSensor);

			try {
				Thread.sleep(10);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}

			if (isCancelled()) {

				return 0;
			}

		}

		return 0;

	}

	@Override
	protected void onPostExecute(Integer result) {

		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

	
		
		korselControl.updateSensorInfo(values[0]);
		

		super.onProgressUpdate(values);
	}

}