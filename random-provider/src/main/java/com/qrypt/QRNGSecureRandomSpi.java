package com.qrypt;

import java.security.SecureRandomSpi;

public class QRNGSecureRandomSpi extends SecureRandomSpi {

    @Override
    protected void engineSetSeed(byte[] seed) {
        this.engineNextBytes(seed);
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        QRNGRestAPIClient apiClient = new QRNGRestAPIClient();
        byte[] decodedBytes = apiClient.getRandom(bytes.length);

        // Test for discrepency in lengths
        if (decodedBytes.length != bytes.length){
            System.out.println("Error: API returned bytes is a different size then the byte array");
            // TODO - Fill decoded bytes with system random
        }

        System.arraycopy(decodedBytes, 0, bytes, 0, decodedBytes.length);
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] returnValue = new byte[numBytes];
        this.engineNextBytes(returnValue);

        return returnValue;
    }
}
