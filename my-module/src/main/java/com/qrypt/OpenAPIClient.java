package com.qrypt;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class OpenAPIClient {
    private static final String API_URL = "https://api-eus.qrypt.com/api/v1/entropy";
    private static final String TOKEN = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImY0NjEzODdkOTg2ZjQ2OTliOTQzOGI5MTA1MTYwYTliIn0.eyJleHAiOjE3NTUxOTM5NjUsIm5iZiI6MTcyMzY1Nzk2NSwiaXNzIjoiQVVUSCIsImlhdCI6MTcyMzY1Nzk2NSwiZ3JwcyI6WyJQVUIiXSwiYXVkIjpbIlJQUyJdLCJybHMiOlsiUk5EVVNSIl0sImNpZCI6IjBqX2N0cFF3UW9YT0NkLVhaeEZvRiIsImR2YyI6IjNlM2NlZTBlYjVlMDRjNmZiNjM0OWViZDIxNjFmNGE1IiwianRpIjoiMzM5ZWMzNmVkMTlmNGE2YWI3ZWZkMTFiNGI1YzcxMWMiLCJ0eXAiOjN9.Tr_0vh4u0GpnRUFYjsy0Adg_VckMrhssrzfCrS9wmjNZ6PSk8B0xhinO4TCIKVW3xYn7ztssthmWYCj-pA3_NA";

    public byte[] getRandom(int length_in_bytes) {
        byte[] combinedByteArray = new byte[length_in_bytes];

        try {
            // Create an HttpClient
            HttpClient client = HttpClient.newHttpClient();
            String token_header = "Bearer " + TOKEN;
            
            // Create an HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"block_size\":1,\"block_count\":" + String.valueOf(length_in_bytes) + "}"))
                    .header("Accept", "application/json")
                    .header("Authorization", token_header)
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // Parse JSON
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonArray jsonArray = jsonObject.getAsJsonArray("entropy");

            if (jsonArray.size() != length_in_bytes) {
                throw new Exception("Unxpected jsonArray Size");
            }

            // Convert the array of base64 encoded strings into a byte array
            byte[][] byteArrayParts = new byte[length_in_bytes][];
            for (int i = 0; i < length_in_bytes; i++) {
                String base64String = jsonArray.get(i).getAsString();
                byteArrayParts[i] = Base64.getDecoder().decode(base64String);
            }
    
            int currentPosition = 0;
            for (byte[] part : byteArrayParts) {
                System.arraycopy(part, 0, combinedByteArray, currentPosition, part.length);
                currentPosition += part.length;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return combinedByteArray;
    }
}


