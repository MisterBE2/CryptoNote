package controllers;

import com.fazecast.jSerialComm.SerialPort;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DeviceNodeController {

	private SerialPort dev;
	private StartScreenController scc;
	
    @FXML
    private Button buttonDevice;

    @FXML
    void onActionDevice() {
    	scc.showHomeScreen(dev, buttonDevice.getText());
    }

    public void setDevice(SerialPort dev)
    {
    	this.dev = dev;
    	buttonDevice.setText(dev.getDescriptivePortName());
    }
    
    public void setSCC(StartScreenController scc)
    {
    	this.scc = scc;
    }
    
    public void setLabel(String name)
    {
    	buttonDevice.setText(name);
    }
    
}
