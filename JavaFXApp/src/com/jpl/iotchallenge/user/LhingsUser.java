package com.jpl.iotchallenge.user;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class LhingsUser {

    private String user;
    private String pass;
    private String surname1;
    private String name;
    private String avatar;
    private String apiKey;
    private String apiKeyCoworking;
    private String uuidLamp;
    private String uuidTable;
    private String uuidCoffe;
    private String uuidInterface;
    private final static LhingsUser instance=new LhingsUser();

    public static LhingsUser getInstance(){ return instance; }
    
    private LhingsUser(){
        
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getSurname1() {
        return surname1;
    }

    public void setSurname1(String surname1) {
        this.surname1 = surname1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKeyCoworking() {
        return apiKeyCoworking;
    }

    public void setApiKeyCoworking(String apiKeyCoworking) {
        this.apiKeyCoworking = apiKeyCoworking;
    }

    public String getUuidLamp() {
        return uuidLamp;
    }

    public void setUuidLamp(String uuidLamp) {
        this.uuidLamp = uuidLamp;
    }

    public String getUuidTable() {
        return uuidTable;
    }

    public void setUuidTable(String uuidTable) {
        this.uuidTable = uuidTable;
    }

    public String getUuidCoffe() {
        return uuidCoffe;
    }

    public void setUuidCoffe(String uuidCoffe) {
        this.uuidCoffe = uuidCoffe;
    }

    public String getUuidInterface() {
        return uuidInterface;
    }

    public void setUuidInterface(String uuidInterface) {
        this.uuidInterface = uuidInterface;
    }

    @Override
    public String toString() {
        return "LhingsUser{" + "user=" + user + ", pass=" + pass + ", surname1=" + surname1 + ", name=" + name + ", avatar=" + avatar + ", apiKey=" + apiKey + ", apiKeyCoworking=" + apiKeyCoworking + ", uuidLamp=" + uuidLamp + ", uuidTable=" + uuidTable + ", uuidCoffe=" + uuidCoffe + ", uuidInterface=" + uuidInterface + '}';
    }

}
