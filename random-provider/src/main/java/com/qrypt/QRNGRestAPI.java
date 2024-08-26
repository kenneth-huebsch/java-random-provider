package com.qrypt;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;


public class QRNGRestAPI extends SecureRandomSpi {

    private SecureRandom getPRNG() {
        SecureRandom prng = new SecureRandom();
        try {
            prng = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }

        return prng;
    }

    @Override
    protected void engineSetSeed(byte[] seed) {
           
        RestAPIClient apiClient = new RestAPIClient(); 
        byte[] decodedBytes = new byte[seed.length];

        try {
            decodedBytes = apiClient.getRandom(seed.length);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (decodedBytes.length != seed.length) {
            throw new RuntimeException("Error: API returned bytes is a different size then the byte array");
        }

        System.arraycopy(decodedBytes, 0, seed, 0, decodedBytes.length);        
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        this.getPRNG().nextBytes(bytes);      
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {   
        byte[] returnValue = new byte[numBytes];
        this.engineSetSeed(returnValue);

        return returnValue;
    }
}
