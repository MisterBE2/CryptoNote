package serial;

import com.fazecast.jSerialComm.SerialPort;

import tools.TimeWatch;

public class BlockingSerial {

	private String buffer = "";
	
	public String sendAwait(SerialPort comPort, String data, int timeout)
	{
		buffer = "";
		
		comPort.setBaudRate(921600);
		comPort.openPort();
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

		comPort.writeBytes(data.getBytes(), data.length());
		
		TimeWatch watch = TimeWatch.start();
		boolean doMainLoop = true;
		
		
		try {
		   while (doMainLoop)
		   {		   
		      while (comPort.bytesAvailable() == 0)
		      {
		         if(watch.time() > timeout)
		         {
		        	 doMainLoop = false;
		        	 break;
		         }
		      }

		      byte[] readBuffer = new byte[comPort.bytesAvailable()];
		      comPort.readBytes(readBuffer, readBuffer.length);
		      buffer += new String(readBuffer);

		      if((buffer.contains("<") && buffer.contains(">")) || buffer.contains("\n"))
		      {
		    	  doMainLoop = false;
		      }
		      
		      watch = TimeWatch.start();

		   }
		} catch (Exception e) { e.printStackTrace(); }
		
		comPort.closePort();
		return buffer;
	}
	
	public String sendAwaitToOpenedPort(SerialPort comPort, String data, int timeout)
	{
		buffer = "";
		comPort.writeBytes(data.getBytes(), data.length());
		
		TimeWatch watch = TimeWatch.start();
		boolean doMainLoop = true;
		
		try {
		   while (doMainLoop)
		   {		   
		      while (comPort.bytesAvailable() == 0)
		      {
		         if(watch.time() > timeout)
		         {
		        	 doMainLoop = false;
		        	 break;
		         }
		      }

		      byte[] readBuffer = new byte[comPort.bytesAvailable()];
		      comPort.readBytes(readBuffer, readBuffer.length);
		      buffer += new String(readBuffer);

		      if((buffer.contains("<") && buffer.contains(">")))
		      {
		    	  doMainLoop = false;
		      }
		      
		      watch = TimeWatch.start();

		   }
		} catch (Exception e) { e.printStackTrace(); }
		
		return buffer;
	}
}
