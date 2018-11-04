package controllers;

import java.io.IOException;

import com.fazecast.jSerialComm.SerialPort;

import application.Global;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mrParser.Command;
import mrParser.Parser;
import serial.*;

public class StartScreenController {
	
	private Stage primaryStage;
	private Scene scene;
	private Global g = new Global();
	private String startMessage;
	
	@FXML
	private VBox VBoxDevices;
	
    @FXML
    private Button buttonStartLooking;

    @FXML
    void onActionStartLooking() {
    	buttonStartLooking.setText("Looking for devices ...");
    	lookForDevices();
    }
    
    @FXML
    public void initialize()
    {
    	startMessage = buttonStartLooking.getText();
    	
    	//SerialConnection.setBaudRate(115200);
    }
    
    public void setPrimaryStage(Stage primaryStage)
    {
    	this.primaryStage = primaryStage;
    }
    
    public void setScene(Scene scene)
    {
    	this.scene = scene;
    }
        
    public void showHomeScreen(SerialPort sp)
    {
    	FXMLLoader ld = g.getResLoader("HomeScreen");
    	
    	try {
			BorderPane home = ld.load();
			HomeScreenController hsc = ld.getController();
			hsc.setDevice(sp);
			
			Scene s = new Scene(home);
			s.getStylesheets().add("/resources/css/global.css");
			
			primaryStage.setScene(s);
			primaryStage.sizeToScene();
			primaryStage.setMaximized(true);
			primaryStage.setResizable(true);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void lookForDevices()
    {
    	VBoxDevices.getChildren().clear();
    	
    	BlockingSerial bs = new BlockingSerial();
    	
    	SerialPort[] ports = SerialPort.getCommPorts();
    	for (SerialPort serialPort : ports) {
    		System.out.println(serialPort.getDescriptivePortName());
			String data = bs.sendAwait(serialPort, "<getInfo>", 100);
			//System.out.println("Recived: " + data);
			Command c = Parser.parse(data);
			
			if(c.isValid() && c.getCommand().contains("devInfo"))
			{
				FXMLLoader ld = g.getResLoader("DeviceNode");
				Button newB;
				try {
					newB = ld.load();
					
					DeviceNodeController dnc = ld.getController();
					dnc.setDevice(serialPort);
					dnc.setSCC(this);
					dnc.setLabel(c.getProp().get(0));
					
					VBoxDevices.getChildren().add(newB);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    	
    	buttonStartLooking.setText(startMessage);
    }

}
