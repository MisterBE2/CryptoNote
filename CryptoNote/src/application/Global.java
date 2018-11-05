package application;

import javafx.fxml.FXMLLoader;

public class Global {

	// Zwraca FXMLLoadera powi¹zanego z folderem fxml
	public FXMLLoader getResLoader(String fxmlName)
	{
		return new FXMLLoader(this.getClass().getResource("/resources/fxml/"+fxmlName+".fxml"));
	}
	
}
