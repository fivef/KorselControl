package sp.KorselControl;

import java.io.IOException;
import java.io.InputStream;

/**
 * Thread used by KorselBluetoothSerial Class
 * to receive the photo sensor state of the Korsel
 */
public class InputStreamThread extends Thread {
	
	
	private InputStream input;
	
	private int readint = 0;
		
	private boolean photoSensor = false;
	
/**
	 * @return the photoSensor
	 */
	public boolean isPhotoSensor() {
		return photoSensor;
	}

	/*
 * @param inputStream the inputStream to read from
 */
	public InputStreamThread(InputStream inputStream){
		
		input = inputStream;
		
	}

	public void run() {
	      for(;;) {
	        try {
	          sleep(10);
	        }
	        catch(InterruptedException e) {
	        }
	        
	        
	    	
			try {
				readint = input.read();
				
				
			} catch (IOException e) {
				
				System.out.println("\n Error: Connection to Korsel lost. \n");
				//e.printStackTrace();
				System.exit(0);
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
					//e.printStackTrace();
					System.exit(0);
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
	
	      }
	    }

}
