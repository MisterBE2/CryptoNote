package controllers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Optional;

import com.fazecast.jSerialComm.SerialPort;

import application.Global;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mrParser.Command;
import mrParser.Parser;
import serial.BlockingSerial;
import serial.SerialConnection;

public class HomeScreenController {

	private SerialPort dev;
	private BlockingSerial bs = new BlockingSerial();
	private Global g = new Global();

	private Stage stage; // Stage for edit note scene

	private int devBufferSize = 0;

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
	private Label labelDevIBuffer;

	@FXML
	private ProgressBar progressBarMem;

	@FXML
	void onActionAddNote() {
		showEditor(true);
	}

	@FXML
	void onaActionDeleteNote() {

		if (labelTitle.getText().length() > 0) {
			Alert alert = new Alert(AlertType.WARNING, "Are you sure you want to delete " + labelTitle.getText() + "?",
					ButtonType.YES, ButtonType.NO);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				Command c = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<remove:" + labelTitle.getText() + ">", 100));
				if (c.isValid()) {
					if (c.getCommand().contains("err")) {
						alert = new Alert(AlertType.WARNING, c.getProp().get(0), ButtonType.OK);
						alert.showAndWait();
					} else
						System.out.println("Delete command response: " + c.getCommand());
				}
			}

			labelTitle.setText("");
			labelContent.setText("");

			populateNoteList();
			populateDeviceInfo();
		}
	}

	public void showEditor(boolean newNote) {
		if (stage == null) {
			FXMLLoader ld = g.getResLoader("NoteEditScreen");
			try {
				AnchorPane ap = ld.load();
				NoteEditScreenController nsc = ld.getController();
				nsc.setHSC(this);
				nsc.setDevBufferSize(devBufferSize);
				nsc.isNewNote(newNote);
				nsc.setSerialPort(dev);

				if (!newNote) {
					nsc.setTitle(labelTitle.getText());
					nsc.setContent(labelContent.getText());
				}

				stage = new Stage();
				nsc.setStage(stage);

				Scene scene = new Scene(ap);

				stage.setTitle("Note editor");
				stage.setScene(scene);
				stage.setResizable(true);
				stage.showAndWait();

				if (!newNote)
					if (labelTitle.getText().length() > 0)
						openNote(labelTitle.getText());

				stage = null;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	void onaActionEditNote() {
		if (labelTitle.getText().length() > 0)
			showEditor(false);
	}

	@FXML
	void onaActionRenameNote() {
		if (labelTitle.getText().length() > 0) {
			TextInputDialog dialog = new TextInputDialog(labelTitle.getText());

			dialog.setTitle("Rename note");
			dialog.setHeaderText("Enter new note name:");
			dialog.setContentText("Name:");

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(name -> {
				Command c = Parser.parse(
						bs.sendAwaitToOpenedPort(dev, "<rename:" + labelTitle.getText() + ":" + name + ">", 100));
				if (c.isValid()) {
					if (c.getCommand().contains("err")) {
						Alert alert = new Alert(AlertType.ERROR, c.getProp().get(0), ButtonType.OK);
						alert.showAndWait();
					} else
						System.out.println("Rename command response: " + c.getCommand());
				}
			});
		}
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

				if (notes.getProp() != null)
					labelContent.setText(notes.getProp().get(0));
				else
					labelContent.setText("");
			} else if (notes.getCommand().contains("err")) {
				Alert alert = new Alert(AlertType.ERROR, notes.getProp().get(0), ButtonType.OK);
				alert.showAndWait();
			} else
				System.out
						.println("Err: Unexpected command: " + notes.getCommand() + ", while loading note " + notePath);
		}
	}

	public void populateDeviceInfo() {
		if (!dev.isOpen()) {
			dev.setBaudRate(921600);
			dev.openPort();
			dev.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
		}

		Command deviceInfo = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<getInfo>", 200));
		if (deviceInfo.isValid()) {
			if (deviceInfo.getCommand().contains("devInfo")) {
				labelDevName.setText(deviceInfo.getProp().get(0));
				labelDevID.setText(deviceInfo.getProp().get(1));

				devBufferSize = Integer.parseInt(deviceInfo.getProp().get(2));
				labelDevIBuffer.setText(deviceInfo.getProp().get(2) + " chars");

			} else if (deviceInfo.getCommand().contains("err")) {
				Alert alert = new Alert(AlertType.ERROR, deviceInfo.getProp().get(0), ButtonType.OK);
				alert.showAndWait();
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
				out += "Mb / ";
				out += df.format(b);
				out += "Mb";

				labelDevMemory.setText(out);

				double progress = usedBytes / totalBytes;

				progressBarMem.setProgress(progress);

			} else if (memInfo.getCommand().contains("err")) {
				Alert alert = new Alert(AlertType.ERROR, memInfo.getProp().get(0), ButtonType.OK);
				alert.showAndWait();
			} else
				System.out
						.println("Err: Unexpected command: " + memInfo.getCommand() + ", while loading deviceMemInfo");
		}
	}

	public void populateNoteList() {
		if (!dev.isOpen()) {
			dev.setBaudRate(921600);
			dev.openPort();
			dev.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
		}

		VBoxTitles.getChildren().clear();

		Command notes = Parser.parse(bs.sendAwaitToOpenedPort(dev, "<list>", 200));
		if (notes.isValid()) {
			if (notes.getCommand().contains("list")) {
				if (notes.getProp() != null) {
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
				}
			} else
				System.out.println("Err: Unexpected command: " + notes.getCommand() + ", while loading notes");
		}

	}

	public void setDevice(SerialPort dev) {
		this.dev = dev;
		SerialConnection.setSerialPort(dev);

		populateNoteList();
		populateDeviceInfo();
	}
}
