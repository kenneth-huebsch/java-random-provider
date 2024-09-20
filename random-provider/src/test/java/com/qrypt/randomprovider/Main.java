package com.qrypt.randomprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        //Provider provider = new QryptProvider();
        //Security.insertProviderAt(provider, 1);

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
