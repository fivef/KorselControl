/*
 * Copyright (C) 2013 Steffen Pfiffner
 * 
 * Licence: GPL v3
 */

package sp.KorselControl;

import java.io.IOException;
import java.io.InputStream;

import android.os.AsyncTask;
public class ReadSensorValueAsyncTask extends AsyncTask<Void, Boolean, Boolean> {

	KorselControl korselControl;

	boolean abort = false;

	private InputStream input;
	
	
		
	private boolean photoSensor = false;


	public ReadSensorValueAsyncTask(KorselControl activity, InputStream inputStream) {

		this.korselControl = activity;
		
		this.input = inputStream;

	}
	

	/**
	 * @return the photoSensor
	 */
	public boolean isPhotoSensor() {
		return photoSensor;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		
		int readint = 0;

		while (!abort) {

			try {
				readint = input.read();
				
				
			} catch (IOException e) {
				
				System.out.println("\n Error: Connection to Korsel lost. \n");
				//e.printStackTrace();
				abort = true;
				return false;
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
					return false;
				}
				
				if(readint != '\n'){
					/*
					System.out.println("Char: " + (char)readint + "\n");
					System.out.println("Int: " + readint + "\n");
					
					String binary = Integer.toBinaryString(readint);
					
					System.out.println("Binary: " + binary + "\n");
					*/
					
					if(readint == 1){
						
						photoSensor = true;
						
				
					}
					
					if(readint == 0){
						
						photoSensor = false;
					
					}
					
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

				return false;
			}

		}

		return false;

	}

	@Override
	protected void onPostExecute(Boolean result) {

		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Boolean... values) {

	
		
		korselControl.updateSensorImage(values[0]);
		

		super.onProgressUpdate(values);
	}

}