package serial;

import com.fazecast.jSerialComm.SerialPort;

import tools.TimeWatch;

public class BlockingSerial {

	private String buffer = "";

	public String sendAwait(SerialPort comPort, String data, int timeout) {
		buffer = "";

		System.out.println("========================= NEW TRANSMISION =========================");

		comPort.setBaudRate(115200);
		comPort.openPort();
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

		System.out.println("========================= Timeout set to: " + timeout + " Sending: " + data);
		comPort.writeBytes(data.getBytes(), data.length());

		TimeWatch watch = TimeWatch.start();
		boolean doMainLoop = true;

		try {
			while (doMainLoop) {
				while (comPort.bytesAvailable() == 0) {
					if (watch.time() >= timeout) {
						doMainLoop = false;
						break;
					}
				}

				byte[] readBuffer = new byte[comPort.bytesAvailable()];
				comPort.readBytes(readBuffer, readBuffer.length);
				buffer += new String(readBuffer);

				if ((buffer.contains("<") && buffer.contains(">"))) {
					System.out.println(
							"========================= TRead comoplete: " + buffer + " in " + watch.time() + "ms");
					watch = null;
					comPort.getInputStream().reset();
					doMainLoop = false;
					comPort.closePort();
					return buffer;
				}
				watch = TimeWatch.start();
			}
		} catch (Exception e) {
			System.out.println("Couldn't send / recive to/from device");
		}

		if (watch != null)
			System.out.println(
					"========================= TRead incomplete: " + buffer + " timeout =  " + watch.time() + "ms");
		watch = null;
		comPort.closePort();
		return buffer;
	}

	public String sendAwaitToOpenedPort(SerialPort comPort, String data, int timeout) {
		buffer = "";

		System.out.println("========================= NEW TRANSMISION =========================");

		if (!comPort.isOpen()) {
			comPort.setBaudRate(115200);
			comPort.openPort();
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
		}

		System.out.println("========================= Timeout set to: " + timeout + " Sending: " + data);
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
					System.out.println(
							"========================= TRead comoplete: " + buffer + " in " + watch.time() + "ms");
					comPort.getInputStream().reset();
					watch = null;
					return buffer;
				}

				watch = TimeWatch.start();
			}
		} catch (Exception e) {
			System.out.println("Couldn't send / recive to/from device");
		}

		System.out.println(
				"========================= TRead incomplete: " + buffer + " timeout =  " + watch.time() + "ms");
		watch = null;
		return buffer;
	}
}
