package com.lhings.iotchallenge;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import java.util.UUID;
import com.fasterxml.uuid.impl.UUIDUtil;

public class RP_Rfid implements Runnable{
	public static byte dataRX[];//Receive buffer.
    public static byte dataTX[];//Transmit buffer.
    public static String s_dataRX;
    public static byte _UID[];// stores the UID (unique identifier) of a card.
    public static byte keyAccess[]= {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF } ;// stores the key or password.
    public static byte address;//Address to read.
    public static byte ATQ[];//Answer to request
    public static int state;//state of the process
    public static byte aux[];//Auxiliar buffer.
    
    public static Serial serial = SerialFactory.createInstance();

    public RP_Rfid (){
    	setup();
    }
	@Override
	public void run() {
		while(true) loop();
	}
    
    public static void setup(){
        dataRX = new byte[35];//Receive buffer.
        dataTX = new byte[35];//Transmit buffer.
        _UID = new byte[4];// stores the UID (unique identifier) of a card.
        address = 0x04;//Address to read.
        ATQ = new byte[2];//Answer to request
        aux = new byte[16];//Auxiliar buffer.
        
        setupSerial();
        getFirmware();
        configureSAM();
        
    }
    
    public static void loop(){
    	
//        System.out.println("Ready to read...");
            /////////////////////////////////////////////////////////////
            //Get the UID Identifier
        init(_UID, ATQ);
        //System.out.print("The UID : ");
        //print(_UID , 4);
            /////////////////////////////////////////////////////////////
            //Auntenticate a block with his keyAccess
        state = authenticate(_UID, address, keyAccess);
        //System.out.println(" ");
        
        if ( state == 0) {
//            System.out.println("Authentication block OK");       
        } else {
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");         
            System.out.println("xxxxxxxxxxxx        AUTHENTIFICATION failed       xxxxxxxxxxxxx");         
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");         
        }
            /////////////////////////////////////////////////////////////
            //Read from address after authentication
        s_dataRX = readData(address, aux);
        if (s_dataRX == null) {
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");         
            System.out.println("xxxxxxxxxxxx        READ failed       xxxxxxxxxxxxx");         
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");         
        } else {
//            System.out.println("Read block OK");
        }
        
        s_dataRX = formatStringDataToUUIDtype(s_dataRX);
//        System.out.print("API KEY: "+s_dataRX);
        //UUID aux_uuid = UUIDUtil.uuid(s_dataRX);
        //System.out.println(aux_uuid);
        try {Thread.sleep(1000);}
        catch (InterruptedException ex){ ex.printStackTrace();}                
    }
  
    public String getStringApiKey(){String result =s_dataRX; s_dataRX=null;return result;}
    
	public static void setupSerial(){
        
		System.out.println("Serial Communication with RFID - 1.0");
        
        // create an instance of the serial communications class
        
        try {
            serial.open(Serial.DEFAULT_COM_PORT, 115200);
        }catch (SerialPortException ex){
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            return;
        }
        try {Thread.sleep(1000);}
        catch (InterruptedException ex){ ex.printStackTrace();}
        
    }
	
