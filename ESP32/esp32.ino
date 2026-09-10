#include <WiFi.h>
#include <WebServer.h>
#include <HX711.h>
#include <ESP32Servo.h>

#include "secrets.h"
// =====================================================
// WIFI SETTINGS
// =====================================================



// =====================================================
// PIN DEFINITIONS
// =====================================================

#define HX711_DOUT 16
#define HX711_SCK  17

#define SERVO_PIN  18
#define BUZZER_PIN 19

// =====================================================
// OBJECTS
// =====================================================

HX711 scale;
WebServer server(80);
Servo feederServo;

// =====================================================
// HX711 CALIBRATION
// =====================================================

// Use your calibrated value here.
// You previously chose approximately -120.
const float CALIBRATION_FACTOR = -461;

// =====================================================
// FOOD SETTINGS
// =====================================================

// This value comes from the user's web application.
// It is NOT hard-coded to 300g.
float foodLimit = 0.0;

// Current food weight measured by HX711
float currentWeight = 0.0;

// =====================================================
// SERVO SETTINGS
// =====================================================

// MG90S continuous rotation servo
const int SERVO_STOP = 90;

// Change this if your servo rotates in the wrong direction.
const int SERVO_FEED = 0;

// =====================================================
// FEEDING STATE
// =====================================================

bool feeding = false;

// =====================================================
// HX711 UPDATE TIMING
// =====================================================

unsigned long lastWeightRead = 0;

const unsigned long WEIGHT_INTERVAL = 200;

// =====================================================
// BUZZER STATE
// =====================================================

bool buzzerActive = false;

int buzzerSound = 0;
int buzzerStep = 0;

unsigned long buzzerTimer = 0;


// =====================================================
// FUNCTION PROTOTYPES
// =====================================================

void addCorsHeaders();

void handleOptions();

void updateWeight();

void handleSetLimit();

void handleFeed();

void handleWeight();

void handleStatus();

void handleRoot();

void startBuzzer();

void updateBuzzer();


// =====================================================
// CORS
// =====================================================

void addCorsHeaders()
{
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.sendHeader(
        "Access-Control-Allow-Methods",
        "GET, POST, OPTIONS"
    );
    server.sendHeader(
        "Access-Control-Allow-Headers",
        "Content-Type"
    );
}


// =====================================================
// OPTIONS REQUEST
// =====================================================

void handleOptions()
{
    addCorsHeaders();

    server.send(
        204,
        "text/plain",
        ""
    );
}


// =====================================================
// UPDATE FOOD WEIGHT
// =====================================================

void updateWeight()
{
    // ---------------------------------------------
    // Check HX711
    // ---------------------------------------------

    if (!scale.is_ready())
    {
        Serial.println("HX711 not ready!");

        // Keep previous weight value.
        return;
    }

    // ---------------------------------------------
    // Read HX711
    // ---------------------------------------------

    float weight = scale.get_units(3);

    // ---------------------------------------------
    // Prevent negative food weight
    // ---------------------------------------------

    if (weight < 0)
    {
        weight = 0;
    }

    currentWeight = weight;

    // ---------------------------------------------
    // Serial Monitor
    // ---------------------------------------------

    Serial.print("Current Weight: ");
    Serial.print(currentWeight, 2);
    Serial.println(" g");


    // =================================================
    // AUTOMATIC FEEDING STOP
    // =================================================

    if (feeding && foodLimit > 0)
    {
        if (currentWeight >= foodLimit)
        {
            Serial.println();
            Serial.println("================================");
            Serial.println("FOOD LIMIT REACHED");
            Serial.println("================================");

            Serial.print("Current Weight: ");
            Serial.print(currentWeight, 2);
            Serial.println(" g");

            Serial.print("Food Limit: ");
            Serial.print(foodLimit, 2);
            Serial.println(" g");

            // -----------------------------------------
            // STOP SERVO
            // -----------------------------------------

            feederServo.write(SERVO_STOP);

            feeding = false;

            Serial.println("Servo STOPPED");
            Serial.println("Feeding completed.");

            // -----------------------------------------
            // PLAY ALL 3 CUTE SOUNDS
            // -----------------------------------------

            startBuzzer();
        }
    }
}


