/*
Ajouino Home Automation System 
with Arduino Yun 

This file is for Ajouino Home Automation System.
Built on the top of Arduino Yun Server.

Possible commands created in this shetch:


Default Example Commands:
* "/arduino/digital/13"     -> digitalRead(13)
* "/arduino/digital/13/1"   -> digitalWrite(13, HIGH)
* "/arduino/analog/2/123"   -> analogWrite(2, 123)
* "/arduino/analog/2"       -> analogRead(2)
* "/arduino/mode/13/input"  -> pinMode(13, INPUT)
* "/arduino/mode/13/output" -> pinMode(13, OUTPUT)

Specific Commands for Ajouino:
* "/arduino/hello/"			-> register & return current value of the device
* "/arduino/bye/"			-> unregister the device
* "/arduino/color/FFAABBCC" -> set lamp color by #FFAABBCC, first two means brightness here
* "/arduino/power/3"		-> set powerstrip value by 1, 1 (in binary notation, 3= 11)

The example code is from part of the public domain
http://arduino.cc/en/Tutorial/Bridge

*/

#include <Console.h>
#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>
#include <Process.h>

// Please change this settings appropriately.
#define ID "ArduinoLLLL"
#define TYPE "lamp"
#define LABEL "Arduino Lamp"
//#define ID "ArduinoPowerstrip"
//#define TYPE "powerstrip"
//#define LABEL "Arduino Powerstrip"

#define PIN_R 9
#define PIN_G 10
#define PIN_B 11

#define NUM_PORTS 2
#define PIN_POWER_A 7
#define PIN_POWER_B 8

int value = 0;

int value_a = 0;
int value_r = 0;
int value_g = 0;
int value_b = 0;

int value_power[NUM_PORTS] = { 0 };

// will forward there all the HTTP requests for us.
YunServer server;

void setup() {
	// Set pin
	pinMode(PIN_R, OUTPUT);
	pinMode(PIN_G, OUTPUT);
	pinMode(PIN_B, OUTPUT);

	pinMode(PIN_POWER_A, OUTPUT);
	pinMode(PIN_POWER_B, OUTPUT);

	// Bridge startup
	digitalWrite(13, HIGH);
	Bridge.begin();
	digitalWrite(13, LOW);

	// Listen for incoming connection only from localhost
	// (no one from the external network could connect)
	server.listenOnLocalhost();
	server.begin();

	// Initialize Console and wait for port to open:
	Console.begin();

	// Wait for Console port to connect
	while (!Console);
	Console.println("Server start");
}

void loop() {
	// Get clients coming from server
	YunClient client = server.accept();

	// There is a new client?
	if (client) {
		// Process request
		process(client);

		// Close connection and free resources.
		client.stop();
	}

	delay(50); // Poll every 50ms
}

void process(YunClient client) {
	// read the command
	String command = client.readStringUntil('/');
	Console.print("command: ");
	Console.println(command);

	// is "hello" command?
	if (command == "hello") {
		helloCommand(client);
	}

	// is "color" command?
	else if (command == "color") {
		colorCommand(client);
	}

	// is "color" command?
	else if (command == "power") {
		powerCommand(client);
	}

	// is "digital" command?
	else if (command == "digital") {
		digitalCommand(client);
	}

	// is "analog" command?
	else if (command == "analog") {
		analogCommand(client);
	}

	// is "mode" command?
	else if (command == "mode") {
		modeCommand(client);
	}

}

void helloCommand(YunClient client) {
	Process p;
	p.runShellCommand("ifconfig");

	String id = ID;
	String type = TYPE;
	String label = LABEL;
	String addr;
	while (p.running());
	while (p.available()) {
		String result = p.readStringUntil('\n');
		if (result.startsWith("wlan0")) {
			p.readStringUntil(':');
			addr = p.readStringUntil(' ');
			p.flush();
			break;
		}
	}

	char deviceInfo[255] = { 0 };
	if (type.equals("lamp")) {
		sprintf(deviceInfo, "{\"id\":\"%s\",\"type\" : \"%s\", \"address\" : \"%s\", \"label\" : \"%s\", \"values\" : {\"R\": %d, \"G\": %d, \"B\": %d, \"value\": %d}}",
			id.c_str(), type.c_str(), addr.c_str(), label.c_str(), value_r, value_g, value_b, value);
	}
	else if (type.equals("powerstrip")) {
		sprintf(deviceInfo, "{\"id\":\"%s\",\"type\" : \"%s\", \"address\" : \"%s\", \"label\" : \"%s\", \"values\" : {\"ports\": %d, \"value\": %d}}",
			id.c_str(), type.c_str(), addr.c_str(), label.c_str(), NUM_PORTS, value);
	}

	client.read();
	client.print(deviceInfo);
}


