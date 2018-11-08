package application;
import controllers.StartScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import serial.*;

public class Main extends Application {

	private Global g = new Global();

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = g.getResLoader("StartScreen");
			AnchorPane root = loader.load();

			StartScreenController ssc = loader.getController();
			Scene scene = new Scene(root);
			scene.getStylesheets().add("/resources/css/global.css");

			ssc.setPrimaryStage(primaryStage);
			ssc.setScene(scene);

			primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/resources/img/icon.png")));
			primaryStage.setTitle("CryptoNote");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void stop() {
		if(SerialConnection.getSerialPort() != null)
		{
			BlockingSerial bs = new BlockingSerial();
			bs.sendAwaitToOpenedPort(SerialConnection.getSerialPort(), "<lock>", 100);
		}
		SerialConnection.closePort();
	}
}
