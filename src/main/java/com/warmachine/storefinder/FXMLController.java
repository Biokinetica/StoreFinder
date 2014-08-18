package com.warmachine.storefinder;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    @FXML
    private Accordion Details;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
                address = new ServerAddress("ec2-54-82-163-131.compute-1.amazonaws.com",27017);
            } catch (UnknownHostException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        
        MongoCredential creds = MongoCredential.createMongoCRCredential("User", "project", "WarmaHordes".toCharArray());
            mongoClient = new MongoClient(address, Arrays.asList(creds));
            storeInfo = new BasicDBObject();
    }    

    @FXML
    private void handleSearch(MouseEvent event) throws IOException {
        
        DBCollection colls = mongoClient.getDB("project").getCollection("Stores");
        
        
        double coordinates[] = new double[2];
            
            final Geocoder geocoder = new Geocoder();
         GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(AddrLine.getText() + " " + CityLine.getText() + ", MI " + ZipLine.getText() ).setLanguage("en").getGeocoderRequest();
         GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

            coordinates[0] = geocoderResponse.getResults().get(0).getGeometry().getLocation().getLng().doubleValue();
            coordinates[1] = geocoderResponse.getResults().get(0).getGeometry().getLocation().getLat().doubleValue();
            
            storeInfo.put("loc",new BasicDBObject("$near", new BasicDBObject("$geometry", new BasicDBObject("type","Point").append("coordinates", coordinates))).append("$maxDistance", Integer.parseInt(KiloLine.getText()+"000")));

            Results.getPanes().clear();
        
            DBCursor cursor = colls.find(storeInfo);
                
        for(DBObject s : cursor){
        GridPane pane = new GridPane();
        GridPane detailPane = new GridPane();
        
        pane.addColumn(0, new Label("Address: "));
        pane.addRow(0, new Label(s.get("Address").toString()));
        
        pane.addColumn(0, new Label("City: "));
        pane.addRow(1, new Label(s.get("City").toString()));
        
        pane.addRow(2, new Label("Zip: "));
        pane.addRow(2, new Label(s.get("Zip").toString()));
        
        pane.addRow(3, new Label("Phone: "));
        pane.addRow(3, new Label(s.get("Phone").toString()));
        
        TitledPane t3 = new TitledPane(s.get("Store").toString(), pane );
        
        BasicDBList times = (BasicDBList) cursor.next().get("Monday");
        
        detailPane.addRow(0, new Label("Open: "));
        detailPane.addRow(0, new Label(times.get(0).toString()));
        
        detailPane.addRow(1, new Label("Close: "));
        detailPane.addRow(1, new Label(times.get(1).toString()));
        
        final TitledPane t2 = new TitledPane("Monday", detailPane );
        
        t3.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) { 
                        Details.getPanes().clear();
                        Details.getPanes().add(t2);
                        };
                    });
        
        Results.getPanes().add(t3);
        }
        
    }
}