void colorCommand(YunClient client) {
	String param = client.readString();
	client.flush();
	client.println(param);
	//int color = atoi(param.c_str());
	if (param.startsWith("#")) param = param.substring(1);
	sscanf(param.c_str(), "%02X%02X%02X%02X", &value_a, &value_r, &value_g, &value_b);
	
	//B = color & 0xFF;
	//G = (color >> 8) & 0xFF;
	//R = (color >> 16) & 0xFF;
	//A = (color >> 24) & 0xFF;

	value = (value_a << 24) | (value_r << 16) | (value_g << 8) | (value_b);

	double brightAdjust = (double)value_a / 255.0;
	
	value_r *= brightAdjust;
	value_g *= brightAdjust;
	value_b *= brightAdjust;

	analogWrite(PIN_R, value_r);
	analogWrite(PIN_G, value_g);
	analogWrite(PIN_B, value_b);

	Console.print(F("LED Set to color R("));
	Console.print(value_r);
	Console.print(F(") G("));
	Console.print(value_g);
	Console.print(F(") B("));
	Console.print(value_b);
	Console.println(F(")"));
	client.println(F("OK"));

}


void powerCommand(YunClient client) {
	int param = client.parseInt();
	client.flush();
	client.println(param);

	value = param;

	int a = param & 1;
	int b = (param >> 1) & 1;

	if (a != value_power[0]) digitalWrite(PIN_POWER_A, a);
	if (b != value_power[1]) digitalWrite(PIN_POWER_B, b);

	value_power[0] = a;
	value_power[1] = b;

	Console.print(F("POWER Set to value("));
	Console.print(value_power[0]);
	Console.print(value_power[1]);
	Console.println(F(")"));
	client.println(F("OK"));
}

void digitalCommand(YunClient client) {
	int pin, value;

	// Read pin number
	pin = client.parseInt();

	// If the next character is a '/' it means we have an URL
	// with a value like: "/digital/13/1"
	if (client.read() == '/') {
		value = client.parseInt();
		digitalWrite(pin, value);
	}
	else {
		value = digitalRead(pin);
	}

	// Send feedback to client
	client.print(F("Pin D"));
	client.print(pin);
	client.print(F(" set to "));
	client.println(value);

	// Update datastore key with the current pin value
	String key = "D";
	key += pin;
	Bridge.put(key, String(value));
}

void analogCommand(YunClient client) {
	int pin, value;

	// Read pin number
	pin = client.parseInt();

	// If the next character is a '/' it means we have an URL
	// with a value like: "/analog/5/120"
	if (client.read() == '/') {
		// Read value and execute command
		value = client.parseInt();
		analogWrite(pin, value);

		// Send feedback to client
		client.print(F("Pin D"));
		client.print(pin);
		client.print(F(" set to analog "));
		client.println(value);

		// Update datastore key with the current pin value
		String key = "D";
		key += pin;
		Bridge.put(key, String(value));
	}
	else {
		// Read analog pin
		value = analogRead(pin);

		// Send feedback to client
		client.print(F("Pin A"));
		client.print(pin);
		client.print(F(" reads analog "));
		client.println(value);

		// Update datastore key with the current pin value
		String key = "A";
		key += pin;
		Bridge.put(key, String(value));
	}
}

void modeCommand(YunClient client) {
	int pin;

	// Read pin number
	pin = client.parseInt();

	// If the next character is not a '/' we have a malformed URL
	if (client.read() != '/') {
		client.println(F("error"));
		return;
	}

	String mode = client.readStringUntil('\r');

	if (mode == "input") {
		pinMode(pin, INPUT);
		// Send feedback to client
		client.print(F("Pin D"));
		client.print(pin);
		client.print(F(" configured as INPUT!"));
		return;
	}

	if (mode == "output") {
		pinMode(pin, OUTPUT);
		// Send feedback to client
		client.print(F("Pin D"));
		client.print(pin);
		client.print(F(" configured as OUTPUT!"));
		return;
	}

	client.print(F("error: invalid mode "));
	client.print(mode);
}

