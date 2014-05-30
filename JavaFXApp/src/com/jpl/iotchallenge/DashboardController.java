package com.jpl.iotchallenge;

import com.jpl.iotchallenge.device.DInterface;
import com.jpl.iotchallenge.services.APIServices;
import com.jpl.iotchallenge.user.LhingsUser;
import com.jpl.iotchallenge.user.Login;
import com.jpl.iotchallenge.utils.Utils;
import eu.hansolo.enzo.common.SymbolType;
import eu.hansolo.enzo.onoffswitch.IconSwitch;
import eu.hansolo.enzo.onoffswitch.SelectionEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class DashboardController implements Initializable {

    @FXML private Label lblUser;
    @FXML private ImageView imgAvatar;
    @FXML private ImageView imgNet;
    @FXML private Label lblStatus;
    @FXML private Tab tabDevices;
    
    @FXML private SVGPath svgTable;
    @FXML private Text lblChecked;
    @FXML private Label lblTimeTable;
    @FXML private HBox hTable;
    private IconSwitch onOffTable;
    
    @FXML private SVGPath svgAvailable;
    @FXML private Text lblAvailable;
    @FXML private Label lblTimeAvailable;
    @FXML private HBox hAvailable;
    private IconSwitch onOffAvailable;
    
    @FXML private SVGPath svgTaxi;
    @FXML private Text lblTaxi;
    @FXML private Label lblTimeTaxi;
    @FXML private HBox hTaxi;
    private IconSwitch onOffTaxi;
    
    @FXML private SVGPath svgLamp;
    @FXML private Text lblLamp;
    @FXML private Label lblTimeLamp;
    @FXML private HBox hLight;
    private IconSwitch onOffLight;
    
    private final Image imgNetOn = new Image(getClass().getResourceAsStream("resources/netOn.png"));
    private final Image imgNetOff = new Image(getClass().getResourceAsStream("resources/netOff.png"));
    private FadeTransition messageTransition = null;
    private final DateTimeFormatter fmd =DateTimeFormatter.ofPattern("d'th of' MMMM").withLocale(Locale.ENGLISH);
    private final DateTimeFormatter fmt =DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());;
    
    private final StringProperty uuidTable=new SimpleStringProperty("");
    
    private DInterface devInterface;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblStatus.setText("Validating user...");
        Label label = new Label("\uf10b");
        label.getStyleClass().add("awesome");
        reset();
        lblTimeTable.setText("");
        lblChecked.setText("");
        
        onOffTable = new IconSwitch();
        onOffTable.setSymbolType(SymbolType.NONE);
        onOffTable.setMinSize(80,30);
        hTable.getChildren().add(onOffTable);
        hTable.setDisable(true);
        
        onOffAvailable = new IconSwitch();
        onOffAvailable.setSymbolType(SymbolType.NONE);
        onOffAvailable.setMinSize(80,30);
        hAvailable.getChildren().add(onOffAvailable);
        hAvailable.setDisable(true);
        
        onOffTaxi = new IconSwitch();
        onOffTaxi.setSymbolType(SymbolType.NONE);
        onOffTaxi.setMinSize(80,30);
        hTaxi.getChildren().add(onOffTaxi);
        hTaxi.setDisable(true);
        
        onOffLight = new IconSwitch();
        onOffLight.setSymbolType(SymbolType.NONE);
        onOffLight.setMinSize(80,30);
        hLight.getChildren().add(onOffLight);
        hLight.setDisable(true);
        
        tabDevices.setGraphic(label);
        tabDevices.setDisable(true);
    }   
    
    private void reset(){
        svgTable.setContent(Utils.TABLE_OFF);
        svgTable.setId("status-gray");
        svgAvailable.setId("status-gray");
        svgTaxi.setId("status-gray");
        lblAvailable.setText("");
        lblTimeAvailable.setText("");
        lblTaxi.setText("");
        lblTimeTaxi.setText("");
        
        svgLamp.setId("status-gray");
        lblLamp.setText("");
        lblTimeLamp.setText("");
    }
    
    public void login(Scene scene){
        scene.setCursor(Cursor.WAIT);
        Login login=new Login();
        login.loggedProperty().addListener((ov,b,b1)->{
            if(b1){
                String url=LhingsUser.getInstance().getAvatar();
                if(url!=null && !url.isEmpty()){
                    url=url.replace("\\", "");
                    imgAvatar.setImage(new Image(url));
                }
                lblUser.setText(String.join(" ",LhingsUser.getInstance().getName(), LhingsUser.getInstance().getSurname1()));
                imgNet.setImage(imgNetOn);
                tabDevices.setDisable(false);
                createDevice();
                displayMessage("User validated");
            } else {
                displayMessage("User not validated");
                logout();
            }
            scene.setCursor(Cursor.DEFAULT);
        });
        if(LhingsUser.getInstance().getUser().isEmpty() || LhingsUser.getInstance().getPass().isEmpty()){
            login.showLoginDialog();
        } else {
            login.validateUser();
        }
    }
    
    public void logout(){
        imgAvatar.setImage(new Image(getClass().getResource("resources/user.png").toExternalForm()));
        lblUser.setText("No user");
        imgNet.setImage(imgNetOff);
    }
    private final EventHandler<SelectionEvent> c=e -> {
        Platform.runLater(()->APIServices.doDeviceAction(LhingsUser.getInstance().getUuidTable(), "toggleCheckin"));
    };
    private void switchTable(boolean status){
        onOffTable.setOnSelect(null);
        onOffTable.setOnDeselect(null);
        onOffTable.setSelected(status);
        onOffTable.setOnSelect(c);
        onOffTable.setOnDeselect(c);
    }
    private final EventHandler<SelectionEvent> c1=e -> {
        Platform.runLater(()->APIServices.doDeviceAction(LhingsUser.getInstance().getUuidTable(), "toggleLight"));
    };
    private void switchLight(boolean status){
        onOffLight.setOnSelect(null);
        onOffLight.setOnDeselect(null);
        onOffLight.setSelected(status);
        onOffLight.setOnSelect(c1);
        onOffLight.setOnDeselect(c1);
    }
    private final EventHandler<SelectionEvent> c2=e -> {
        Platform.runLater(()->APIServices.doDeviceAction(LhingsUser.getInstance().getUuidTable(), "toggleAvailable"));
    };
    private void switchAvailable(boolean status){
        onOffAvailable.setOnSelect(null);
        onOffAvailable.setOnDeselect(null);
        onOffAvailable.setSelected(status);
        onOffAvailable.setOnSelect(c2);
        onOffAvailable.setOnDeselect(c2);
    }
    private String getDateTime(){
        return LocalDate.now().format(fmd).concat(" at ").concat(LocalTime.now().format(fmt));
    }
    private void createDevice(){
        devInterface=new DInterface();
        hTable.disableProperty().bind(devInterface.checkedInProperty().not());
        hAvailable.disableProperty().bind(devInterface.checkedInProperty().not());
        hTaxi.disableProperty().bind(devInterface.checkedInProperty().not());
        hLight.disableProperty().bind(devInterface.checkedInProperty().not());
        
        /*
        TABLE
        */
        uuidTable.addListener((ov,s,s1)->{
            if(s1.isEmpty()){
                System.out.println("**DISABLE TABLE");
                switchTable(false);
            } else {
                System.out.println("**ENABLE TABLE: "+s1);
                Platform.runLater(()->{
                    LhingsUser.getInstance().setUuidTable(s1);

                    svgTable.setContent(Utils.TABLE_ON);
                    svgTable.setId("status-green");
                    lblChecked.setText("You have checked in this CoWorking Table Device");
                    lblTimeTable.setText(getDateTime());
                    switchTable(true);
                });
            }
        });
        devInterface.checkedInProperty().addListener((ov,b,b1)->{
            if(b1){
                System.out.println("Retrieving Table from Coworking");
                uuidTable.set(APIServices.getLhingsDeviceByName(LhingsUser.getInstance().getApiKeyCoworking(),"Table"));
            } else {
                svgTable.setContent(Utils.TABLE_OFF);
                svgTable.setId("status-off");
                lblChecked.setText("You have checked out this CoWorking Table Device");
                lblTimeTable.setText(getDateTime());
                
                svgAvailable.setId("status-gray");
                lblAvailable.setText("");
                lblTimeAvailable.setText("");
                svgTaxi.setId("status-gray");
                lblTaxi.setText("");
                lblTimeTaxi.setText("");
                svgLamp.setId("status-gray");
                lblLamp.setText("");
                lblTimeLamp.setText("");
                uuidTable.set("");
            }
        });
        devInterface.notifyProperty().addListener((ov,s,s1)->{
            if(!s1.isEmpty()){
                devInterface.resetActions();
            }
        });
        devInterface.lightProperty().addListener((ov,b,b1)->{
            if(b1){
                svgLamp.setId("status-green");
                lblLamp.setText("You have turned on the Lamp in this CoWorking Table Device");
                lblTimeLamp.setText(getDateTime());
                switchLight(true);
            } else if(devInterface.checkedInProperty().get()){
                svgLamp.setId("status-off");
                lblLamp.setText("You have turned off the Lamp in this CoWorking Table Device");
                lblTimeLamp.setText(getDateTime());
                switchLight(false);
            }
        });
        devInterface.availabilityProperty().addListener((ov,b,b1)->{
            if(b1){
                svgAvailable.setContent(Utils.AVAILABLE);
                svgAvailable.setId("status-green");
                lblAvailable.setText("You have selected the Available mode at this CoWorking Table Device");
                lblTimeAvailable.setText(getDateTime());    
                switchAvailable(false);
            } else if(devInterface.checkedInProperty().get()){
                svgAvailable.setContent(Utils.NOT_AVAILABLE);
                svgAvailable.setId("status-red");
                lblAvailable.setText("You have selected the Not Available mode at this CoWorking Table Device");
                lblTimeAvailable.setText(getDateTime());                
                switchTable(true);
            }
        });
    }
    
    public void devLogout(){
        if(devInterface!=null){
            devInterface.logout();
        }
    }
    
    public void displayMessage(String message) {
        if (messageTransition != null) {
            messageTransition.stop();
        } else {
            messageTransition = new FadeTransition(Duration.millis(2000), lblStatus);
            messageTransition.setFromValue(1.0);
            messageTransition.setToValue(0.0);
            messageTransition.setDelay(Duration.millis(1000));
            messageTransition.setOnFinished(e -> lblStatus.setVisible(false));
        }
        lblStatus.setText(message);
        lblStatus.setVisible(true);
        lblStatus.setOpacity(1.0);
        messageTransition.playFromStart();
    }
}
