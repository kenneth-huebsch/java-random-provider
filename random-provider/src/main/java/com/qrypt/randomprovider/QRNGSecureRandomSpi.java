package com.qrypt.randomprovider;

import java.security.SecureRandomSpi;


public class QRNGSecureRandomSpi extends SecureRandomSpi {
    //private static final Logger logger = Logger.getLogger(QRNGSecureRandomSpi.class);
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
        //feed it through SHA512 hash before return
    }

    @Override
    public void engineNextBytes(byte[] bytes) {
        randomStore.nextBytes(bytes);
        //feed it through SHA512 hash before return
    }

    @Override
    public byte[] engineGenerateSeed(int numBytes) {
        byte[] seed = new byte[numBytes];
        engineNextBytes(seed);
        return seed;
    }
}
