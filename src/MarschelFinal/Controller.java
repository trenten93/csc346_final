package MarschelFinal;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.File;
import java.net.URI;
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
    TextArea output;

    @FXML
    Hyperlink mapLink;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapLink.setDisable(true);
        run.setDisable(true);
        output.setText("");
        try {
            MakeCrime.makeMap();
            mapLink.setDisable(false);
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


    public void linkClick(){ // for link
        try {
            Desktop desktop = Desktop.getDesktop();
            File mapFile = new File("outputMap.svg");
            desktop.open(mapFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
