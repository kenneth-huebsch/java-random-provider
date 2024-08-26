package com.qrypt;

import java.security.SecureRandom;
import java.util.Base64;

public class Main {
     
    public static void main(String[] args) {

        SecureRandom secureRandom = new SecureRandom();
        System.out.println("Using algorithm: " + secureRandom.getAlgorithm());
        System.out.println("Using provider: " + secureRandom.getProvider().getName());                 
        
        // Test Generate Seed
        byte[] bytes = secureRandom.generateSeed(32);
        String encodedString = Base64.getEncoder().encodeToString(bytes);
        System.out.println("generateSeed: " + encodedString);
        
        // Test Get Seed
        bytes = SecureRandom.getSeed(32);
        encodedString = Base64.getEncoder().encodeToString(bytes);
        System.out.println("getSeed: " + encodedString);

        // Test Next Bytes
        secureRandom.nextBytes(bytes);
        encodedString = Base64.getEncoder().encodeToString(bytes);
        System.out.println("nextBytes: " + encodedString);
    }
}
