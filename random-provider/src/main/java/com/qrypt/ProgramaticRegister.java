package com.qrypt;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class ProgramaticRegister {
     
    public static void main(String[] args) {

        QRNGRandomProvider provider = new QRNGRandomProvider();
        Security.addProvider(provider);

        try {         
            SecureRandom secureRandom = SecureRandom.getInstance("QRNGSecureRandomSpi", "QRNGRandomProvider");
            byte[] bytes = new byte[1024];       
            secureRandom.nextBytes(bytes);

            // Print result
            String encodedString = Base64.getEncoder().encodeToString(bytes);
            System.out.println(encodedString);

        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        // Use the generated random bytes
    }
}
