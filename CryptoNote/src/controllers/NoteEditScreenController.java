package controllers;

import java.util.Arrays;

import com.fazecast.jSerialComm.SerialPort;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import mrParser.Command;
import mrParser.Parser;
import serial.BlockingSerial;
import serial.SerialConnection;

public class NoteEditScreenController {

	private HomeScreenController hsc;
	private Stage thisStage;
	private BlockingSerial bs = new BlockingSerial();
	private int devBufferSize = 0;
	private boolean newNode = false;
	private SerialPort dev;

	@FXML
	private TextField textFieldTitle;

	@FXML
	private TextArea textFieldContent;

	@FXML
	void onActionCancel() {
		thisStage.close();
	}

	@FXML
	void onActionSave() {
		int bufMaxSize = devBufferSize - 9;

		if (bufMaxSize > 0) {

			String note = textFieldTitle.getText();

			if (note.contains("/") && note.contains(".txt") && note.length() > 5) {
				Command c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<open:" + note + ":w>", 100));

				System.out.println("Open file response: " + c.getCommand());

				if (c.isValid())
					if (c.getCommand().contains("ok")) {
						String[] buf = textFieldContent.getText().split("(?<=\\G.{" + bufMaxSize + "})");
						for (String string : buf) {
							String out = "<append:" + string + ">";
							c = Parser.parse(bs.sendAwaitToOpenedPort(dev, out, 2000));
							if (c.isValid()) {
								if (c.getCommand().contains("err")) {
									Alert alert = new Alert(AlertType.WARNING, c.getProp().get(0), ButtonType.OK);
									alert.showAndWait();
								} else
									System.out.println("Append command response: " + c.getCommand());
							}
						}
						c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<close>", 100));
						if (c.isValid()) {
							if (c.getCommand().contains("err")) {
								Alert alert = new Alert(AlertType.WARNING, c.getProp().get(0), ButtonType.OK);
								alert.showAndWait();
							} else
								System.out.println("Close command response: " + c.getCommand());
						}
					} else if (c.getCommand().contains("err")) {
						Alert alert = new Alert(AlertType.WARNING, c.getProp().get(0), ButtonType.OK);
						alert.showAndWait();
					} else
						System.out.println("Err: Unexpected command: " + c.getCommand() + ", while openineg file");

			}
		} else
			System.out.println("Can't save note - devBuffSize is too small");

		hsc.populateDeviceInfo();
		hsc.populateNoteList();

		thisStage.close();
	}

	public void setStage(Stage stage) {
		this.thisStage = stage;
	}

	public void setHSC(HomeScreenController hsc) {
		this.hsc = hsc;
	}

	public void setTitle(String title) {
		textFieldTitle.setText(title);
	}

	public void setContent(String content) {
		textFieldContent.setText(content);
	}

	public void setDevBufferSize(int size) {
		this.devBufferSize = size;
	}

	public void isNewNote(boolean n) {
		this.newNode = n;
	}

	public void setSerialPort(SerialPort dev) {
		this.dev = dev;
	}
}
