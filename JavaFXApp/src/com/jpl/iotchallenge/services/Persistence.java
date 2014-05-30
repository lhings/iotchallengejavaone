package com.jpl.iotchallenge.services;

import com.jpl.iotchallenge.user.LhingsUser;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javafx.stage.Stage;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class Persistence {

    private static Properties properties;
    private static final Map<String, Object> persistentComponents = new HashMap();

    public static void loadProperties() {
        properties = PropertiesUtils.loadProperties();
    }

    public static String loadProperty(String propertyName, String defaultValue) {
        return properties.getProperty(propertyName, defaultValue);
    }

    public static void saveProperty(String propertyName, String defaultValue) {
        properties.put(propertyName, defaultValue);
    }

    public static void loadProperty(String propertyName, Object component, Object defaultValue) {
        String property = properties.getProperty(propertyName, defaultValue.toString());
        if (component instanceof Stage) {
            if (propertyName.contains("Width")) {
                ((Stage)component).setWidth(Double.parseDouble(property));
            } else if (propertyName.contains("Height")) {
                ((Stage)component).setHeight(Double.parseDouble(property));
            } else if (propertyName.contains("X")) {
                ((Stage)component).setX(Double.parseDouble(property));
            } else if (propertyName.contains("Y")) {
                ((Stage)component).setY(Double.parseDouble(property));
            } 
        } else if (component instanceof LhingsUser){
            if (propertyName.equals("user")){
                ((LhingsUser)component).setUser(property);
            } else if (propertyName.equals("pass")){
                ((LhingsUser)component).setPass(property);
            } else if (propertyName.equals("name")){
                ((LhingsUser)component).setName(property);
            } else if (propertyName.equals("surname1")){
                ((LhingsUser)component).setSurname1(property);
            } else if (propertyName.equals("avatar")){
                ((LhingsUser)component).setAvatar(property);
            } else if (propertyName.equals("apiKey")){
                ((LhingsUser)component).setApiKey(property);
            } else if (propertyName.equals("uuidLamp")){
                ((LhingsUser)component).setUuidLamp(property);
            } else if (propertyName.equals("uuidTable")){
                ((LhingsUser)component).setUuidTable(property);
            } else if (propertyName.equals("uuidCoffe")){
                ((LhingsUser)component).setUuidCoffe(property);
            } else if (propertyName.equals("uuidInterface")){
                ((LhingsUser)component).setUuidInterface(property);
            }  
        }
        persistentComponents.put(propertyName, component);
    }

    public static void saveProperties() {
        System.out.println("Guardando propiedades");
        persistentComponents.keySet().stream().forEach((propertyName) -> {
            Object component = persistentComponents.get(propertyName);
            if(component instanceof Stage){
                Stage s=(Stage)component;
                if (propertyName.contains("Width")){
                    properties.put(propertyName, Double.toString(s.getWidth()));
                } else if (propertyName.contains("Height")){
                    properties.put(propertyName, Double.toString(s.getHeight()));
                } else if (propertyName.contains("X")){
                    properties.put(propertyName, Double.toString(s.getX()));
                } else if (propertyName.contains("Y")){
                    properties.put(propertyName, Double.toString(s.getY()));
                }
            } else if(component instanceof LhingsUser){
                LhingsUser u=(LhingsUser)component;
                if (propertyName.equals("user")){
                    properties.put(propertyName, u.getUser());
                } else if (propertyName.equals("pass")){
                    properties.put(propertyName, u.getPass());
                } else if (propertyName.equals("name")){
                    properties.put(propertyName, u.getName());
                } else if (propertyName.equals("surname1")){
                    properties.put(propertyName, u.getSurname1());
                } else if (propertyName.equals("avatar")){
                    properties.put(propertyName, u.getAvatar());
                } else if (propertyName.equals("apiKey")){
                    properties.put(propertyName, u.getApiKey());
                } else if (propertyName.equals("uuidLamp")){
                    properties.put(propertyName, u.getUuidLamp());
                } else if (propertyName.equals("uuidTable")){
                    properties.put(propertyName, u.getUuidTable());
                } else if (propertyName.equals("uuidCoffe")){
                    properties.put(propertyName, u.getUuidCoffe());
                } else if (propertyName.equals("uuidInterface")){
                    properties.put(propertyName, u.getUuidInterface());
                } 
            }
        });
        PropertiesUtils.saveProperties();
    }
}