// =====================================================
// SET FOOD LIMIT
// =====================================================
//
// Frontend sends:
//
// {
//     "max_food_amount": 450
// }
//
// =====================================================

void handleSetLimit()
{
    addCorsHeaders();

    Serial.println();
    Serial.println("================================");
    Serial.println("SET FOOD LIMIT REQUEST");
    Serial.println("================================");


    // ---------------------------------------------
    // Check request body
    // ---------------------------------------------

    if (!server.hasArg("plain"))
    {
        Serial.println("ERROR: No request body.");

        server.send(
            400,
            "application/json",
            "{\"success\":false,\"message\":\"No request body\"}"
        );

        return;
    }


    // ---------------------------------------------
    // Get JSON body
    // ---------------------------------------------

    String body = server.arg("plain");

    Serial.print("Received JSON: ");
    Serial.println(body);


    // ---------------------------------------------
    // Find max_food_amount
    // ---------------------------------------------

    int keyPosition =
        body.indexOf("\"max_food_amount\"");


    if (keyPosition == -1)
    {
        Serial.println(
            "ERROR: max_food_amount not found."
        );

        server.send(
            400,
            "application/json",
            "{\"success\":false,\"message\":\"max_food_amount not found\"}"
        );

        return;
    }


    // ---------------------------------------------
    // Find :
    // ---------------------------------------------

    int colonPosition =
        body.indexOf(":", keyPosition);


    if (colonPosition == -1)
    {
        Serial.println("ERROR: Invalid JSON.");

        server.send(
            400,
            "application/json",
            "{\"success\":false,\"message\":\"Invalid JSON\"}"
        );

        return;
    }


    // ---------------------------------------------
    // Find }
    // ---------------------------------------------

    int endPosition =
        body.indexOf("}", colonPosition);


    if (endPosition == -1)
    {
        endPosition = body.length();
    }


    // ---------------------------------------------
    // Extract amount
    // ---------------------------------------------

    String amountString =
        body.substring(
            colonPosition + 1,
            endPosition
        );

    amountString.trim();

    amountString.replace(",", "");
    amountString.replace("\"", "");


    // ---------------------------------------------
    // Convert to float
    // ---------------------------------------------

    float newLimit =
        amountString.toFloat();


    // ---------------------------------------------
    // Validate
    // ---------------------------------------------

    if (newLimit <= 0)
    {
        Serial.println(
            "ERROR: Invalid food amount."
        );

        server.send(
            400,
            "application/json",
            "{\"success\":false,\"message\":\"Invalid food amount\"}"
        );

        return;
    }


    // ---------------------------------------------
    // Save user's food limit
    // ---------------------------------------------

    foodLimit = newLimit;


    Serial.print("New Food Limit: ");
    Serial.print(foodLimit, 2);
    Serial.println(" g");


    // ---------------------------------------------
    // Response
    // ---------------------------------------------

    String response = "{";

    response += "\"success\":true,";
    response += "\"status\":\"limit_updated\",";
    response += "\"limit\":";
    response += String(foodLimit, 2);

    response += "}";


    server.send(
        200,
        "application/json",
        response
    );
}


// =====================================================
// FEED NOW
// =====================================================

