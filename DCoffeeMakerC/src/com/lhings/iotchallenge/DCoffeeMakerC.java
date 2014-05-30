package com.lhings.iotchallenge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.net.URL;
import java.net.URI;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;


import org.json.JSONArray;
import org.json.JSONObject;

import com.lhings.library.Action;
import com.lhings.library.Stats;
import com.lhings.library.Event;
import com.lhings.library.LhingsDevice;
import com.lhings.library.Payload;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class DCoffeeMakerC extends LhingsDevice {

	private boolean on = false;

	final static GpioController gpio = GpioFactory.getInstance();
    
    static GpioPinDigitalOutput trig;
    static Thread rfid_thread;
    static RP_Rfid rfid;
    
	private static boolean enablePower = false;
	public static long minutesForCoffe = 120000;
	public static long lastTimeChecked;
	private boolean eventSent = false;
    Map<String,String> devices;
    
    
    // ************************************
    // ************* CONSTRUCTOR **********
    // ************************************
	public DCoffeeMakerC() {
		// substituir credenciales con las del coworking antes de enviar a JavaOne
 	   	super("david@lhings.com", "coworking", 5000, "CoffeeMaker");
		System.setProperty("jsse.enableSNIExtension", "false");

	}
    
    // ************************************
    // ************* LHINGS LOOP+UPDATE ***
    // ************************************
	@Override
	public void setup() {
		setupTrigger();
		setupRFID();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loop() {
		try {
 			updateTrigger();
            try{
                updateRfid();
            }
            catch (IOException e){
                e.printStackTrace(System.err);
            }

 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}

	}

    // ************************************
    // ************* RFID *****************
    // ************************************
    private void setupRFID() {
		rfid = new RP_Rfid();
		rfid_thread = new Thread(rfid,"RFID Table");
		rfid_thread.start();
	}
	
	public void setupTrigger(){ trig = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "trigger", PinState.LOW); }
    
	private void updateRfid() throws IOException{
		String apikey = rfid.getStringApiKey();
		if(apikey != null){
			if(isAllowed(apikey)){
				System.out.println("User with apikey " + apikey + " is allowed to make coffee");
				sendEventCoffeeMade(apikey);
				lastTimeChecked = System.currentTimeMillis();
			}else{
				System.out.println("User with apikey " + apikey + " is NOT allowed to make coffee");
			}
		}
	}
	
    public void updateTrigger() throws InterruptedException{
	   	if(System.currentTimeMillis()-lastTimeChecked<minutesForCoffe){
	   		enablePower = true;
			if(!eventSent){ eventSent = true; }
	   	}else{
	   		enablePower = false;
			eventSent = false;
	   	}
	   	if(enablePower){trig.high();}
        else{ trig.low(); }
    }

    // ************************************
    // ************* ACTIONS **************
    // ************************************

	@Action(name = "AllowUser", description = "", argumentNames = {"apikey"}, argumentTypes = {"String"})
	public void allowUser(String payload) {
		storeApiKey(payload);
		System.out.println("Successfully stored apikey " + payload);
	}

    // ************************************
    // ************* OTHER METHODS ********
    // ************************************

	private void storeApiKey(String apikey){
		HashSet<String> list = getAuthList();
		list.add(apikey);
		writeAuthList(list);
	}

	private boolean isAllowed(String apikey){ return getAuthList().contains(apikey);}

	private HashSet<String> getAuthList(){
		final String filename = "authUsers.list";
		try {
			File f = new File(filename);
			// check if file exists, create it if not
			if (!f.exists()){
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
				oos.writeObject(new HashSet<String>());
				oos.close();
			}
			
			// read list of authorized api keys
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
			HashSet<String> apikeyList = (HashSet<String>) ois.readObject();
			ois.close();
			return apikeyList;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return null;
	}

	private void writeAuthList(HashSet<String> apikeyList){
		final String filename = "authUsers.list";
		try{
			// add new api key and write again to file
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(apikeyList);
			oos.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

    private void sendEventCoffeeMade(String apikey)throws IOException{
        devices = getAllDevicesInAccount(apikey);
        sendNotificationToPlugLhings(apikey);
    }

    // ************************************
    // ************* WEBSERVICES **********
    // ************************************
	private Map<String,String> getAllDevicesInAccount(String apikey) {
		System.out.println("I will try to list all the devices in"+apikey);
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet get = new HttpGet("https://www.lhings.com/laas/api/v1/devices/");
            get.addHeader("User-Agent", "Mozilla/5.0");
			get.addHeader("X-Api-Key", apikey);

			CloseableHttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() != 200) {
				System.err.println("Device.list request failed: " + response.getStatusLine());
				response.close();
				System.exit(1);
			}
			String responseBody = EntityUtils.toString(response.getEntity());
			response.close();
			JSONArray listDevices = new JSONArray(responseBody);
			int numElements = listDevices.length();
			Map<String,String> results = new HashMap<String, String>();
			for (int i = 0; i<numElements; i++){
				JSONObject device = listDevices.getJSONObject(i);
				String uuid = device.getString("uuid");
				String name = device.getString("name");
				results.put(name, uuid);
			}
			
			return results;
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
			System.exit(1);
		}
		return null;
	}
    
    private void sendNotificationToPlugLhings(String apikey) throws IOException{
        System.out.println("I will try to send an Event in Account: "+apikey);
		String uuid = devices.get("PlugLhings");
            //sent to Pereda event
		CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        try {
            String message = "You have 5 minutes to make a coffee, enjoy!";
            httpClient = HttpClients.createDefault();
            URI uri = new URI("https://www.lhings.com/laas/api/v1/devices/"+uuid+"/actions/notifications");
            HttpPost httpPost = new HttpPost(uri);
            
            httpPost.setHeader("X-Api-Key", apikey);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            String json="[{ \"name\": \"text\", \"value\": \""+message+"\"}]";
            HttpEntity postBody = new StringEntity(json);
            httpPost.setEntity(postBody);
            
            response = httpClient.execute(httpPost);
            
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
                return;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(
                                                                         (response.getEntity().getContent())));
            
        } catch (IOException | URISyntaxException e) {
            System.out.println("Error "+e.toString());
        } finally {
            try{
                if(response!=null){
                    response.close();
                }
                if(httpClient!=null){
                    httpClient.close();
                }
            }catch(IOException ex) {
                System.out.println("Finally Error "+ex.toString());
            }
        }
        System.out.println("Notification to PlugLhings sent correctly");
	}
    
    // ************************************
    // ************* MAIN *****************
    // ************************************
    public static void main(String[] args) {
		@SuppressWarnings("unused")
            // starting your device is as easy as creating an instance!!
		DCoffeeMakerC coffeeMaker = new DCoffeeMakerC();
        
	}

}
