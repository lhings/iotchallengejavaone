package com.jpl.iotchallenge.services;

import com.jpl.iotchallenge.user.LhingsUser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class APIServices {
    
    public static final String LHINGS       = "https://www.lhings.com/laas/api/v1";

    public static String getUsernameFromServer() {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet get = new HttpGet(LHINGS+"/account/");
            get.addHeader("X-Api-Key", LhingsUser.getInstance().getApiKey());
            String responseBody;
            try (CloseableHttpResponse response = httpclient.execute(get)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("Username could not be retrieved from server, server responded " + response.getStatusLine());
                    response.close();
                    return null;
                }   
                responseBody = EntityUtils.toString(response.getEntity());
            }
            JSONObject accountInfo = new JSONObject(responseBody);
            LhingsUser.getInstance().setName(accountInfo.get("name").toString());
            LhingsUser.getInstance().setSurname1(accountInfo.get("surname1").toString());
            LhingsUser.getInstance().setAvatar(accountInfo.get("avatarURL").toString());
            return "OK";
        } catch (IOException ex) {
        }

        return null;
    }
    
    public static String getLhingsDeviceByName(String apikey, String deviceName) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet get = new HttpGet(LHINGS+"/devices/");
            get.addHeader("X-Api-Key", apikey);
            get.setHeader("Content-type", "application/json");
            String responseBody;
            try (CloseableHttpResponse response = httpclient.execute(get)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("DEVICE Failed - HTTP error code: " + response.getStatusLine().getStatusCode());
                    response.close();
                    return null;
                }   
                responseBody = EntityUtils.toString(response.getEntity());
            }
            JSONArray tokener = new JSONArray(responseBody);
            for(int i=0; i< tokener.length(); i++){
                JSONObject jsonObj=tokener.getJSONObject(i);
                if (jsonObj.getString("name").matches(deviceName)){
                    return jsonObj.get("uuid").toString();
                }
            }
        } catch (IOException ex) {
            System.out.println("Error "+ex.toString());
        }

        return null;
    }
    
    public static void getDeviceInfo(String uuid){
        CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        try {

            httpClient = HttpClients.createDefault();		
            URI uri = new URI(LHINGS+"/devices/"+uuid+"/");
            HttpGet httpGet = new HttpGet(uri);

            httpGet.setHeader("X-Api-Key", LhingsUser.getInstance().getApiKey());
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            
            response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("INFO Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
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
    }
    
    public static void getEvents(String uuid){
        CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        try {
            Calendar cal=Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            httpClient = HttpClients.createDefault();		
            URI uri = new URI(LHINGS+"/devices/"+uuid+"/events/finished?begin="+cal.getTimeInMillis()+
                    "&end="+Calendar.getInstance().getTimeInMillis()+"&mode=aggregate_hourly&human_readable=false");
            System.out.println("url: "+uri.toString());
            HttpGet httpGet = new HttpGet(uri);

            httpGet.setHeader("X-Api-Key", LhingsUser.getInstance().getApiKey());
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            
            response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("EVENTS Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
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
    }
    
    public static String getUserInfo(String name, String pass){
        CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        try {

            httpClient = HttpClients.createDefault();		
            URI uri = new URI(LHINGS+"/account/"+name+"/apikey?password="+pass);
            HttpGet httpGet = new HttpGet(uri);

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            
            response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("USER Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
                return Integer.toString(response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String json = br.readLine();
            if(json==null || json.isEmpty()){
                return "";
            }
            JSONTokener tokener = new JSONTokener(json);	
            JSONObject jsonObj = new JSONObject(tokener);

            if (jsonObj.getString("name").matches("apikey")){
                   return jsonObj.get("value").toString();
            } else {
                   return "";
            }
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
        return "";
    }
    
    public static void doDeviceAction(String uuid, String actionName){
        doDeviceAction(uuid, actionName,"[]");
    }
    public static void doDeviceAction(String uuid, String actionName, String json){
        CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        try {

            httpClient = HttpClients.createDefault();		
            URI uri = new URI(LHINGS+"/devices/"+uuid+"/actions/"+actionName);
            HttpPost httpPost = new HttpPost(uri);

            httpPost.setHeader("X-Api-Key", LhingsUser.getInstance().getApiKeyCoworking());
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            
            HttpEntity postBody = new StringEntity(json);
            httpPost.setEntity(postBody);

            response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("ACTION Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
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
    }
    
    public static void fakeDeviceEvent(String uuid, String event){
        fakeDeviceEvent(uuid, event, "");
    }
    public static void fakeDeviceEvent(String uuid, String event, String payload){
        if(uuid.isEmpty()){
            return;
        }
        CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        try {

            httpClient = HttpClients.createDefault();		
            URI uri = new URI(LHINGS+"/devices/"+uuid+"/events/"+event);
            HttpPost httpPost = new HttpPost(uri);

            httpPost.setHeader("X-Api-Key", LhingsUser.getInstance().getApiKey());
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            
            if(!payload.isEmpty()){
                HttpEntity postBody = new StringEntity("{ \"name\": \"payload\", \"value\": \""+payload+"\"}");
                httpPost.setEntity(postBody);
            }

            response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("EVENT Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
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
    }
    
    public static JSONArray listStatus(String uuid){
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet get = new HttpGet(LHINGS+"/devices/"+uuid+"/states");
            get.addHeader("X-Api-Key", LhingsUser.getInstance().getApiKeyCoworking());
            get.setHeader("Content-type", "application/json");
            String responseBody;
            try (CloseableHttpResponse response = httpclient.execute(get)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("STATUS Failed - HTTP error code: " + response.getStatusLine().getStatusCode());
                    response.close();
                    return null;
                }   
                responseBody = EntityUtils.toString(response.getEntity());
            }
            
            return new JSONArray(responseBody);
            
        } catch (IOException ex) {
            System.out.println("Error "+ex.toString());
        }

        return null;
    }
    
    public static JSONArray listRules(){
        System.out.println("Updating rules");
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet get = new HttpGet(LHINGS+"/rules/");
            get.addHeader("X-Api-Key", LhingsUser.getInstance().getApiKey());
            get.setHeader("Content-type", "application/json");
            String responseBody;
            try (CloseableHttpResponse response = httpclient.execute(get)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("RULES Failed - HTTP error code: " + response.getStatusLine().getStatusCode());
                    response.close();
                    return null;
                }   
                responseBody = EntityUtils.toString(response.getEntity());
            }
            return new JSONArray(responseBody);
            
        } catch (IOException ex) {
            System.out.println("Error "+ex.toString());
        }

        return null;
    }
    
    // rule: 
    /*
    {"id":"239",
     "name":"Table Taxi Requested Success",
     "sourceDevice":"c4380b0f-514c-490e-82c2-b381c8cfc4d8",
     "targetDevice":"1d5f6e94-ba90-40fb-9302-19016568754f",
     "sourceApp":null,
     "targetApp":null,
     "targetAction":"taxiRequestedSuccess",
     "sourceEvent":"TaxiRequestedSuccess",
     "targetActionArgs":[]}
    */
    // name - sourceEvent - targetAction
    
    public static void createRules(String uuidSource, String uuidTarget, int id, String[][] rules){
        CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        try {

            httpClient = HttpClients.createDefault();		
            URI uri = new URI(LHINGS+"/rules");
            HttpPost httpPost = new HttpPost(uri);

            httpPost.setHeader("X-Api-Key", LhingsUser.getInstance().getApiKey());
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            
            HttpEntity postBody = new StringEntity("{\"name\":\""+rules[id][0]+"\"," +
                " \"sourceDevice\":\""+uuidSource+"\", \"targetDevice\":\""+uuidTarget+"\"," +
                " \"sourceApp\":null, \"targetApp\":null," +
                " \"targetAction\":\""+rules[id][2]+"\"," +
                " \"sourceEvent\":\""+rules[id][1]+"\", \"targetActionArgs\":[]}");
            httpPost.setEntity(postBody);

            response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
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
    }
         
    public static void updateRules(JSONArray tokener, String uuidSource, String uuidTarget, String[][] rules){
        int cont=0;
        for (String[] rule : rules) {
            boolean bExists=false;
            for(int i=0; i<tokener.length(); i++){
                JSONObject jsonObj=tokener.getJSONObject(i);  
//                System.out.println("j: "+jsonObj.toString());
                if(jsonObj.getString("targetAction").equals(rule[2]) && 
                   jsonObj.getString("sourceEvent").equals(rule[1])) {
                    // la regla existe
                    bExists=true;
                    if(!jsonObj.getString("sourceDevice").equals(uuidSource) || 
                        !jsonObj.getString("targetDevice").equals(uuidTarget)){
                        // pero con distintos uuid, entonces borra regla
                        System.out.println("updating rule "+rule[0]);
                        deleteRule(jsonObj.getString("id"));
                        // y la crea de nuevo con uuid correcto
                        createRules(uuidSource, uuidTarget, cont, rules);
                    }
                }
            }
            if(!bExists){
                // no existe, se crea
                System.out.println("creating rule "+rule[0]);
                createRules(uuidSource, uuidTarget, cont, rules);
            }
            cont+=1;
        }
    }
    
    public static String deleteRule(String ruleId) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpDelete del = new HttpDelete(LHINGS+"/rules/"+ruleId);
            del.addHeader("X-Api-Key", LhingsUser.getInstance().getApiKey());
            String responseBody;
            try (CloseableHttpResponse response = httpclient.execute(del)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("Rule can not be deleted from server, server responded " + response.getStatusLine());
                    response.close();
                    return null;
                }   
                responseBody = EntityUtils.toString(response.getEntity());
                JSONObject rule = new JSONObject(responseBody);
                return rule.getString("message");
            }
        } catch (IOException ex) {
        }

        return null;
    }
}