void handleFeed()
{
    addCorsHeaders();

    Serial.println();
    Serial.println("================================");
    Serial.println("FEED REQUEST RECEIVED");
    Serial.println("================================");


    // ---------------------------------------------
    // Display current information
    // ---------------------------------------------

    Serial.print("Current Weight: ");
    Serial.print(currentWeight, 2);
    Serial.println(" g");

    Serial.print("Food Limit: ");
    Serial.print(foodLimit, 2);
    Serial.println(" g");


    // ---------------------------------------------
    // Make sure user has set a limit
    // ---------------------------------------------

    if (foodLimit <= 0)
    {
        Serial.println(
            "ERROR: Food limit has not been set."
        );

        server.send(
            400,
            "application/json",
            "{\"success\":false,\"status\":\"limit_not_set\"}"
        );

        return;
    }


    // ---------------------------------------------
    // Check whether already feeding
    // ---------------------------------------------

    if (feeding)
    {
        Serial.println(
            "Already feeding."
        );

        server.send(
            200,
            "application/json",
            "{\"success\":true,\"status\":\"already_feeding\"}"
        );

        return;
    }


    // ---------------------------------------------
    // Check whether food limit already reached
    // ---------------------------------------------

    if (currentWeight >= foodLimit)
    {
        Serial.println(
            "Food limit already reached."
        );

        Serial.println(
            "Servo will NOT start."
        );


        // Play all 3 sounds
        startBuzzer();


        server.send(
            200,
            "application/json",
            "{\"success\":true,\"status\":\"limit_already_reached\"}"
        );

        return;
    }


    // =================================================
    // START FEEDING
    // =================================================
    //
    // IMPORTANT:
    //
    // We DO NOT check scale.is_ready() here.
    //
    // This allows the servo to start even when
    // HX711 is temporarily not ready.
    //
    // =================================================

    feeding = true;


    // ---------------------------------------------
    // Start servo
    // ---------------------------------------------

    feederServo.write(SERVO_FEED);


    Serial.println("Servo STARTED");
    Serial.println(
        "HX711 monitoring continues."
    );


    // ---------------------------------------------
    // Response
    // ---------------------------------------------

    server.send(
        200,
        "application/json",
        "{\"success\":true,\"status\":\"feeding_started\"}"
    );
}


// =====================================================
// GET WEIGHT
// =====================================================
//
// GET:
// http://192.168.1.21/weight
//
// Response:
//
// {
//     "weight": 125.50
// }
//
// =====================================================

void handleWeight()
{
    addCorsHeaders();


    String response = "{\"weight\":";

    response +=
        String(currentWeight, 2);

    response += "}";


    server.send(
        200,
        "application/json",
        response
    );
}


// =====================================================
// GET STATUS
// =====================================================
//
// GET:
// http://192.168.1.21/status
//
// =====================================================

void handleStatus()
{
    addCorsHeaders();


    String response = "{";


    response += "\"feeding\":";

    response +=
        feeding ? "true" : "false";


    response += ",\"weight\":";

    response +=
        String(currentWeight, 2);


    response += ",\"limit\":";

    response +=
        String(foodLimit, 2);


    response += "}";


    server.send(
        200,
        "application/json",
        response
    );
}


// =====================================================
// ROOT PAGE
// =====================================================

void handleRoot()
{
    addCorsHeaders();


    String response = "";


    response +=
        "SMART PET FEEDER ESP32\n";

    response +=
        "-----------------------\n";


    response +=
        "IP: ";

    response +=
        WiFi.localIP().toString();

    response += "\n";


    response +=
        "Weight: ";

    response +=
        String(currentWeight, 2);

    response += " g\n";


    response +=
        "Limit: ";

    response +=
        String(foodLimit, 2);

    response += " g\n";


    response +=
        "Feeding: ";

    response +=
        feeding ? "YES" : "NO";

    response += "\n";


    server.send(
        200,
        "text/plain",
        response
    );
}


// =====================================================
// START 3 CUTE SOUNDS
// =====================================================

void startBuzzer()
{
    // Start from Sound 1
    buzzerSound = 1;

    buzzerStep = 0;

    buzzerTimer = millis();

    buzzerActive = true;


    Serial.println();
    Serial.println(
        "Starting 3 cute buzzer sounds..."
    );
}


// =====================================================
// UPDATE BUZZER
// =====================================================
//
// Sound 1 -> Sound 2 -> Sound 3
//
// This is NON-BLOCKING.
// No delay() is used.
// =====================================================

