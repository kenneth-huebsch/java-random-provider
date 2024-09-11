package com.qrypt.randomprovider;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import org.apache.log4j.Logger;

public class Main {

    static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        Provider provider = new QryptProvider();
        Security.insertProviderAt(provider, 1);

        SecureRandom secureRandom = new SecureRandom();

        logger.info("Using algorithm: " + secureRandom.getAlgorithm());
        logger.info("Using provider: " + secureRandom.getProvider().getName());                 
        
        // Test Generate Seed
        byte[] bytes = secureRandom.generateSeed(32);
        String encodedString = Base64.getEncoder().encodeToString(bytes);
        logger.info("*****generateSeed: " + encodedString);
        
        // Test Get Seed
        bytes = SecureRandom.getSeed(32);
        encodedString = Base64.getEncoder().encodeToString(bytes);
        logger.info("*****getSeed: " + encodedString);

        // Test Next Bytes
        for (int i=0; i< 50; i++ ) {
            bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            encodedString = Base64.getEncoder().encodeToString(bytes);
            logger.info("*****nextBytes["+i+"]: " + encodedString);
        }

        //cleanup
        QryptSingleQueueRandomStore.getInstance().destroy();
    }
}
