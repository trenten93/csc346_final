package MarschelFinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.*;
import java.io.*;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("crime.fxml"));
        primaryStage.setTitle("crime Data");
        primaryStage.setScene(new Scene(root, 550, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}
