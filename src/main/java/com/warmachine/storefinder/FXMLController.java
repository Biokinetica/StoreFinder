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
import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javax.swing.JOptionPane;

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
            Component frame = null;
            JOptionPane.showMessageDialog(frame,
            ex.getMessage(),
            "Can't connect to database",
            JOptionPane.ERROR_MESSAGE);
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
    
    private static void open(URI uri){
    if (Desktop.isDesktopSupported()) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException ex) {
            Component frame = null;
            JOptionPane.showMessageDialog(frame,
    "Unsupported on your platform.",
    "Browse error",
    JOptionPane.ERROR_MESSAGE);
        }
    }
  }
    
    @FXML
    private void handleSearch(MouseEvent event) throws IOException {
        
        
        Hours.getPanes().clear();
        
        if(CityLine.getLength() == 0 && ZipLine.getLength() == 0)
        {
            Component frame = null;
            JOptionPane.showMessageDialog(frame,
    "Fill in either the City or Zip fields for results.",
    "Error: Empty Location",
    JOptionPane.ERROR_MESSAGE);
        }
        
        DBCollection colls = mongoClient.getDB("warmachine1").getCollection("Stores");
        
        
        final double coordinates[] = new double[2];
            
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
        
        for(final DBObject s : cursor){
        GridPane pane = new GridPane();
        GridPane hoursPane = new GridPane();
        
        pane.addColumn(0, new Label("Address: "));
        pane.addRow(0, new Label(s.get("Address").toString()));
        
        pane.addColumn(0, new Label("City: "));
        pane.addRow(1, new Label(s.get("City").toString()));
        
        pane.addRow(2, new Label("Zip: "));
        pane.addRow(2, new Label(s.get("Zip").toString()));
        
        pane.addRow(3, new Label("Phone: "));
        pane.addRow(3, new Label(s.get("Phone").toString()));
        pane.addRow(4, new Label("Open Play:"));
        pane.addRow(6, new Label("Press Gangers: "));
        
         BasicDBObject loc = (BasicDBObject) s.get("loc");
         BasicDBList coords = (BasicDBList) loc.get("coordinates");
        String url = "http://maps.googleapis.com/maps/api/staticmap?center=" + URLEncoder.encode(s.get("Address").toString(),"UTF-8") + "," 
                + URLEncoder.encode(s.get("City").toString(),"UTF-8")
                + ",MI&zoom=14&size=300x200&maptype=roadmap&markers=color:red%7Clabel:A%7C"
                + coords.get(1) + "," + coords.get(0);
                
       
        ImageView iv = new ImageView();
        iv.setImage(new Image(url));
        
        Hyperlink directions = new Hyperlink(null,iv);
        
        pane.addRow(5,new Label("Map: "));
        pane.add(directions,1,5);
        
        if(s.containsField("OP")){
            pane.addRow(4, new Label(s.get("OP").toString()));
        }
        
        directions.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event){
                try {
                    open(new URI("www.google.com/maps/dir/" + URLEncoder.encode(AddrLine.getText(),"UTF-8") + ","
                            + URLEncoder.encode(CityLine.getText(),"UTF-8") + ",MI/" + URLEncoder.encode(s.get("Address").toString(),"UTF-8")
                            + "," + URLEncoder.encode(s.get("City").toString(),"UTF-8") + ",MI/@" + coordinates[1] + "," + coordinates[0] + ",12z"));
                } catch (UnsupportedEncodingException | URISyntaxException ex) {
                    Component frame = null;
                    JOptionPane.showMessageDialog(frame,
                    ex.getMessage(),
                    "Encoding or Syntax Error",
                    JOptionPane.ERROR_MESSAGE);
                }
            }
            
        });
        if(s.containsField("PG")){
        BasicDBList PGs = (BasicDBList) s.get("PG");
        int index = 0;
        
        if(PGs.size() == 1)
            pane.addColumn(1, new Label(PGs.get(index).toString()) );
        else
            while(index != PGs.size()){
        pane.addColumn(1,new Label(PGs.get(index).toString()));
        ++index;
            }
        }
        
        TitledPane t = new TitledPane(s.get("Store").toString(), pane );

        DBObject res = cursor.next();
        
        BasicDBList times = (BasicDBList) res.get(ld.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US));
        
        hoursPane.addRow(0, new Label("Open: "));
        hoursPane.addRow(0, new Label(times.get(0).toString()));
        
        hoursPane.addRow(1, new Label("Close: "));
        hoursPane.addRow(1, new Label(times.get(1).toString()));
        
        final TitledPane t1 = new TitledPane(ld.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US), hoursPane );
        
        GridPane hoursPane1 = new GridPane();
        BasicDBList times1 = (BasicDBList) res.get(ld.minusDays(1).format(DateTimeFormatter.ofPattern("EEEE")));
        hoursPane1.addRow(0, new Label("Open: "));
        hoursPane1.addRow(0, new Label(times1.get(0).toString()));
        
        hoursPane1.addRow(1, new Label("Close: "));
        hoursPane1.addRow(1, new Label(times1.get(1).toString()));
        
        final TitledPane t2 = new TitledPane(ld.minusDays(1).format(DateTimeFormatter.ofPattern("EEEE")), hoursPane1);
        
        GridPane hoursPane2 = new GridPane();
        BasicDBList times2 = (BasicDBList) res.get(ld.minusDays(2).format(DateTimeFormatter.ofPattern("EEEE")));
        hoursPane2.addRow(0, new Label("Open: "));
        hoursPane2.addRow(0, new Label(times2.get(0).toString()));
        
        hoursPane2.addRow(1, new Label("Close: "));
        hoursPane2.addRow(1, new Label(times2.get(1).toString()));
        
        final TitledPane t3 = new TitledPane(ld.minusDays(2).format(DateTimeFormatter.ofPattern("EEEE")), hoursPane2);
        
        GridPane hoursPane3 = new GridPane();
        BasicDBList times3 = (BasicDBList) res.get(ld.minusDays(3).format(DateTimeFormatter.ofPattern("EEEE")));
        hoursPane3.addRow(0, new Label("Open: "));
        hoursPane3.addRow(0, new Label(times3.get(0).toString()));
        
        hoursPane3.addRow(1, new Label("Close: "));
        hoursPane3.addRow(1, new Label(times3.get(1).toString()));
        
        final TitledPane t4 = new TitledPane(ld.minusDays(3).format(DateTimeFormatter.ofPattern("EEEE")), hoursPane3);
        
        GridPane hoursPane4 = new GridPane();
        BasicDBList times4 = (BasicDBList) res.get(ld.minusDays(4).format(DateTimeFormatter.ofPattern("EEEE")));
        hoursPane4.addRow(0, new Label("Open: "));
        hoursPane4.addRow(0, new Label(times4.get(0).toString()));
        
        hoursPane4.addRow(1, new Label("Close: "));
        hoursPane4.addRow(1, new Label(times4.get(1).toString()));
        
        final TitledPane t5 = new TitledPane(ld.minusDays(4).format(DateTimeFormatter.ofPattern("EEEE")), hoursPane4);
        
        GridPane hoursPane5 = new GridPane();
        BasicDBList times5 = (BasicDBList) res.get(ld.minusDays(5).format(DateTimeFormatter.ofPattern("EEEE")));
        hoursPane5.addRow(0, new Label("Open: "));
        hoursPane5.addRow(0, new Label(times5.get(0).toString()));
        
        hoursPane5.addRow(1, new Label("Close: "));
        hoursPane5.addRow(1, new Label(times5.get(1).toString()));
        
        final TitledPane t6 = new TitledPane(ld.minusDays(5).format(DateTimeFormatter.ofPattern("EEEE")), hoursPane5);
        
        GridPane hoursPane6 = new GridPane();
        BasicDBList times6 = (BasicDBList) res.get(ld.minusDays(6).format(DateTimeFormatter.ofPattern("EEEE")));
        hoursPane6.addRow(0, new Label("Open: "));
        hoursPane6.addRow(0, new Label(times6.get(0).toString()));
        
        hoursPane6.addRow(1, new Label("Close: "));
        hoursPane6.addRow(1, new Label(times6.get(1).toString()));
        
        final TitledPane t7 = new TitledPane(ld.minusDays(6).format(DateTimeFormatter.ofPattern("EEEE")), hoursPane6);
        
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
