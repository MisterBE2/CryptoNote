package serial;

import com.fazecast.jSerialComm.SerialPort;

import tools.TimeWatch;

public class BlockingSerial {

	private String buffer = "";

	public String sendAwait(SerialPort comPort, String data, int timeout) {
		buffer = "";

		comPort.setBaudRate(115200);
		comPort.openPort();
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

		comPort.writeBytes(data.getBytes(), data.length());

		TimeWatch watch = TimeWatch.start();
		boolean doMainLoop = true;

		try {
			while (doMainLoop) {
				while (comPort.bytesAvailable() <= 0) {
					if (watch.time() >= timeout) {
						doMainLoop = false;
						break;
					}
				}

				int size = comPort.bytesAvailable();

				if (size > 0) {

					byte[] readBuffer = new byte[size];
					comPort.readBytes(readBuffer, readBuffer.length);
					buffer += new String(readBuffer);

					if ((buffer.contains("<") && buffer.contains(">")) || buffer.contains("\n")) {
						doMainLoop = false;
					}
					watch = TimeWatch.start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		comPort.closePort();
		return buffer;
	}

	public String sendAwaitToOpenedPort(SerialPort comPort, String data, int timeout) {
		buffer = "";
		
		System.out.println("========================= NEW TRANSMISION =========================");
		
		if(!comPort.isOpen())
		{
			comPort.setBaudRate(115200);
			comPort.openPort();
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
		}
		
		System.out.println("========================= Timeout set to: "+ timeout+ " Sending: " + data);
		comPort.writeBytes(data.getBytes(), data.length());

		TimeWatch watch = TimeWatch.start();
		boolean doMainLoop = true;

		try {
			while (doMainLoop) {
				while (comPort.bytesAvailable() == 0) {
					if (watch.time() > timeout) {
						doMainLoop = false;
						break;
					}
				}

				byte[] readBuffer = new byte[comPort.bytesAvailable()];
				comPort.readBytes(readBuffer, readBuffer.length);
				buffer += new String(readBuffer);

				if ((buffer.contains("<") && buffer.contains(">"))) {
					doMainLoop = false;
					System.out.println("========================= TRead comoplete: " + buffer + " in " + watch.time() + "ms");
					watch = null;
					return buffer;
				}

				watch = TimeWatch.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("========================= TRead incomplete: " + buffer + " timeout =  " + watch.time() + "ms");
		watch = null;
		return buffer;
	}
}
