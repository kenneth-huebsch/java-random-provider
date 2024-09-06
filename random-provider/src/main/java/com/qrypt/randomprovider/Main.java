package com.qrypt.randomprovider;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class Main {
     
    public static void main(String[] args) {
        Provider provider = new QryptProvider();
        Security.insertProviderAt(provider, 1);


        SecureRandom secureRandom = new SecureRandom();


        System.out.println("Using algorithm: " + secureRandom.getAlgorithm());
        System.out.println("Using provider: " + secureRandom.getProvider().getName());                 
        
        // Test Generate Seed
        byte[] bytes = secureRandom.generateSeed(32);
        String encodedString = Base64.getEncoder().encodeToString(bytes);
        System.out.println("*****generateSeed: " + encodedString);
        
        // Test Get Seed
        bytes = secureRandom.getSeed(32);
        encodedString = Base64.getEncoder().encodeToString(bytes);
        System.out.println("*****getSeed: " + encodedString);

        // Test Next Bytes
        for (int i=0; i< 50; i++ ) {
            bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            encodedString = Base64.getEncoder().encodeToString(bytes);
            System.out.println("*****nextBytes["+i+"]: " + encodedString);
        }
    }
}
