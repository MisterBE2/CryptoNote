package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NoteEditScreenController {

	HomeScreenController hsc;
	
    @FXML
    private TextField textFieldTitle;

    @FXML
    private TextArea textFieldContent;

    @FXML
    void onActionCancel() {

    }

    @FXML
    void onActionSave() {

    }
    
    public void setHSC(HomeScreenController hsc)
    {
    	this.hsc = hsc;
    }

}
