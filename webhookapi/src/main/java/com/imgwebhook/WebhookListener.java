package com.imgwebhook;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.json.JSONObject;
import org.json.JSONException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@RestController
public class WebhookListener {

    private static final String directoryPath = "D:\\Projetos\\Hildermes\\spring-webhook";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @GetMapping("/")
    public ResponseEntity<String> getWebhook() {
        
        String baseUrl = ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(null)  // Removes the path to set it for "/webhook"
            .path("/webhook")   // Append "/webhook" to the base URL
            .build()
            .toUriString();

        return ResponseEntity.ok("<h3>Webhook Listener!</h3>"
                + "<h5>Spring Boot application that can receive incoming Webhook payloads,"
                + " convert to JSON format, and write to a file on disk!</h5>"
                + "<h4>Post payloads to: " + baseUrl + "</h4>");
    }
    
    @GetMapping("/webhook")
    public ResponseEntity<String> getWebhookHelp() {
        return ResponseEntity.ok("<h3>Webhook Listener!</h3>"
                + "<h5>Your payloads must be poste at the body on this route<h5>");
    }
    

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        try {
            JSONObject jsonPayload = new JSONObject(payload);
            String formattedPayload = jsonPayload.toString(4) + System.lineSeparator();
            System.out.println("Received Payload:: " + formattedPayload);

            String timestamp = LocalDateTime.now().format(dtf);
            UUID fileUUID = UUID.randomUUID();
            String fileName = String.format("%s\\webhookPayload_%s_%s.json", directoryPath, fileUUID, timestamp);

            Path path = Paths.get(fileName);
            byte[] strToBytes = formattedPayload.getBytes();
            Files.write(path, strToBytes, CREATE, APPEND);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"success\":\"true\"}");
        } catch (JSONException e) {
            System.out.println("JSON parsing error: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\":\"Invalid JSON format\"}");
        } catch (Exception e) {
            System.out.println("General error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Internal Server Error\"}");
        }
    }
}
