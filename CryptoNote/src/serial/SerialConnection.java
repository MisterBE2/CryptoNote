package serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class SerialConnection {

	private static SerialPort port;
	private static int baudRate;
	private static InputStream in;
	private static OutputStream out;
	private static List<String> inLog = new ArrayList<String>();
	private static List<SerialInputListener> listeners = new ArrayList<SerialInputListener>();
	private static String serialBuffer = "";

	public static void setSerialPort(SerialPort _port) {
		// Makes sure to close last port
		// This allows to later use last port without restarting serial device
		if (port != null)
			port.closePort();

		port = _port;
	}

	public static void setBaudRate(int _baudRate) {
		baudRate = _baudRate;
	}

	public static SerialPort getSerialPort() {
		return port;
	}

	public static int getBaudRate() {
		return baudRate;
	}

	// Returns true when port successfully opened
	public static boolean openPort() {
		if (port != null) {
			if (port.isOpen())
				return true;
			else {
				if (baudRate != 0) {
					port.setNumDataBits(8);
					port.setNumStopBits(1);
					port.setParity(0);
					port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0); // As advised in
																							// jSerialComm, this
																							// function decreases load
																							// on CPU
					port.setBaudRate(baudRate);
					port.openPort();

					in = port.getInputStream();
					out = port.getOutputStream();

					inLog.clear();

					port.addDataListener(new SerialPortDataListener() {

						@Override
						public void serialEvent(SerialPortEvent event) {
							
							// Loads buffer of bits (Usually ACII encoded text)
							byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
							event.getSerialPort().readBytes(buffer, buffer.length);

							String data = new String(buffer);

							if (data != null && data.length() > 0) {
								serialBuffer += data;

								System.out.println("Recived: " + data);

								if ((serialBuffer.contains("<") && serialBuffer.contains(">"))
										|| serialBuffer.contains("\n")) {
									System.out.println("Combined message: " + serialBuffer);
									
									// Adds received data to history list
									//inLog.add(serialBuffer);

									// Triggers received data event
									//messageListeners(serialBuffer);

									serialBuffer = "";
								} else {
									//System.out.println("Multi part message");
								}
							}
						}

						@Override
						public int getListeningEvents() {
							//return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
							return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
						}
					});

					return true;
				} else
					return false;
			}
		} else
			return false;
	}

	public static void restartPort() {
		if (port != null) {
			closePort();
			openPort();
		}
	}

	public static void closePort() {
		if (port != null)
			port.closePort();
	}

	public static boolean send(String msg) {
		try {
			out.write(msg.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void addListener(SerialInputListener li) {
		listeners.add(li);
	}

	public static void removeListener(SerialInputListener listener) {
		for (int i = listeners.size() - 1; i > 0; i--) {
			if (listeners.get(i).equals(listener)) {
				listeners.remove(i);
				break;
			}

		}
	}

	private static void messageListeners(String msg) {
		for (SerialInputListener listener : listeners) {
			listener.newInput(msg);
		}
	}
}
