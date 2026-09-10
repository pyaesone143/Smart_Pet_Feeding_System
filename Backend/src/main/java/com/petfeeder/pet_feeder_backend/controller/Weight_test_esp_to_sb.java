package com.petfeeder.pet_feeder_backend.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class Weight_test_esp_to_sb {
	private final RestClient restClient;

     // ESP32 URL
    private static final String ESP32_URL =
            "http://10.245.156.214";

public Weight_test_esp_to_sb(RestClient.Builder builder) {
    this.restClient = builder.build();
}

@GetMapping("/api/weight")
public String getWeightFromESP32() {


    String response = restClient.get()
            .uri(ESP32_URL + "/weight")
            .retrieve()
            .body(String.class);

    System.out.println("================================");
    System.out.println("Weight received from ESP32:");
    System.out.println(response);
    System.out.println("================================");

    return response;
}

}
