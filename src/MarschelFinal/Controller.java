package MarschelFinal;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

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
        }
    }
    





}
