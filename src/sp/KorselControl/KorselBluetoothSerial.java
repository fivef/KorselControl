package sp.KorselControl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Class representing the Korsel robot
 * 
 * Connect, disconnect to/from robot sending steering commands to the Korsel and
 * receiving the photo sensor state
 */
public class KorselBluetoothSerial {

	// final UUID SERIAL_PORT_SERVICE = new UUID(0x1101);

	/**
	 * Command definitions
	 */
	final char LEFT_MOTOR_SPEED_FORWARD_COMMAND = 0x01;
	final char LEFT_MOTOR_SPEED_BACKWARD_COMMAND = 0x02;
	final char RIGHT_MOTOR_SPEED_FORWARD_COMMAND = 0x03;
	final char RIGHT_MOTOR_SPEED_BACKWARD_COMMAND = 0x04;

	// auxiliary commands
	final char ENABLE_AUTO_COMMAND = 0x05;
	final char AUX_COMMAND = 0x06;

	public final static char PHOTO_SENSOR_COMMAND = 0x07;

	// values up to this number are reserved for commands and can't be used as
	// speed value
	public final static int RESERVED_FOR_COMMANDS = 10;

	private OutputStream output = null;
	private InputStream input = null;

	private boolean connected = false;

	private AsyncTask<Void, Integer, Integer> readSensorValueAsyncTask;

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;

	private KorselControl korselControlActivity;

	// Well known SPP UUID (will *probably* map to RFCOMM channel 1
	// (default) if not in use);

	// see comments in onResume().
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static final String TAG = "THINBTCLIENT";

	public KorselBluetoothSerial(KorselControl korselControlActivity) {

		this.korselControlActivity = korselControlActivity;
		
		
	}

	/**
	 * Commands
	 */

	public void sendEnableAutoCommand() {

		sendCommand(ENABLE_AUTO_COMMAND, 1);

	}

	public void sendDisableAutoCommand() {

		sendCommand(ENABLE_AUTO_COMMAND, 0);
	}

	public void sendAuxCommand() {

		sendCommand(AUX_COMMAND, 0);
	}

	public void sendCommand(char command, int value) {

		try {
			
			if(KorselControl.D) Log.d(KorselControl.TAG, "Sent command: " + command + " with value: " + value);

			output.write(command);


			output.write(value);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			connected = false;

		}
	}

	/**
	 * @param the
	 *            left motor speed to set
	 */
	public void setLeftMotorSpeed(int MotorSpeed) {

		setMotorSpeed('L', MotorSpeed);

	}

	/**
	 * @param the
	 *            right motor speed to set
	 */
	public void setRightMotorSpeed(int MotorSpeed) {

		setMotorSpeed('R', MotorSpeed);

	}

	/**
	 * 
	 * @param Motor
	 *            position left or right [L,R]
	 * @param Left
	 *            motor speed [-255..255]
	 */
	private void setMotorSpeed(char position, int speed) {

		try {

			if (position == 'L' && speed >= 0) {

				output.write(LEFT_MOTOR_SPEED_FORWARD_COMMAND);
			}

			if (position == 'R' && speed >= 0) {
				output.write(RIGHT_MOTOR_SPEED_FORWARD_COMMAND);
			}

			if (position == 'L' && speed < 0) {

				output.write(LEFT_MOTOR_SPEED_BACKWARD_COMMAND);
			}

			if (position == 'R' && speed < 0) {
				output.write(RIGHT_MOTOR_SPEED_BACKWARD_COMMAND);
			}

			// dont' send speeds smaller than RESERVED_FOR_COMMANDS, these
			// values are used for commands
			/*
			if (Math.abs(speed) <= RESERVED_FOR_COMMANDS) {
				output.write(RESERVED_FOR_COMMANDS + 1);

			} else {
				output.write(Math.abs(speed));

			}
			*/
			
			output.write(Math.abs(speed));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			connected = false;

		}

	}

	/**
	 * Connects to a given bluetooth url, opens an output stream and the
	 * inputStream thread.
	 * 
	 * @param Bluetooth
	 *            url to connect to
	 */
	public boolean ConnectToUrl(String address) {

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// When this returns, it will 'know' about the server, via it's MAC
		// address.
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

		// We need two things before we can successfully connect (authentication
		// issues
		// aside): a MAC address, which we already have, and an RFCOMMchannel.
		// Because RFCOMM channels (aka ports) are limited in number, Android
		// doesn't allow
		// you to use them directly; instead you request a RFCOMM mapping based
		// on a service
		// ID. In our case, we will use the well-known SPP Service ID. This ID
		// is in UUID
		// (GUID to you Microsofties) format. Given the UUID, Android will
		// handle the
		// mapping for you. Generally, this will return RFCOMM 1, but not
		// always; it
		// depends what other BlueTooth services are in use on your Android
		// device.
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Socket creation failed.", e);
			return false;
		}

		// Discovery may be going on, e.g., if you're running a 'scan for
		// devices' search
		// from your handset's Bluetooth settings, so we call cancelDiscovery().
		// It doesn't
		// hurt to call it, but it might hurt not to... discovery is a
		// heavyweight process;
		// you don't want it in progress when a connection attempt is made.
		mBluetoothAdapter.cancelDiscovery();

		// Blocking connect, for a simple client nothing else can happen until a
		// successful
		// connection is made, so we don't care if it blocks.

		try {
			btSocket.connect();
			Log.e(TAG,
					"ON RESUME: BT connection established, data transfer link open.");
		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: BT connection failure \n");

			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.e(TAG,
						"ON RESUME: Unable to close socket during connection failure",
						e2);
				return false;
			}
			return false;
		}

		// Create a data stream so we can talk to server.

		try {
			output = btSocket.getOutputStream();
			connected = true;

		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
			return false;
		}

		try {
			input = btSocket.getInputStream();
			connected = true;

		} catch (IOException e) {
			Log.e(TAG, "ON RESUME: Input stream creation failed.", e);
			return false;
		}

		readSensorValueAsyncTask = new ReadSensorValueAsyncTask(
				korselControlActivity, input).execute();
		// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return true;

	}

	/**
	 * Closes the streams and exits the program
	 */
	public void disconnect() {
		try {
			// inputStreamThread.interrupt();
			if (output != null) {
				output.close();
			}

			if (btSocket != null) {
				btSocket.close();
			}
			// input.close();
			// System.exit(0);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public boolean isConnected() {

		return connected;

	}

}