            //**********************************************************************
            //!The goal of this command is to detect as many targets (maximum MaxTg)
            // as possible in passive mode.
	public static byte init(byte []UID , byte[] aTQ2){
		serial.flush();

        dataTX[0] = (byte)0x04; // Length
        lengthCheckSum(dataTX); // Length Checksum
        dataTX[2] = (byte) 0xD4;
        dataTX[3] = (byte)0x4A; // Code
        dataTX[4] = (byte)0x01; //MaxTarget
        dataTX[5] = (byte)0x00; //BaudRate = 106Kbps
        dataTX[6] = (byte)0x00; // Clear checkSum position
        checkSum(dataTX);

        sendTX(dataTX, 7, 23);

        for (int i = 17; i < (21) ; i++){
            _UID[i-17] = dataRX[i];
            UID[i-17] = _UID[i-17];
        }

        aTQ2[0] = dataRX[13];
        aTQ2[1] = dataRX[14];

        if ((dataRX[9]== (byte)0xD5) & (dataRX[10] == (byte)0x4B) & (dataRX[11] == (byte)0x01)) {
            return 0;
        } else {
            return 1;
        }
    }
        //**********************************************************************
        //!A block must be authenticated before read and write operations
    public static int authenticate(byte[] UID, byte blockAddress, byte[] keyAccess){
        dataTX[0] = (byte)0x0F;
        lengthCheckSum(dataTX);
        dataTX[2] = (byte) 0xD4;
        dataTX[3] = (byte)0x40; // inDataEchange
        dataTX[4] = (byte)0x01; //Number of targets
        dataTX[5] = (byte)0x60; // Authentication code
        dataTX[6] = blockAddress;
        for (int i = 0; i < 6 ; i++) {
            dataTX[i + 7] = keyAccess[i];
        }
        dataTX[13] = UID[0];  dataTX[14] = UID[1];
        dataTX[15] = UID[2];  dataTX[16] = UID[3];
        dataTX[17] = 0x00;
        checkSum(dataTX);
        sendTX(dataTX , 18 ,14);
        if ((dataRX[9]== (byte)0xD5) & (dataRX[10] == (byte)0x41) & (dataRX[11] == (byte)0x00)) {
            return 0;
        } else {
            return 1;
        }
    }
        //**********************************************************************
        //!Write 16 bytes in address .
    public static int writeData(byte address, byte[] blockData) {
        serial.write("                ");
        dataTX[0] = 0x15;
        lengthCheckSum(dataTX); // Length Checksum
        dataTX[2] = (byte)0xD4;
        dataTX[3] = (byte)0x40;//inDataEchange CODE
        dataTX[4] = (byte)0x01;//Number of targets
        dataTX[5] = (byte)0xA0;//Write Command
        dataTX[6] = address; //Address

        for (int i = 0; i < 16; i++) {
            dataTX[i+7] = blockData[i];
        }

        dataTX[23] = 0x00;
        checkSum(dataTX);
        sendTX(dataTX , 24 ,14);

        if ((dataRX[9]== (byte)0xD5) & (dataRX[10] == (byte)0x41) & (dataRX[11] == (byte)0x00)) {
            return 0;
        } else {
            return 1;
        }
    }
        //**********************************************************************
        //!Read 16 bytes from  address .
    public static String readData(byte address, byte[] readData) {
        serial.write("                ");
        dataTX[0] = (byte)0x05;
        lengthCheckSum(dataTX); // Length Checksum
        dataTX[2] = (byte) 0xD4; // Code
        dataTX[3] = (byte)0x40; // Code
        dataTX[4] = (byte)0x01; // Number of targets
        dataTX[5] = (byte)0x30; //ReadCode
        dataTX[6] = address;  //Read address
        dataTX[7] = (byte)0x00;
        checkSum(dataTX);
        sendTX(dataTX , 8, 30);
    //    memset(readData, 0x00, 16);

        StringBuffer b = new StringBuffer();
        String s = null;
        
        if ((dataRX[9]== (byte)0xD5) & (dataRX[10] == (byte)0x41) & (dataRX[11] == (byte)0x00)) {
            for (int i = 12; i < 28; i++) {
                readData[i-12] = dataRX[i];
                s = Integer.toHexString(readData[i-12]);
                if (readData[i-12] < 0) {
                    b.append(s.substring(s.length() - 2));
                } else {
                	if(readData[i-12]<10){
                		b.append("0"+s);
                	}else{
                		b.append(s);
                	}
                    
                }
            }            
            return b.toString();
        } else {
            return null;
        }
    }
    //b0df9be0-9fc8-4a09-b805-007ef550e32d
    private static String formatStringDataToUUIDtype(String s_dataRX2) {
    	String result = s_dataRX2.substring(0,8)+"-"+s_dataRX2.substring(8,12)+"-"+s_dataRX2.substring(12,16)+"-"+s_dataRX2.substring(16,20)+"-"+s_dataRX2.substring(20,s_dataRX2.length());
    	return result;
	}

