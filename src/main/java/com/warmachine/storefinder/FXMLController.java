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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private Accordion Hours;
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
                address = new ServerAddress("ds049858.mongolab.com",49858);
            } catch (UnknownHostException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        
        MongoCredential creds = MongoCredential.createMongoCRCredential("User", "warmachine1", "WarmaHordes".toCharArray());
            mongoClient = new MongoClient(address, Arrays.asList(creds));
            storeInfo = new BasicDBObject();
    }    
    
    @FXML
    private void CityFieldEnter(KeyEvent event) throws IOException {
        MouseEvent m = null;
        if(event.getCode() == KeyCode.ENTER)
            handleSearch(m);
    }
    
    @FXML
    private void ZipFieldEnter(KeyEvent event) throws IOException {
        MouseEvent m = null;
        if(event.getCode() == KeyCode.ENTER)
            handleSearch(m);
    }
    
    @FXML
    private void KiloFieldEnter(KeyEvent event) throws IOException {
        MouseEvent m = null;
        if(event.getCode() == KeyCode.ENTER)
            handleSearch(m);
    }
    
    @FXML
    private void handleSearch(MouseEvent event) throws IOException {
        
        Hours.getPanes().clear();
        
        DBCollection colls = mongoClient.getDB("warmachine1").getCollection("Stores");
        
        
        double coordinates[] = new double[2];
            
            final Geocoder geocoder = new Geocoder();
            GeocoderRequest geocoderRequest;
            
        if(AddrLine.getLength() == 0 && CityLine.getLength() == 0)
           geocoderRequest = new GeocoderRequestBuilder().setAddress(ZipLine.getText() + ", MI ").setLanguage("en").getGeocoderRequest();
        else if(AddrLine.getLength() == 0)
            geocoderRequest = new GeocoderRequestBuilder().setAddress(CityLine.getText() + ", MI ").setLanguage("en").getGeocoderRequest();
        else
            geocoderRequest = new GeocoderRequestBuilder().setAddress(AddrLine.getText() + " " + CityLine.getText() + ", MI " + ZipLine.getText() ).setLanguage("en").getGeocoderRequest();
            
         GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

            coordinates[0] = geocoderResponse.getResults().get(0).getGeometry().getLocation().getLng().doubleValue();
            coordinates[1] = geocoderResponse.getResults().get(0).getGeometry().getLocation().getLat().doubleValue();
            
            storeInfo.put("loc",new BasicDBObject("$near", new BasicDBObject("$geometry", new BasicDBObject("type","Point").append("coordinates", coordinates))).append("$maxDistance", Integer.parseInt(KiloLine.getText()+"000")));

            Results.getPanes().clear();
        
            DBCursor cursor = colls.find(storeInfo);
            
            LocalDate ld = LocalDate.now();
        
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
        pane.addRow(4, new Label("Open Play: "));
        pane.addRow(5, new Label("Press Gangers: "));
        
        if(s.containsField("OP")){
            pane.addRow(4, new Label(s.get("OP").toString()));
        }
        
        if(s.containsField("PG")){
        BasicDBList PGs = (BasicDBList) s.get("PG");
        int index = 0;
        
        if(PGs.size() == 1)
            pane.addRow(5, new Label(PGs.get(index).toString()) );
        else
            while(index != PGs.size()){
        pane.addColumn(1,new Label(PGs.get(index).toString()));
        ++index;
            }
        }
        TitledPane t = new TitledPane(s.get("Store").toString(), pane );

        DBObject res = cursor.next();
        
        BasicDBList times = (BasicDBList) res.get(ld.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US));
        
        detailPane.addRow(0, new Label("Open: "));
        detailPane.addRow(0, new Label(times.get(0).toString()));
        
        detailPane.addRow(1, new Label("Close: "));
        detailPane.addRow(1, new Label(times.get(1).toString()));
        
        final TitledPane t1 = new TitledPane(ld.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US), detailPane );
        
        GridPane detailPane1 = new GridPane();
        BasicDBList times1 = (BasicDBList) res.get(ld.minusDays(1).format(DateTimeFormatter.ofPattern("EEEE")));
        detailPane1.addRow(0, new Label("Open: "));
        detailPane1.addRow(0, new Label(times1.get(0).toString()));
        
        detailPane1.addRow(1, new Label("Close: "));
        detailPane1.addRow(1, new Label(times1.get(1).toString()));
        
        final TitledPane t2 = new TitledPane(ld.minusDays(1).format(DateTimeFormatter.ofPattern("EEEE")), detailPane1);
        
        GridPane detailPane2 = new GridPane();
        BasicDBList times2 = (BasicDBList) res.get(ld.minusDays(2).format(DateTimeFormatter.ofPattern("EEEE")));
        detailPane2.addRow(0, new Label("Open: "));
        detailPane2.addRow(0, new Label(times2.get(0).toString()));
        
        detailPane2.addRow(1, new Label("Close: "));
        detailPane2.addRow(1, new Label(times2.get(1).toString()));
        
        final TitledPane t3 = new TitledPane(ld.minusDays(2).format(DateTimeFormatter.ofPattern("EEEE")), detailPane2);
        
        GridPane detailPane3 = new GridPane();
        BasicDBList times3 = (BasicDBList) res.get(ld.minusDays(2).format(DateTimeFormatter.ofPattern("EEEE")));
        detailPane3.addRow(0, new Label("Open: "));
        detailPane3.addRow(0, new Label(times3.get(0).toString()));
        
        detailPane3.addRow(1, new Label("Close: "));
        detailPane3.addRow(1, new Label(times3.get(1).toString()));
        
        final TitledPane t4 = new TitledPane(ld.minusDays(3).format(DateTimeFormatter.ofPattern("EEEE")), detailPane3);
        
        GridPane detailPane4 = new GridPane();
        BasicDBList times4 = (BasicDBList) res.get(ld.minusDays(4).format(DateTimeFormatter.ofPattern("EEEE")));
        detailPane4.addRow(0, new Label("Open: "));
        detailPane4.addRow(0, new Label(times4.get(0).toString()));
        
        detailPane4.addRow(1, new Label("Close: "));
        detailPane4.addRow(1, new Label(times4.get(1).toString()));
        
        final TitledPane t5 = new TitledPane(ld.minusDays(4).format(DateTimeFormatter.ofPattern("EEEE")), detailPane4);
        
        GridPane detailPane5 = new GridPane();
        BasicDBList times5 = (BasicDBList) res.get(ld.minusDays(5).format(DateTimeFormatter.ofPattern("EEEE")));
        detailPane5.addRow(0, new Label("Open: "));
        detailPane5.addRow(0, new Label(times5.get(0).toString()));
        
        detailPane5.addRow(1, new Label("Close: "));
        detailPane5.addRow(1, new Label(times5.get(1).toString()));
        
        final TitledPane t6 = new TitledPane(ld.minusDays(5).format(DateTimeFormatter.ofPattern("EEEE")), detailPane5);
        
        GridPane detailPane6 = new GridPane();
        BasicDBList times6 = (BasicDBList) res.get(ld.minusDays(6).format(DateTimeFormatter.ofPattern("EEEE")));
        detailPane6.addRow(0, new Label("Open: "));
        detailPane6.addRow(0, new Label(times6.get(0).toString()));
        
        detailPane6.addRow(1, new Label("Close: "));
        detailPane6.addRow(1, new Label(times6.get(1).toString()));
        
        final TitledPane t7 = new TitledPane(ld.minusDays(6).format(DateTimeFormatter.ofPattern("EEEE")), detailPane6);
        
        t.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) { 
                        Hours.getPanes().clear();
                        Hours.getPanes().addAll(t1,t2,t3,t4,t5,t6,t7);
                        };
                    });
        
        Results.getPanes().add(t);
        }
        
    }

    
}
