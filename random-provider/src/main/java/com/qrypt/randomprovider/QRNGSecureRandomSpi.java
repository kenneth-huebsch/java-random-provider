package com.qrypt.randomprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandomSpi;


public class QRNGSecureRandomSpi extends SecureRandomSpi {
    private static final Logger logger = LoggerFactory
            .getLogger(QRNGSecureRandomSpi.class);
    private RandomStore randomStore;

    public QRNGSecureRandomSpi() {
        randomStore = QryptSingleQueueRandomStore.getInstance();
    }

    //useful for testing purposes
    public void setRandomStore(RandomStore randomStore) {
        this.randomStore = randomStore;
    }


    @Override
    public void engineSetSeed(byte[] seed) {
       randomStore.nextBytes(seed);
       //SHA512 hash the seed before the return
        sha512digest(seed);
        logger.info("...Called Qrypt QRNG spi:engineNextBytes ("+seed.length+")...");
    }

    @Override
    public void engineNextBytes(byte[] bytes) {
        randomStore.nextBytes(bytes);
        sha512digest(bytes);
        logger.info("...Called Qrypt QRNG spi:engineNextBytes ("+bytes.length+")...");
    }

    @Override
    public byte[] engineGenerateSeed(int numBytes) {
        byte[] seed = new byte[numBytes];
        engineNextBytes(seed);
        logger.info("...Called Qrypt QRNG spi:engineGenerateSeed ("+numBytes+")...");
        return seed;
    }

    static void sha512digest(byte[] seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            if (seed.length <= 64) {
                // Handle case when seed is less than or equal to 64 bytes
                byte[] hashedSeed = digest.digest(seed);
                System.arraycopy(hashedSeed, 0, seed, 0, seed.length);
            } else {
                // Handle case when seed is greater than 64 bytes
                int offset = 0;
                while (offset < seed.length) {
                    byte[] chunk = digest.digest(seed);
                    int lengthToCopy = Math.min(chunk.length, seed.length - offset);
                    System.arraycopy(chunk, 0, seed, offset, lengthToCopy);
                    offset += chunk.length;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-512 algorithm not found", e);
        }
    }
}