        //**********************************************************************
        //!The PN532 sends back the version of the embedded firmware.
    public static void getFirmware(){
        serial.write("                ");
        dataTX[0] = (byte)0x02; // Length
        lengthCheckSum(dataTX); // Length Checksum
        dataTX[2] = (byte) 0xD4; // CODE
        dataTX[3] = (byte)0x02; //TFI
        checkSum(dataTX); //0x2A; //Checksum
        sendTX(dataTX , 5 , 17);
        System.out.print("FIRWARE VERSION: ");

        for (int i = 11; i < (15) ; i++){
            System.out.printf("%02x",dataRX[i]);
            System.out.print(" ");
        }        
        System.out.println(" ");
    }

        //**********************************************************************
        //!Print data stored in vectors .
    public static void print(byte[] _UID2, int j){
    	for (int i = 0; i < j ; i++){
        	System.out.printf("%02x", _UID2[i]);
        	System.out.print(" ");        
        }    	
        System.out.println("");
    }
        //**********************************************************************
        //!This command is used to set internal parameters of the PN532,
    public static void configureSAM() {
        serial.write("               ");
        dataTX[0] = (byte)0x05; //Length

        lengthCheckSum(dataTX); // Length Checksum
        dataTX[2] = (byte) 0xD4;
        dataTX[3] = (byte)0x14;
        dataTX[4] = (byte)0x01; // Normal mode
        dataTX[5] = (byte)0x14; // TimeOUT
        dataTX[6] = (byte)0x00; // IRQ
        dataTX[7] = (byte)0x00; // Clean checkSum position
        checkSum(dataTX);

        sendTX(dataTX , 8, 13);
    }
        //**********************************************************************
        //!Send data stored in dataTX
    public static void sendTX(byte[] dataTX, int length, int outLength){
    	serial.write((byte) 0x00);
        serial.write((byte) 0x00);
        serial.write((byte) 0xFF);
        
        for (int i = 0; i < length; i++) {
            serial.write(dataTX[i]);
        }

        serial.write((byte) 0x00);
        getACK();
        waitResponse();// Receive response
        getData(outLength);
    }
        //**********************************************************************
        //!Wait for ACK response and stores it in the dataRX buffer
    public static void getACK(){
        try{
            Thread.sleep(5);
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }
        waitResponse();
        for (int i = 0; i < 5 ; i++) {
            dataRX[i] = (byte) serial.read();
        }
    }
        //**********************************************************************
        //!Wait the response of the module
    public static void waitResponse(){
        byte val = (byte)0xFF;

    	while(val != 0x00) { //Wait for 0x00 response
            val = (byte)serial.read();
            try{
                Thread.sleep(5);
            }catch(InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
        //**********************************************************************
        //!Get data from the module
    public static void getData(int outLength){
        for (int i=5; i < outLength; i++) {
            dataRX[i] = (byte) serial.read();//read data from the module.
        }
    }
        //**********************************************************************
        //!Calculates the checksum and stores it in dataTX buffer
    public static void checkSum(byte[] dataTX2){
        for (int i = 0; i < dataTX2[0] ; i++) {
            dataTX2[dataTX2[0] + 2] += dataTX2[i + 2];
        }
        dataTX2[dataTX2[0] + 2]= (byte) - dataTX2[dataTX2[0] + 2];
    }
    
        //**********************************************************************
        //!Calculates the length checksum and sotres it in the buffer.
    public static void lengthCheckSum(byte[] dataTX2){
        dataTX2[1] = (byte) (0x100 - dataTX2[0]);
    }
}
