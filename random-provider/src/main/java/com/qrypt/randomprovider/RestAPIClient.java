package com.qrypt.randomprovider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

class FailStopException extends RuntimeException {
    public FailStopException(String message) {
        super(message);
    }
    public FailStopException(String message, Exception e) {
        super(message,e);
    }
}

public class RestAPIClient {
    private final String apiUrl;
    private final String token;
    private static final int MAX_REQUEST_BLOCK_SIZE = 1024;
    private static final int MAX_REQUEST_BLOCK_COUNT = 512;

    // HttpClient is now a class member to reuse the connection pool
    private HttpClient client;

    public RestAPIClient(final String apiUrl, final String token) {
        this.apiUrl = apiUrl;
        this.token = token;
        //...initializing it here causes great pain due to uninitialized SSLContexts
        //this.client = HttpClient.newHttpClient();
    }

    private HttpClient getHttpClient() {
        if (client == null)
            client = HttpClient.newHttpClient();
        return client;
    }
    public byte[] getRandom(int totalBytes) {
        if (totalBytes < 0) {
            throw new IllegalArgumentException("Total bytes cannot be negative");
        }

        byte[] returnValue = new byte[totalBytes];

        int blockSize = Math.min(MAX_REQUEST_BLOCK_SIZE, totalBytes);
        int blockCount = Math.min(MAX_REQUEST_BLOCK_COUNT, (int) Math.ceil((double) totalBytes / (double) blockSize));

        int remainingBytes = totalBytes;
        int loopCount = 0;
        final int FAIL_STOP = 999;
        while (remainingBytes > 0 && loopCount <= FAIL_STOP) {
            int currentBlockSize = Math.min(blockSize, remainingBytes);
            int currentBlockCount = Math.min(blockCount, (int) Math.ceil((double) remainingBytes / currentBlockSize));

            // Call the API and copy result into combined array
            //byte[] bytesFromAPICall = this.callApi(currentBlockSize, currentBlockCount);
            try {
                byte[] bytesFromAPICall = this.callApi(currentBlockSize, currentBlockCount);
                System.out.println("RestAPIClient: currentBlock="+currentBlockCount+",bytesReturned="+Base64.getEncoder().encodeToString(bytesFromAPICall));
                int indexToStartCopy = (blockSize * loopCount);
                //TODO: figure out why only the first 1024 bytes were copied in the original code,
                //changing currentBlockSize to returnValue.length-indexToStartCopy
                System.arraycopy(bytesFromAPICall, 0, returnValue, indexToStartCopy, /*currentBlockSize*/returnValue.length-indexToStartCopy);

            } catch (FailStopException e) {
                System.out.println("API error occurred: " + e.getMessage());
                break;
            }

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
        System.out.println("RestAPIClient: return value="+Base64.getEncoder().encodeToString(returnValue));

        return returnValue;
    }

    private byte[] callApi(int blockSize, int blockCount) throws FailStopException {
        System.out.println("callApi(blockSize: " + String.valueOf(blockSize) +", blockCount: " + String.valueOf(blockCount));
        byte[] returnValue = new byte[blockSize * blockCount];
        String responseBody = "";

        if (blockSize == 0 || blockCount == 0) {
            return returnValue;
        }

        String tokenHeader = "Bearer " + this.token;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString("{\"block_size\":" + blockSize + ",\"block_count\":" + blockCount + "}"))
                .header("Accept", "application/json")
                .header("Authorization", tokenHeader)
                .build();

        try {
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            // throw an exception so the next random provider takes over
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                throw new FailStopException("API Error, unexpected status code: " + statusCode);
            }
            responseBody = response.body();

            //
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonArray jsonArray = jsonObject.getAsJsonArray("entropy");

            // throw an exception so the next random provider takes over
            if (jsonArray.size() != blockCount) {
                throw new FailStopException("Error: Unxpected API return value");
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

        } catch (IOException ioe) {
            throw new FailStopException("IOException when sending the API request", ioe);
        } catch (InterruptedException ie) {
            throw new FailStopException("InterruptedException when sending the API request", ie);
        }


        return returnValue;
    }
}


