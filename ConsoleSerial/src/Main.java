import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class Main {

	public static void main(String[] args) {
		
		final String justSended = "";

		SerialPort comPort = SerialPort.getCommPorts()[0];
		comPort.setBaudRate(115200);
		comPort.openPort();

		System.out.println("Connected:" + comPort.getDescriptivePortName());

		comPort.addDataListener(new SerialPortDataListener() {
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					return;
				
				byte[] newData = new byte[comPort.bytesAvailable()];
				int numRead = comPort.readBytes(newData, newData.length);
				if (numRead > 0)
				{
					System.out.println(new String(newData));
				}
			}
		});


		while (true) {
			String data;
			System.out.print("=>");

			Scanner scanner = new Scanner(System.in);
			data = scanner.nextLine();

			if (data.contains("exit"))
				System.exit(0);
			else if (comPort.isOpen())
			{
				comPort.writeBytes(data.getBytes(), data.length());
			}
		}
	}
}
