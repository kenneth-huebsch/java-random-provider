package com.qrypt.randomprovider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;


public interface APIClient {
    byte[] getRandom(int totalBytes);

    class DefaultImpl implements APIClient {
        private static final Logger logger = Logger.getLogger(APIClient.DefaultImpl.class);
        private final String apiUrl;
        private final String token;
        private static final int MAX_REQUEST_BLOCK_SIZE = 1024;
        private static final int MAX_REQUEST_BLOCK_COUNT = 512;

        // HttpClient is now a class member to reuse the connection pool
        private HttpClient client;

        public DefaultImpl(final String apiUrl, final String token) {
            this.apiUrl = apiUrl;
            this.token = token;
            //...we're forced to opt to lazy-loading due to either race condition or runtime incomplete/partial initialization by the time it's called
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
                try {
                    byte[] bytesFromAPICall = this.callApi(currentBlockSize, currentBlockCount);
                    logger.debug("RestAPIClient: currentBlock=" + currentBlockCount + ",bytesReturned=" + Base64.getEncoder().encodeToString(bytesFromAPICall));
                    int indexToStartCopy = (blockSize * loopCount);
                    //TODO: check with Kenny (port from C++ code?) why only the first 1024 bytes were copied in the original code; changing currentBlockSize to returnValue.length-indexToStartCopy
                    System.arraycopy(bytesFromAPICall, 0, returnValue, indexToStartCopy, /*currentBlockSize*/returnValue.length - indexToStartCopy);

                } catch (RestAPIClientException e) {
                    logger.error("API error occurred: ", e);
                    //TODO: throw unrecoverable exception if we sense misconfig
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
            logger.debug("RestAPIClient: return value=" + Base64.getEncoder().encodeToString(returnValue));

            return returnValue;
        }

        private byte[] callApi(int blockSize, int blockCount) throws RestAPIClientException {
            logger.debug("callApi(blockSize: " + blockSize + ", blockCount: " + blockCount);
            byte[] returnValue = new byte[blockSize * blockCount];
            String responseBody;

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
                    throw new RestAPIClientException("API Error, unexpected status code: " + statusCode);
                }
                responseBody = response.body();

                //
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                JsonArray jsonArray = jsonObject.getAsJsonArray("entropy");

                // throw an exception so the next random provider takes over
                if (jsonArray.size() != blockCount) {
                    throw new RestAPIClientException("Error: Unxpected API return value");
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
                throw new RestAPIClientException("IOException when sending the API request", ioe);
            } catch (InterruptedException ie) {
                throw new RestAPIClientException("InterruptedException when sending the API request", ie);
            }


            return returnValue;
        }


    }

    class RestAPIClientException extends RuntimeException {
        public RestAPIClientException(String message) {
            super(message);
        }

        public RestAPIClientException(String message, Exception e) {
            super(message, e);
        }
    }
}


