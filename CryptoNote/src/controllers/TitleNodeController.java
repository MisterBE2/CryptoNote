package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TitleNodeController {

	HomeScreenController hsc;
	
    @FXML
    private Label labelTitle;

    @FXML
    void onMouseClick() {
    	hsc.openNote(labelTitle.getText());
    }
    
    public void setName(String name)
    {
    	labelTitle.setText(name);
    }

    public void setHSC(HomeScreenController hsc)
    {
    	this.hsc = hsc;
    }
}
