package com.qrypt;

import java.security.SecureRandomSpi;

public class QRNGSecureRandomSpi extends SecureRandomSpi {

    @Override
    protected void engineSetSeed(byte[] seed) {
        this.engineNextBytes(seed);
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        // Local vars
        OpenAPIClient openapi = new OpenAPIClient();
        int maxRequestSize = 512;
        int totalIterations = (int) Math.ceil((double)bytes.length / (double)maxRequestSize);
        int counter = 0;

        // If we need more then 512, loop through as many times as necessary
        while (counter < totalIterations) {
            int bytesRemaining = bytes.length - (counter * maxRequestSize);
            int bytesToPullInThisIteration = Math.min(bytesRemaining, maxRequestSize);
            byte[] decodedBytes = openapi.getRandom(bytesToPullInThisIteration);

            // Copy the bites
            System.arraycopy(decodedBytes, 0, bytes, (counter * maxRequestSize), bytesToPullInThisIteration);
            counter++;
        }
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] retVal = new byte[numBytes];
        this.engineNextBytes(retVal);

        return retVal;
    }
}
