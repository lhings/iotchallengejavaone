package com.lhings.iotchallenge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.lhings.library.Action;
import com.lhings.library.Stats;
import com.lhings.library.Event;
import com.lhings.library.LhingsDevice;
import com.lhings.library.Payload;


public class DLamp extends LhingsDevice {

	private enum Mode { OFF, ON_AUTO, ON_MANUAL };

	Mode mode;

	private boolean wasTurnedOff = false;
	private boolean wasTurnedOn = false;
	private int intensity = 0;
	private int lastIntensityChange=0;


	public DLamp() {
		super();

	}

	@Override
	public void setup() {
		/*// set up GPIO pin 6 on Raspberry as digital output pin
		relay = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06,
		"Fan_Relay",
		PinState.LOW);*/

		// ensure lamp is in a know state (off)
		String payload = "{\"on\":false}";
		callWebService(payload);
		mode = Mode.OFF;

	}

	@Override
	public void loop() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (mode == Mode.ON_AUTO){
			int intensityLamp = readLightSensor();
			if (intensityLamp != intensity){
				callWebService(intensityLamp);
				intensity = intensityLamp;
			}
		}

	}

	@Event(name="TurnedOn")
	public String eventTurnOn(){
		if (wasTurnedOn){
			wasTurnedOn = false;
			return "";
		} else
			return null;
	}

	@Event(name="TurnedOff")
	public String eventTurnOff(){
		if (wasTurnedOff){
			wasTurnedOff = false;
			return "";
		} else
			return null;
	}

	@Stats(name = "Mode", type = "string")
	public String getMode(){
		if (mode == Mode.OFF)
			return "OFF";
		if (mode == Mode.ON_AUTO)
			return "ON - Automatic";
		if (mode == Mode.ON_MANUAL)
			return "ON - Manual";
		return "Unknown";
	}

	// *********** LAMP CONTROL *********************

	@Action(name = "TurnOn", description = "", argumentNames = {}, argumentTypes = {})
	public void on() {
		System.out.println("Received TurnOn");
		String payload = "{\"on\":true}";
		callWebService(payload);
		if (mode == Mode.OFF){
			mode = Mode.ON_AUTO;
			wasTurnedOn = true;
		}
		if (mode == Mode.ON_MANUAL)
			mode = Mode.ON_AUTO;

	}

	@Action(name = "TurnOff", description = "", argumentNames = {}, argumentTypes = {})
	public void off() {
		System.out.println("Received TurnOff");
		String payload = "{\"on\":false}";
		callWebService(payload);
		if (mode != Mode.OFF){
			mode = Mode.OFF;
			wasTurnedOff = true;
		}
	}

	@Action(name = "SetIntensity", description = "Set the intensity of the bulb.", argumentNames = {"intensity"}, argumentTypes = {"integer"})
	public void setLampIntensity(int intensity) {
		System.out.println("Intensity changed to " + intensity);
		mode = Mode.ON_MANUAL;
		callWebService(intensity);
	}

	@Action(name = "Shutdown", description = "turns off the lamp and disconnects it from the internet", argumentNames = {}, argumentTypes = {})
	public void shutdown(){
		System.out.println("Shutting down");
		System.exit(0);
	}

	// ************* private methods (Lamp related) ***************

	private void callWebService(int percentage) {
		int x = (int)(percentage * 2.55);
		String payload = "{\"on\":true, \"bri\":" + x + "}";
		callWebService(payload);
	}
	private void callWebService(String payload) {
		
		try {
			URL hueColorService = new URL(
					"http://192.168.0.111/api/iotchallenge/lights/4/state");
			HttpURLConnection conn = (HttpURLConnection) hueColorService.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = payload;

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 

	// ***************** private methods (light sensor related) **********************
	private int readLightSensor(){
		int currentTime = (int)(System.currentTimeMillis()/1000L);
		if (currentTime % 10 == 0 && lastIntensityChange != currentTime){
			int intensityLamp = (int)(Math.random()*100 + 1);
			lastIntensityChange = currentTime;
		}
		return intensity;
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		// starting your device is as easy as creating an instance!!
		DLamp lamp = new DLamp();

	}

}
