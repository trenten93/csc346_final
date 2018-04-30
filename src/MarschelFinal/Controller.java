package MarschelFinal;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    @FXML
    Pane mainPane;

    @FXML
    TextField zipEnter;

    @FXML
    Button run;

    @FXML
    Button generateMap;

    @FXML
    TextArea output;

    @FXML
    Hyperlink mapLink;

    @FXML
    Hyperlink propertyCrimeMap;

    @FXML
    public ProgressBar progressBar = ProgressTest.getProgressBar();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapLink.setDisable(true);
        propertyCrimeMap.setDisable(true);
        run.setDisable(true);
        output.setText("");
        try {
            MakeCrime.makeMap(1);
            mapLink.setDisable(false);
            propertyCrimeMap.setDisable(false);
            System.out.println("controller: "+ProgressTest.getProgress());
            progressBar.setProgress(ProgressTest.getProgress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zipKeyReleased(){
        if(zipEnter.getText().length() == 5){
            run.setDisable(false);
        }else{
            run.setDisable(true);
        }
    }

    public void zipFieldEnter(){
        if(zipFieldValid()){
            mainRun();
        }
    }

    public boolean zipFieldValid(){
        if(zipEnter.getText().length() ==5){
            return true;
        }else{
            return false;
        }
    }

    public void mainRun(){
        CountyCrimeData data = new CountyCrimeData();
        data = MakeCrime.getCrimeDataForOneCounty(zipEnter.getText());

        if(data.getCountyName().equalsIgnoreCase("null")){
            output.setText("there was no data for this county!");
        }else{
            output.setText("State: "+data.getStateFull()+"\nCounty: "+data.getCountyName()+"\n"
            +"Violent Crime: "+data.getViolentCrime()+"\nProperty Crime: "+data.getPropertyCrime());
        }

    }


    public void linkClick(){ // for mainCrime violent crime link
        try {
            Desktop desktop = Desktop.getDesktop();
            File mapFile = new File("violentCrimeMap.svg");
            desktop.open(mapFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void propertyCrimeMapLinkClick(){// for property crime link
        try {
            Desktop desktop = Desktop.getDesktop();
            File mapFile = new File("propertyCrimeMap.svg");
            desktop.open(mapFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void generateMapButton(){
        generateMap.setDisable(true);
        run.setDisable(true);
        mapLink.setDisable(true);
        propertyCrimeMap.setDisable(true);
        ProgressTest.progress = 0.0;
        progressBar.setProgress(0.0);
        try {
            MakeCrime.makeViolentMap();
            progressBar.setProgress(0.5);
            MakeCrime.makePropertyMap();
            progressBar.setProgress(1.0);

            generateMap.setDisable(false);
            run.setDisable(false);
            mapLink.setDisable(false);
            propertyCrimeMap.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateProgress(){
        try {
            Robot robot = new Robot();
            robot.delay(500);
            progressBar.setProgress(ProgressTest.getProgress());
        } catch (AWTException e) {
            e.printStackTrace();
        }


    }



}