void updateBuzzer()
{
    if (!buzzerActive)
    {
        return;
    }


    unsigned long now =
        millis();


    // =================================================
    // SOUND 1
    // Cute double beep
    // =================================================

    if (buzzerSound == 1)
    {
        if (buzzerStep == 0)
        {
            tone(
                BUZZER_PIN,
                1000
            );

            buzzerTimer = now;

            buzzerStep = 1;
        }


        else if (
            buzzerStep == 1 &&
            now - buzzerTimer >= 120
        )
        {
            noTone(BUZZER_PIN);

            buzzerTimer = now;

            buzzerStep = 2;
        }


        else if (
            buzzerStep == 2 &&
            now - buzzerTimer >= 80
        )
        {
            tone(
                BUZZER_PIN,
                1400
            );

            buzzerTimer = now;

            buzzerStep = 3;
        }


        else if (
            buzzerStep == 3 &&
            now - buzzerTimer >= 180
        )
        {
            noTone(BUZZER_PIN);


            // -----------------------------------------
            // Move to Sound 2
            // -----------------------------------------

            buzzerSound = 2;

            buzzerStep = 0;

            buzzerTimer = now;
        }
    }


    // =================================================
    // SOUND 2
    // Cute high-low
    // =================================================

    else if (buzzerSound == 2)
    {
        if (buzzerStep == 0)
        {
            tone(
                BUZZER_PIN,
                1500
            );

            buzzerTimer = now;

            buzzerStep = 1;
        }


        else if (
            buzzerStep == 1 &&
            now - buzzerTimer >= 150
        )
        {
            tone(
                BUZZER_PIN,
                800
            );

            buzzerTimer = now;

            buzzerStep = 2;
        }


        else if (
            buzzerStep == 2 &&
            now - buzzerTimer >= 200
        )
        {
            noTone(BUZZER_PIN);


            // -----------------------------------------
            // Move to Sound 3
            // -----------------------------------------

            buzzerSound = 3;

            buzzerStep = 0;

            buzzerTimer = now;
        }
    }


    // =================================================
    // SOUND 3
    // Cute triple beep
    // =================================================

    else if (buzzerSound == 3)
    {
        if (buzzerStep == 0)
        {
            tone(
                BUZZER_PIN,
                1200
            );

            buzzerTimer = now;

            buzzerStep = 1;
        }


        else if (
            buzzerStep == 1 &&
            now - buzzerTimer >= 100
        )
        {
            noTone(BUZZER_PIN);

            buzzerTimer = now;

            buzzerStep = 2;
        }


        else if (
            buzzerStep == 2 &&
            now - buzzerTimer >= 60
        )
        {
            tone(
                BUZZER_PIN,
                1200
            );

            buzzerTimer = now;

            buzzerStep = 3;
        }


        else if (
            buzzerStep == 3 &&
            now - buzzerTimer >= 100
        )
        {
            noTone(BUZZER_PIN);

            buzzerTimer = now;

            buzzerStep = 4;
        }


        else if (
            buzzerStep == 4 &&
            now - buzzerTimer >= 60
        )
        {
            tone(
                BUZZER_PIN,
                1600
            );

            buzzerTimer = now;

            buzzerStep = 5;
        }


        else if (
            buzzerStep == 5 &&
            now - buzzerTimer >= 200
        )
        {
            noTone(BUZZER_PIN);


            buzzerActive = false;


            Serial.println(
                "All 3 cute sounds finished."
            );
        }
    }
}


// =====================================================
// SETUP
// =====================================================

