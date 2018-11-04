package controllers;

import java.io.IOException;
import java.text.DecimalFormat;

import com.fazecast.jSerialComm.SerialPort;

import application.Global;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import mrParser.Command;
import mrParser.Parser;
import serial.BlockingSerial;
import serial.SerialConnection;

public class HomeScreenController {

	private SerialPort dev;
	private BlockingSerial bs = new BlockingSerial();
	private Global g = new Global();

	@FXML
	private VBox VBoxTitles;

	@FXML
	private Label labelTitle;

	@FXML
	private Label labelContent;

	@FXML
	private Label labelDevName;

	@FXML
	private Label labelDevID;

	@FXML
	private Label labelDevMemory;

	@FXML
	private ProgressBar progressBarMem;

	@FXML
	void onActionAddNote() {

	}

	@FXML
	void onaActionDeleteNote() {

	}

	@FXML
	void onaActionEditNote() {

	}

	@FXML
	void onaActionRenameNote() {

	}

	@FXML
	public void initialize() {
		labelTitle.setText("");
		labelContent.setText("");
	}

	public void openNote(String notePath) {
		Command notes = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<read:" + notePath + ">", 10000));
		if (notes.isValid()) {
			if (notes.getCommand().contains("read")) {
				labelTitle.setText(notePath);
				labelContent.setText(notes.getProp().get(0));
			} else
				System.out.println("Err: Unexpected command: " + notes.getCommand() + ", while loading note " + notePath);
		}
	}

	public void setDevice(SerialPort dev) {
		this.dev = dev;
		
		SerialConnection.setSerialPort(dev);
		
		dev.setBaudRate(921600);
		dev.openPort();
		dev.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);


		Command notes = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<list>", 200));
		if (notes.isValid()) {
			if (notes.getCommand().contains("list")) {
				VBoxTitles.getChildren().clear();
				for (String note : notes.getProp()) {

					FXMLLoader ld = g.getResLoader("TitleNode");
					Label newB;
					try {
						newB = ld.load();

						TitleNodeController tnc = ld.getController();
						tnc.setName(note);
						tnc.setHSC(this);

						VBoxTitles.getChildren().add(newB);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else
				System.out.println("Err: Unexpected command: " + notes.getCommand() + ", while loading notes");
		}

		Command deviceInfo = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<getInfo>", 200));
		if (deviceInfo.isValid()) {
			if (deviceInfo.getCommand().contains("devInfo")) {
				labelDevName.setText(deviceInfo.getProp().get(0));
				labelDevID.setText(deviceInfo.getProp().get(1));
			} else
				System.out
						.println("Err: Unexpected command: " + deviceInfo.getCommand() + ", while loading deviceInfo");
		}

		Command memInfo = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<getMemInfo>", 200));
		if (memInfo.isValid()) {
			if (memInfo.getCommand().contains("memInfo")) {
				DecimalFormat df = new DecimalFormat("0.##");
				int totalBytes = Integer.parseInt(memInfo.getProp().get(0));
				int usedBytes = Integer.parseInt(memInfo.getProp().get(1));

				double a, b;
				a = usedBytes / 1024.0;
				b = (totalBytes / 1024.0);

				String out = "";
				out += df.format(a);
				out += "b / ";
				out += df.format(b);
				out += "b";

				labelDevMemory.setText(out);

				double progress = usedBytes / totalBytes;

				progressBarMem.setProgress(progress);

			} else
				System.out
						.println("Err: Unexpected command: " + memInfo.getCommand() + ", while loading deviceMemInfo");
		}
	}
}