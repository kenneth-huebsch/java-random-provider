package com.qrypt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class QRNGRestAPIClient {
    private static final String API_URL = "https://api-eus.qrypt.com/api/v1/entropy";
    private static final String TOKEN = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImY0NjEzODdkOTg2ZjQ2OTliOTQzOGI5MTA1MTYwYTliIn0.eyJleHAiOjE3NTUxOTM5NjUsIm5iZiI6MTcyMzY1Nzk2NSwiaXNzIjoiQVVUSCIsImlhdCI6MTcyMzY1Nzk2NSwiZ3JwcyI6WyJQVUIiXSwiYXVkIjpbIlJQUyJdLCJybHMiOlsiUk5EVVNSIl0sImNpZCI6IjBqX2N0cFF3UW9YT0NkLVhaeEZvRiIsImR2YyI6IjNlM2NlZTBlYjVlMDRjNmZiNjM0OWViZDIxNjFmNGE1IiwianRpIjoiMzM5ZWMzNmVkMTlmNGE2YWI3ZWZkMTFiNGI1YzcxMWMiLCJ0eXAiOjN9.Tr_0vh4u0GpnRUFYjsy0Adg_VckMrhssrzfCrS9wmjNZ6PSk8B0xhinO4TCIKVW3xYn7ztssthmWYCj-pA3_NA";
    private static final int MAX_REQUEST_BLOCK_SIZE = 1024;
    private static final int MAX_REQUEST_BLOCK_COUNT = 512;
    
    private byte[] callApi(int blockSize, int blockCount) throws RuntimeException, IOException, InterruptedException{
        System.out.println("callApi(blockSize: " + String.valueOf(blockSize) +", blockCount: " + String.valueOf(blockCount));
        byte[] returnValue = new byte[blockSize * blockCount];
        String responseBody = "";
                
        if (blockSize == 0 || blockCount == 0){
            return returnValue;
        }

        HttpClient client = HttpClient.newHttpClient();
        String tokenHeader = "Bearer " + TOKEN;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{\"block_size\":" + String.valueOf(blockSize) + ",\"block_count\":" + String.valueOf(blockCount) + "}"))
                .header("Accept", "application/json")
                .header("Authorization", tokenHeader)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // throw an exception so the next random provider takes over
        int statusCode = response.statusCode();
        if (statusCode != 200) {
            throw new RuntimeException("API Error returned code: " + String.valueOf(statusCode));
        }
        responseBody = response.body();

         // Parse JSON
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        JsonArray jsonArray = jsonObject.getAsJsonArray("entropy");

        // throw an exception so the next random provider takes over
        if (jsonArray.size() != blockCount) {
            throw new RuntimeException("Error: Unxpected API return value");
        }

        // Convert the array of base64 encoded strings into a byte array
        byte[][] byteArrayParts = new byte[blockCount][];
        for (int i = 0; i < blockCount; i++) {
            String base64String = jsonArray.get(i).getAsString();
            byteArrayParts[i] = Base64.getDecoder().decode(base64String);
        }

        int currentPosition = 0;
        for (byte[] part : byteArrayParts) {
            System.arraycopy(part, 0, returnValue, currentPosition, part.length);
            currentPosition += part.length;
        }
        
        return returnValue;
    }

    public byte[] getRandom(int totalBytes) throws RuntimeException, IOException, InterruptedException {
        byte[] returnValue = new byte[totalBytes];

        int blockSize = Math.min(MAX_REQUEST_BLOCK_SIZE, totalBytes);
        int blockCount = Math.min(MAX_REQUEST_BLOCK_COUNT, (int) Math.ceil((double) totalBytes / (double) blockSize));

        int remainingBytes = totalBytes;
        int loopCount = 0;
        final int FAIL_STOP = 999;
        while (remainingBytes > 0 && loopCount <= FAIL_STOP ) {
            int currentBlockSize = Math.min(blockSize, remainingBytes);
            int currentBlockCount = Math.min(blockCount, (int) Math.ceil((double) remainingBytes / currentBlockSize));

            // Call the API and copy result into combined array
            byte[] bytesFromAPICall = this.callApi(currentBlockSize, currentBlockCount);
            int indexToStartCopy = (blockSize * loopCount);
            System.arraycopy(bytesFromAPICall, 0, returnValue, indexToStartCopy, currentBlockSize);

            // Update remaining request
            remainingBytes -= currentBlockSize * currentBlockCount;

            // If there's more to fetch, recalculate block_size and block_count
            if (remainingBytes > 0) {
                blockSize = Math.min(MAX_REQUEST_BLOCK_SIZE, remainingBytes);
                blockCount = Math.min(MAX_REQUEST_BLOCK_COUNT, (int) Math.ceil((double) remainingBytes / blockSize));
            }

            // Prevent the loop from running away
            if (loopCount >= FAIL_STOP) {
                throw new RuntimeException("Error: Loop hit failstop");
            }
            ++loopCount;
        }

        return returnValue;
    }
}