void setup()
{
    // =================================================
    // SERIAL
    // =================================================

    Serial.begin(115200);

    delay(1000);


    Serial.println();
    Serial.println();
    Serial.println(
        "======================================"
    );

    Serial.println(
        "SMART PET FEEDER ESP32"
    );

    Serial.println(
        "======================================"
    );


    // =================================================
    // BUZZER
    // =================================================

    pinMode(
        BUZZER_PIN,
        OUTPUT
    );

    digitalWrite(
        BUZZER_PIN,
        LOW
    );


    // =================================================
    // SERVO
    // =================================================

    feederServo.attach(
        SERVO_PIN
    );


    // Make sure servo is stopped
    feederServo.write(
        SERVO_STOP
    );


    Serial.println(
        "Servo initialized."
    );


    // =================================================
    // HX711
    // =================================================

    scale.begin(
        HX711_DOUT,
        HX711_SCK
    );


    scale.set_scale(
        CALIBRATION_FACTOR
    );


    Serial.println(
        "HX711 initialized."
    );


    Serial.print(
        "Calibration Factor: "
    );

    Serial.println(
        CALIBRATION_FACTOR
    );


    // =================================================
    // TARE
    // =================================================

    delay(2000);


    if (scale.is_ready())
    {
        Serial.println(
            "HX711 is ready."
        );

        Serial.println(
            "Taring load cell..."
        );


        scale.tare();


        Serial.println(
            "Tare completed."
        );
    }

    else
    {
        Serial.println(
            "WARNING: HX711 is NOT ready."
        );

        Serial.println(
            "Servo can still operate."
        );

        Serial.println(
            "Weight monitoring will resume"
        );

        Serial.println(
            "when HX711 becomes ready."
        );
    }


    // =================================================
    // WIFI
    // =================================================

    Serial.println();
    Serial.println(
        "Connecting to WiFi..."
    );


    WiFi.begin(
        WIFI_SSID,
        WIFI_PASSWORD
    );


    while (
        WiFi.status() != WL_CONNECTED
    )
    {
        delay(500);

        Serial.print(".");
    }


    Serial.println();
    Serial.println(
        "WiFi connected!"
    );


    // =================================================
    // DISPLAY IP
    // =================================================

    Serial.print(
        "ESP32 IP Address: "
    );

    Serial.println(
        WiFi.localIP()
    );


    // =================================================
    // API ROUTES
    // =================================================

    // ---------------------------------------------
    // SET LIMIT
    // POST /set-limit
    // ---------------------------------------------

    server.on(
        "/set-limit",
        HTTP_POST,
        handleSetLimit
    );


    server.on(
        "/set-limit",
        HTTP_OPTIONS,
        handleOptions
    );


    // ---------------------------------------------
    // FEED
    // POST /feed
    // ---------------------------------------------

    server.on(
        "/feed",
        HTTP_POST,
        handleFeed
    );


    server.on(
        "/feed",
        HTTP_OPTIONS,
        handleOptions
    );


    // ---------------------------------------------
    // WEIGHT
    // GET /weight
    // ---------------------------------------------

    server.on(
        "/weight",
        HTTP_GET,
        handleWeight
    );


    // ---------------------------------------------
    // STATUS
    // GET /status
    // ---------------------------------------------

    server.on(
        "/status",
        HTTP_GET,
        handleStatus
    );


    // ---------------------------------------------
    // ROOT
    // GET /
    // ---------------------------------------------

    server.on(
        "/",
        HTTP_GET,
        handleRoot
    );


    // =================================================
    // START WEB SERVER
    // =================================================

    server.begin();


    Serial.println();
    Serial.println(
        "ESP32 Web Server started."
    );

    Serial.println(
        "======================================"
    );

    Serial.println(
        "READY!"
    );

    Serial.println(
        "======================================"
    );
}


// =====================================================
// LOOP
// =====================================================

void loop()
{
    // =================================================
    // HANDLE WEB REQUESTS
    // =================================================

    server.handleClient();


    // =================================================
    // READ HX711
    // =================================================

    if (
        millis() - lastWeightRead
        >= WEIGHT_INTERVAL
    )
    {
        lastWeightRead = millis();

        updateWeight();
    }


    // =================================================
    // UPDATE BUZZER
    // =================================================

    updateBuzzer();
}
