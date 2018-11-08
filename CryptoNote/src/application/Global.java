package application;

import java.util.Optional;

import com.fazecast.jSerialComm.SerialPort;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import mrParser.Command;
import mrParser.Parser;
import serial.BlockingSerial;

public class Global {
	
	BlockingSerial bs = new BlockingSerial();

	// Zwraca FXMLLoadera powi¹zanego z folderem fxml
	public FXMLLoader getResLoader(String fxmlName)
	{
		return new FXMLLoader(this.getClass().getResource("/resources/fxml/"+fxmlName+".fxml"));
	}
	
	public String getDescriptiveNoteName(String noteName)
	{
		noteName = noteName.replace("/", "");
		noteName = noteName.replace(".txt", "");
		
		return noteName;
	}
	
	public String encodeNoteName(String noteName)
	{
		if(!noteName.contains("/") && !noteName.contains(".txt"))
			noteName = "/" + noteName + ".txt";
		
		return noteName;
	}
	
	
	public boolean unlock(String devName, SerialPort dev) {
		TextInputDialog dialog = new TextInputDialog();

		dialog.setTitle("Unlock device " + devName);
		dialog.setHeaderText("Enter password to unlock this device");
		dialog.setContentText("Enter password:");

		Optional<String> result = dialog.showAndWait();

		boolean devUnlocked = true;

		if(result.isPresent())
		{
			if (result.get().length() > 0) {
				Command c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<unlock:" + result.get() + ">", 100));
				if (c.isValid()) {
					if (c.getCommand().contains("err")) {
						Alert alert = new Alert(AlertType.ERROR, c.getProp().get(0), ButtonType.OK);
						alert.showAndWait();
						devUnlocked = false;
					}
				}
			}
			else
			{
				Alert alert = new Alert(AlertType.ERROR, "Device is still locked!", ButtonType.OK);
				alert.showAndWait();
				devUnlocked = false;
			}
		}
		else
		{
			devUnlocked = false;
		}

		return devUnlocked;
	}
}
