package com.jpl.iotchallenge.device;

import com.jpl.iotchallenge.services.APIServices;
import com.jpl.iotchallenge.user.LhingsUser;
import com.lhings.library.Action;
import com.lhings.library.LhingsDevice;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class DInterface extends LhingsDevice {

    private final BooleanProperty checkedIn=new SimpleBooleanProperty(false);
    private final BooleanProperty light=new SimpleBooleanProperty(false);
    private final BooleanProperty availability=new SimpleBooleanProperty(false);
    private final StringProperty notify=new SimpleStringProperty("");
    
    
    public DInterface(){
        super(LhingsUser.getInstance().getUser(), LhingsUser.getInstance().getPass(), 6000, "Interface");
    }
    
    @Override
    public void setup() {
    }

    @Override
    public void loop() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) { }
    }

    /***********************************
     ************** TABLE **************
     ***********************************/
    
    @Action(name = "checkIn", description = "action used to notify checked in event in Table", 
            argumentNames = {"apikey"}, argumentTypes = {"string"})
    public void actionCheckIn(String apikey){
        Platform.runLater(()->{
            LhingsUser.getInstance().setApiKeyCoworking(apikey);
            checkedIn.set(true);
        });
    }
    @Action(name = "checkStatus", description = "action used to notify status changes in Table", 
            argumentNames = {}, argumentTypes = {})
    public void actionCheckStatus(){
        Platform.runLater(()-> {
            JSONArray listStatus = APIServices.listStatus(LhingsUser.getInstance().getUuidTable());
            if(listStatus!=null){
                for(int i=0; i<listStatus.length(); i++){
                    JSONObject jsonObj=listStatus.getJSONObject(i);
//                    System.out.println("j: "+jsonObj.toString());
                    String r;
                    boolean res;
                    try{
                        r=jsonObj.getString("value");
                        res=Boolean.parseBoolean(r);
                    } catch(JSONException j){
                        res=jsonObj.getBoolean("value");
                    }
                    switch(jsonObj.getString("name")){
                        case "checkedIn": checkedIn.set(res);
                            break;
                        case "light": light.set(res);
                            break;
                        case "availability": availability.set(res);
                            break;
                    } 
                }
            }
        });
    }
    @Action(name = "notifications", description = "action used to notify status changes in Table", 
            argumentNames = {"text"}, argumentTypes = {"string"})
    public void actionNotifications(String text){
        Platform.runLater(()->notify.set(text));
    }
    
    public BooleanProperty checkedInProperty() { return checkedIn; }
    public BooleanProperty lightProperty() { return light; }
    public BooleanProperty availabilityProperty() { return availability; }
    public StringProperty notifyProperty() { return notify; }
    
    public void resetActions(){
        notify.set("");
    }
    
}
