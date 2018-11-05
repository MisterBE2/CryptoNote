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
		int bufMaxSize = devBufferSize - 100;
		int maxRetry = 10;
		int curRetry = 0;

		if (bufMaxSize > 0) {
			
			String note = textFieldTitle.getText();
			String content = textFieldContent.getText();
			content = content.replace(":", "-");
			content = content.replace("<", "-");
			content = content.replace(">", "-");

			if (note.contains("/") && note.contains(".txt") && note.length() > 5) {
				Command c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<open:" + note + ":w>", 100));
				c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<append>", 100));
				c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<close>", 100));

				try {
					Thread.sleep(200);
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<open:" + note + ":a>", 100));
				
				int chunk = 100; // chunk size to divide
				for(int i=0;i<content.length();i+=chunk){
					char tempBuff[] = Arrays.copyOfRange(content.toCharArray(), i, Math.min(content.length(),i+chunk));
					String out = new String(tempBuff);
				    
				    c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<append:" + out + ">", devBufferSize*5));

					while (!c.is("ok") && curRetry <= maxRetry) {
						try {
							Thread.sleep(500);
						} catch (Exception e) {
							// TODO: handle exception
						}
						System.out.println("Append error, retrying");
						c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<append:" + out + ">", devBufferSize*5));
						curRetry++;
					}

					if (curRetry >= maxRetry)
						break;

					curRetry = 0;
				}  

//				c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<open:" + note + ":a>", 100));
//
//				System.out.println("Open file response: " + c.getCommand());
//
//				if (c.isValid())
//					if (c.getCommand().contains("ok")) {
//
//						char tempBuff[] = content.toCharArray();
//
//						for (int i = 0; i < content.length(); i++) {
//							c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<append:" + tempBuff[i] + ">", 1000));
//
//							thisStage.setTitle("Saving: " + (i/content.length()) + " %");
//							
//							while (!c.is("ok") && curRetry <= maxRetry) {
//								try {
//									Thread.sleep(500);
//								} catch (Exception e) {
//									// TODO: handle exception
//								}
//								System.out.println("Append error, retrying");
//								c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<append:" + tempBuff[i] + ">", 1000));
//								curRetry++;
//							}
//
//							if (curRetry >= maxRetry)
//								break;
//
//							curRetry = 0;
//						}
//					}

				if (curRetry >= maxRetry)
					System.out.println(" ####### Error while saving message! #######");

//						String[] buf = textFieldContent.getText().split("(?<=\\G.{" + bufMaxSize + "})");
//						for (String string : buf) {
//							String out = "<append:" + string + ">";
//							c = Parser.parse(bs.sendAwaitToOpenedPort(dev, out, 2000));
//							if (c.isValid()) {
//								if (c.getCommand().contains("err")) {
//									Alert alert = new Alert(AlertType.WARNING, c.getProp().get(0), ButtonType.OK);
//									alert.showAndWait();
//								} else
//									System.out.println("Append command response: " + c.getCommand());
//							}
//							
//							try {
//								Thread.sleep(200);
//							} catch (Exception e) {
//								// TODO: handle exception
//							}

				c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<close>", 100));
				if (c.isValid()) {
					if (c.getCommand().contains("err")) {
						Alert alert = new Alert(AlertType.WARNING, c.getProp().get(0), ButtonType.OK);
						alert.showAndWait();
					} else
						System.out.println("Close command response: " + c.getCommand());
				}
			}
		} else
			System.out.println("Can't save note - devBuffSize is too small");

		try {
			Thread.sleep(200);
		} catch (Exception e) {
			// TODO: handle exception
		}
		hsc.populateDeviceInfo();

		try {
			Thread.sleep(200);
		} catch (Exception e) {
			// TODO: handle exception
		}
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
