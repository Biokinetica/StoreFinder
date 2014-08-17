package com.warmachine.storefinder;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javax.management.Query;

public class FXMLController implements Initializable {
    
    @FXML
    private Font x1;
    @FXML
    private Font x3;
    @FXML
    private Color x4;
    @FXML
    private TextField AddrLine;
    @FXML
    private TextField KiloLine;
    @FXML
    private Label SearchLabel;
    @FXML
    private Label kLabel;
    @FXML
    private TextField CityLine;
    @FXML
    private TextField ZipLine;
    @FXML
    private AnchorPane ScrollAnchor;
    @FXML
    private Button SearchButton;
    
    private ServerAddress address;
    
    private MongoClient mongoClient;
    @FXML
    private Accordion Results;
    
    private BasicDBObject storeInfo;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
                address = new ServerAddress("ec2-54-82-163-131.compute-1.amazonaws.com",27017);
            } catch (UnknownHostException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        
        MongoCredential creds = MongoCredential.createMongoCRCredential("User", "project", "WarmaHordes".toCharArray());
            mongoClient = new MongoClient(address, Arrays.asList(creds));
    }    

    @FXML
    private void handleSearch(MouseEvent event) {
        
        DBCollection colls = mongoClient.getDB("project").getCollection("Stores");
        /*
        storeInfo.append("Address", AddrLine.getText())
                .append("City", CityLine.getText())
                .append("Zip", ZipLine.getText());
        */
        
        Map stores = colls.findOne().toMap();
        
        GridPane pane = new GridPane();
        pane.addColumn(0, new Label("Address: "));
        pane.addRow(0, new Label(stores.get("Address").toString()));
        
        pane.addColumn(0, new Label("City: "));
        pane.addRow(1, new Label(stores.get("City").toString()));
        
        pane.addRow(2, new Label("Zip: "));
        pane.addRow(2, new Label(stores.get("Zip").toString()));
        
        pane.addRow(3, new Label("Phone: "));
        pane.addRow(3, new Label(stores.get("Phone").toString()));
        
 TitledPane t3 = new TitledPane(colls.findOne().toMap().get("Store").toString(), pane );
        Results.getPanes().add(t3);
        
    }
}
