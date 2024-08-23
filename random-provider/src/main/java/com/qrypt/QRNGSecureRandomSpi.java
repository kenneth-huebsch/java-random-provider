package com.qrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandomSpi;

public class QRNGSecureRandomSpi extends SecureRandomSpi {

    private byte[] SHA512Hash(byte[] bytes){
        final int SHA512_RETURN_LENGTH = 64;

        // List to store all hashed chunks
        byte[] returnValue = new byte[bytes.length];

        try {
            // Get SHA-512 MessageDigest instance
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // Process the input in chunks of 64 bytes
            for (int i = 0; i < bytes.length; i += SHA512_RETURN_LENGTH) {
                // Calculate the chunk size (handle last chunk if smaller than 64 bytes)
                int chunkSize = Math.min(SHA512_RETURN_LENGTH, bytes.length - i);
                
                // Copy the chunk
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(bytes, i, chunk, 0, chunkSize);
                
                // Hash the chunk and add the result to the return value
                byte[] hashedChunk = sha512.digest(chunk);
                System.arraycopy(hashedChunk, 0, returnValue, i, chunkSize);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }

        return returnValue;
    }

    @Override
    protected void engineSetSeed(byte[] seed) {
        System.out.println("EngineSetSeed");
        try { // TODO - Get rid of try/catch block. I want the exception to be thrown      
            QRNGRestAPIClient apiClient = new QRNGRestAPIClient(); 
            
            // throws exception if fails to retrieve random from API
            byte[] decodedBytes = apiClient.getRandom(seed.length);
            if (decodedBytes.length != seed.length){
                throw new RuntimeException("Error: API returned bytes is a different size then the byte array");
            }

            System.arraycopy(decodedBytes, 0, seed, 0, decodedBytes.length);
        }
        catch (Exception e) {}
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        System.out.println("EngineNextBytes");        
        try{  // TODO - Get rid of try/catch block. I want the exception to be thrown  
            QRNGRestAPIClient apiClient = new QRNGRestAPIClient();
            
            // throws exception if fails to retrieve random from API
            byte[] decodedBytes = apiClient.getRandom(bytes.length);
            if (decodedBytes.length != bytes.length){
                throw new RuntimeException("Error: API returned bytes is a different size then the byte array");
            }

            // Use a NIST 800-90A DRBG on the output
            decodedBytes = SHA512Hash(decodedBytes);
            System.arraycopy(decodedBytes, 0, bytes, 0, decodedBytes.length);
        }
        catch (Exception e) {}
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        System.out.println("EngineGenerateSeed");        
        byte[] returnValue = new byte[numBytes];
        this.engineSetSeed(returnValue);

        return returnValue;
    }
}
