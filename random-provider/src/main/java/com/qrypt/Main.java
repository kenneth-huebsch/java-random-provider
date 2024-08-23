package com.qrypt;

import java.security.SecureRandom;

public class Main {
     
    public static void main(String[] args) {

        SecureRandom secureRandom = new SecureRandom();
        System.out.println("Using algorithm: " + secureRandom.getAlgorithm());
        System.out.println("Using provider: " + secureRandom.getProvider().getName());            
        byte[] bytes = new byte[16];       
        secureRandom.nextBytes(bytes);

        // Print result
        //String encodedString = Base64.getEncoder().encodeToString(bytes);
        //System.out.println(encodedString);
    }
}
