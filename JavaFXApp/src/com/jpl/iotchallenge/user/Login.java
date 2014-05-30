package com.jpl.iotchallenge.user;

import com.jpl.iotchallenge.services.APIServices;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class Login {

    private Service<Void> loginService=null;
    private final BooleanProperty logged = new SimpleBooleanProperty(false);
    private final TextField txUserName = new TextField();
    private final PasswordField txPassword = new PasswordField();
    private final Label lblWrong=new Label("Wrong user or password");
    
    private final Action actionLogin = new AbstractAction("Login") {
        {  
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE); 
        }

        @Override
        public void execute(ActionEvent ae) {
            Dialog dlg = (Dialog) ae.getSource();
            LhingsUser.getInstance().setUser(txUserName.getText().trim());
            LhingsUser.getInstance().setPass(txPassword.getText().trim());
            logged.addListener((ov,b,b1)->{
                if(b1){
                    dlg.hide();
                } else {
                    
                }
            });
            validateUser();
        }
    };
    
//    private static final String[][] rulesTable={{"Table Checked In","CheckedIn","checkedIn"},
//                                            {"Table Checked Out","CheckedOut","checkedOut"},
//                                            {"Table Taxi Requested","TaxiRequested","taxiRequested"},
//                                            {"Table Taxi Requested Success","TaxiRequestedSuccess","taxiRequestedSuccess"},
//                                            {"Table Taxi Requested Failure","TaxiRequestedFailure","taxiRequestedFailure"},
//                                            {"Table Table Available","Available","available"},
//                                            {"Table Not Available","NotAvailable","notAvailable"}};
//    private static final String[][] rulesLamp={{"Lamp Turned On","TurnedOn","turnedOn"},
//                                            {"Lamp Turned Off","TurnedOff","turnedOff"}};
//    private static final String[][] rulesInterfaz={{"Turn On Lamp","turnOn","TurnOn"},
//                                                   {"Turn Off Lamp","turnOff","TurnOff"},
//                                                   {"Set Lamp Intensity","setIntensity","SetIntensity"}};
    
    public Login(){
        logged.set(false);
        txUserName.setText(LhingsUser.getInstance().getUser());
        txPassword.setText(LhingsUser.getInstance().getPass());
        lblWrong.setStyle("-fx-text-fill: red;");
        loginService=new Service<Void>(){

            @Override
            protected Task<Void> createTask() {
                
                return new Task<Void>(){

                    @Override
                    protected Void call() throws Exception {
                        System.out.println("Retrieving User Info: ");
                        String key=APIServices.getUserInfo(txUserName.getText().trim(),txPassword.getText().trim());
                        if(!key.isEmpty()){
                            if(key.equals("457")){
                                // wrong user/pass
                                System.out.println("Error 457 (Wrong user/pass)");
                                Platform.runLater(() -> lblWrong.setVisible(true));       
                            } else {
                                System.out.println("Api Key: "+key);
                                Platform.runLater(() -> lblWrong.setVisible(false));
                                
                                LhingsUser.getInstance().setApiKey(key);
                                System.out.println("Retrieving User Account: ");
                                if(!APIServices.getUsernameFromServer().equals("OK")){
                                    System.out.println("Retry Retrieving User Account: ");
                                    APIServices.getUsernameFromServer();
                                }
                                System.out.println("Retrieving Interface uuid: ");                                
                                String uuid=APIServices.getLhingsDeviceByName(LhingsUser.getInstance().getApiKey(),"Interface");
                                if(uuid!=null){
                                    System.out.println("Interface uuid: "+uuid);
                                    LhingsUser.getInstance().setUuidInterface(uuid);
                                }
                                
//                                APIServices.updateRules(APIServices.listRules(),
//                                                        LhingsUser.getInstance().getUuidTable(),
//                                                        LhingsUser.getInstance().getUuidInterface(),
//                                                        rulesTable);
                                
                            }
                        }
                        return null;
                    }                    
                };
            }
        };
        loginService.stateProperty().addListener((ov, t, t1) -> {
            if(t1==Worker.State.SUCCEEDED) {
                System.out.println("Logged!");
                logged.set(!LhingsUser.getInstance().getApiKey().isEmpty());
            }
        });
    }
    
    public void validateUser(){
        loginService.restart();
    }
    
    public void showLoginDialog() {
        Dialog dlg = new Dialog(null, "Login Dialog");
       
        // listen to user input on dialog (to enable / disable the login button)
        ChangeListener<String> changeListener = (ov,t,t1)->validate();

        txUserName.textProperty().addListener(changeListener);
        txPassword.textProperty().addListener(changeListener);

        // layout a custom GridPane containing the input fields and labels
        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);

        content.add(new Label("User name"), 0, 0);
        content.add(txUserName, 1, 0);
        GridPane.setHgrow(txUserName, Priority.ALWAYS);
        content.add(new Label("Password"), 0, 1);
        content.add(txPassword, 1, 1);
        GridPane.setHgrow(txPassword, Priority.ALWAYS);
        lblWrong.setVisible(false);
        content.add(lblWrong, 0, 2);
        GridPane.setHgrow(lblWrong, Priority.ALWAYS);
        GridPane.setColumnSpan(lblWrong, 2);
        
        // create the dialog with a custom graphic and the gridpane above as the
        // main content region
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        dlg.setGraphic(new ImageView(Login.class.getResource("login.png").toString()));
        dlg.setContent(content);
        dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
        validate();
       
        // request focus on the username field by default (so the user can
        // type immediately without having to click first)
        Platform.runLater(() -> txUserName.requestFocus());
        dlg.show();
    }
    
    // This method is called when the user types into the username / password fields  
    private void validate() {
        actionLogin.disabledProperty().set( 
        txUserName.getText().trim().isEmpty() || txPassword.getText().trim().isEmpty());
    }
   
    public BooleanProperty loggedProperty() { return logged; }
}
