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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger logger = LoggerFactory.getLogger(WebhookListener.class);
    private static final String directoryPath = "D:\\Projetos\\Hildermes\\spring-webhook"; //Change to your desired path
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
                + "<h5>Your payloads must be posted at the body on this route<h5>");
    }
    

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        try {
            JSONObject jsonPayload = new JSONObject(payload);
            String formattedPayload = jsonPayload.toString(4) + System.lineSeparator();
            logger.info("Received Payload:: {}", formattedPayload);

            String timestamp = LocalDateTime.now().format(dtf);
            UUID fileUUID = UUID.randomUUID();
            String fileName = String.format("%s\\webhookPayload_%s_%s.json", directoryPath, fileUUID, timestamp);

            Path path = Paths.get(fileName);
            byte[] strToBytes = formattedPayload.getBytes();
            Files.write(path, strToBytes, CREATE, APPEND);
            return ResponseEntity.ok().body("{\"success\":\"true\"}");
        } catch (JSONException e) {
        	logger.error("JSON parsing error: ", e);
            return ResponseEntity.badRequest().body("{\"error\":\"Invalid JSON format\"}");
        } catch (Exception e) {
        	logger.error("General error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Internal Server Error\"}");
        }
    }
}
