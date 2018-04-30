package MarschelFinal;


// This program uses totals from COUNTY offices ONLY to produce a map in an xml format that shows the rate of crime
// per 1000 people and shades in the counties in the map from lighter ,less crime to brighter red for more crime per 1000
// people. It also allows the user to enter a zip code to get the results for their county.

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
        primaryStage.setScene(new Scene(root, 590, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}
