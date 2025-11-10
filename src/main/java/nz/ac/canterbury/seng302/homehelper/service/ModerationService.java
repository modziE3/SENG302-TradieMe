package nz.ac.canterbury.seng302.homehelper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
public class ModerationService {

    @Value("${perspective.api.key}")
    private String apiKey;

    private final Object lock = new Object();
    private long lastQueryTime = 0;
    private static final long MIN_INTERVAL = 1000;
    private final double PROFANITY_THRESHOLD = 0.3;
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}");

    /**
     * This chunk of code has been assisted using ChatGPT
     * Normalizes the input by removing diacritical marks like macrons.
     * @param text The tag entered by the user
     * @return The normalized text without macron marks
     */
    public String normalizeText(String text){
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
    }

    /**
     * Sends a request to the perspective api for the chance a string contains profanity. It has a lock at the start
     * to avoid multiple queries happening at the same time and also returns an error to the controller if the api
     * call is not successful.
     * @param text The tag entered by the user
     * @return Returns true if probability of profanity is too high (above 0.5) and false if not
     * @throws IOException
     */
    public boolean isProfanity(String text) throws IOException {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            long timeSinceLastQuery = now - lastQueryTime;

            if (timeSinceLastQuery < MIN_INTERVAL) {
                try {
                    Thread.sleep(MIN_INTERVAL - timeSinceLastQuery);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastQueryTime = System.currentTimeMillis();
        }

        String cleanedText = normalizeText(text);
        String url = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;
        String jsonInput = """
        {
        "comment": {"text": "%s"},
        "requestedAttributes": {"PROFANITY": {}},
        "languages": ["en"],
        "doNotStore": true
        }
        """.formatted(cleanedText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(jsonInput, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return findProfanityScore(response.getBody()) > PROFANITY_THRESHOLD;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("An error occurred while validating tag please try again");
        }
    }

    /**
     * Gets the score from the json output response by the perspective api
     * @param jsonOutput The response from a request to perspective api
     * @return The probability that a given string contains profanity
     * @throws JsonProcessingException Throws an error if json response isn't set up correctly
     */
    public double findProfanityScore(String jsonOutput) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonOutput);
        return root.path("attributeScores")
                .path("PROFANITY")
                .path("summaryScore")
                .path("value")
                .asDouble(-1);
    }
}
