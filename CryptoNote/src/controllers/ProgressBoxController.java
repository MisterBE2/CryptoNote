package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class ProgressBoxController {

	private Stage thisStage;
	
    @FXML
    private Label labelData;

    @FXML
    private ProgressBar progressBar;
    
    public void setData(String data)
    {
    	labelData.setText(data);
    }
    
    public void setProgress(double val)
    {
    	progressBar.setProgress(val);
    }
    
    public void setStage(Stage s)
    {
    	this.thisStage = s;
    }
    
    public void close()
    {
    	thisStage.close();
    }
    
    public void show()
    {
    	thisStage.show();
    }

}
