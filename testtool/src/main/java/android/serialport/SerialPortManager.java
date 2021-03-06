package android.serialport;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public abstract class SerialPortManager{
	private static final String TAG = "scan->SerialPortManager";
	private Context mContext;
	private SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	protected InputStream mInputStream;
	private boolean isReadIng = false;
	private boolean isThreadRead = false;
	private ReadThread mReadThread = null;
	public static final int RET_DEVICE_OPENED = 1;
	public static final int RET_OPEN_SUCCESS = 0;
	public static final int RET_NO_PRTMISSIONS = -1;
	public static final int RET_ERROR_CONFIG = -2;
	public static final int RET_ERROR_UNKNOW = -3;
	public static final int max_size = 2048;

	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			
			while(!isInterrupted() && isThreadRead) {
				int size;
				try {
					byte[] buffer = new byte[max_size];
					if (mInputStream == null){
						Log.e(TAG, "mInputStream == null");
						continue;
					}
					size = mInputStream.read(buffer);
					if (size > 0) {
						if(size > max_size)
							size = max_size;
						if(isReadIng){
							onDataReceived(buffer, size);
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "IOException");
					e.printStackTrace();
					return;
				}
			}
		}
	}

//	private void DisplayError(String string) {
//		AlertDialog.Builder b = new AlertDialog.Builder(mContext);
//		b.setTitle("Error");
//		b.setMessage(string);
//		b.setPositiveButton("OK", new OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//				closeSerialPort();
//			}
//		});
//		b.show();
//	}

	public SerialPortManager(Context context) {
		mContext = context;
	}

	protected abstract void onDataReceived(final byte[] buffer, final int size);
	
	protected int openSerialPort(File device, int baudrate, int flow_ctrl, int databits, int stopbits, int parity){
		Log.d(TAG, "openSerialPort");
		try {
			if(mSerialPort == null){
				mSerialPort = new SerialPort(device, baudrate, flow_ctrl, databits, stopbits, parity);
				if(mSerialPort != null){
					mOutputStream = mSerialPort.getOutputStream();
					mInputStream = mSerialPort.getInputStream();
//					byte[] buffer = new byte[max_size];
//					mInputStream.read(buffer);
					return RET_OPEN_SUCCESS;
				}
			}
			else return RET_DEVICE_OPENED;
		} catch (SecurityException e) {
//			DisplayError("You do not have read/write permission to the serial port.");
			return RET_NO_PRTMISSIONS;
		} catch (IOException e) {
//			DisplayError("The serial port can not be opened for an unknown reason.");
			return RET_ERROR_UNKNOW;
		} catch (InvalidParameterException e) {
//			DisplayError("Please configure your serial port first.");
			return RET_ERROR_CONFIG;
		}
		return RET_ERROR_UNKNOW;
	}
	
	
	protected void startReadSerialPort() {
		isReadIng = true;
		Log.d(TAG, "startReadSerialPort");
		if(mReadThread == null || isThreadRead == false){
			isThreadRead = true;
			mReadThread = new ReadThread();
			mReadThread.start();
		}
	}
	
	protected void stopReadSerialPort() {
		Log.d(TAG, "stopReadSerialPort");
		isReadIng = false;
	}
	
	public int writeSerialPort(byte[] data){
		if(mOutputStream != null && data != null){
			try {
				mOutputStream.write(data);
				mOutputStream.flush();
				return 0;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		}else return -2;
	}

	protected byte[] readSerialPort(int timeOut){
		if(mInputStream != null){
			try {
				int count = 0;
				while (count == 0 && timeOut > 0) {
					timeOut--;
					count = mInputStream.available(); 
					if(count <= 0){
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else break;
				}
				byte[] buffer = new byte[count];
				int size = mInputStream.read(buffer, 0, count);
				if(size <= 0)
					return null;
				return buffer;
			} catch (IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}else return null;
	}
	
	
	protected void closeSerialPort() {
		Log.d(TAG, "closeSerialPort");
		isReadIng = false;
		if (mReadThread != null){
			isThreadRead = false;
			mReadThread.interrupt();
		}
		mReadThread = null;
		if(mSerialPort != null)
			mSerialPort.closeSerialPort();
		mSerialPort = null;
	}
}
