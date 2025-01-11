package com.qrypt.qrandom.bcprov;

import com.qrypt.randomprovider.QryptNaiveProvider;
import com.qrypt.randomprovider.QryptSingleQueueRandomStore;
import com.qrypt.randomprovider.RandomStore;
import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.crypto.prng.EntropySourceProvider;
import org.bouncycastle.crypto.prng.ThreadedSeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChainedEntropySourceProvider implements EntropySourceProvider {

    static final Logger logger = LoggerFactory.getLogger(ChainedEntropySourceProvider.class.getName());

    private final EntropySourceProvider primary;
    private final EntropySourceProvider fallback;
    //private final int thresholdBytes;

    public ChainedEntropySourceProvider() {
        logger.info("Initializing ChainedEntropySourceProvider....");
        this.primary = new RandomStoreEntropySourceProvider();
        this.fallback = new ThreadedEntropySourceProvider();
        //this.thresholdBytes = thresholdBytes;
    }

    @Override
    public EntropySource get(final int bitsRequired) {
        // The DRBG will ask for bitsRequired bits, i.e., (bitsRequired + 7) / 8 bytes
        final int bytesRequired = (bitsRequired + 7) / 8;

        // We create a "chained" EntropySource that checks primary vs. fallback
        return new EntropySource() {
            @Override
            public boolean isPredictionResistant() {
                return true;
            }

            @Override
            public byte[] getEntropy() {
                // 1. Check if the primary has enough data
                byte[] primaryBytes = primary.get(bitsRequired).getEntropy();
                if (primaryBytes != null && primaryBytes.length >= bytesRequired) {
                    // But we might do a more granular check if the "store" can tell us how many remain
                    //if (primaryBytes.length >= thresholdBytes) {
                        return primaryBytes;
                    //}
                }
                // 2. If primary is insufficient, fallback
                return fallback.get(bitsRequired).getEntropy();
            }

            @Override
            public int entropySize() {
                // Typically just bitsRequired from the DRBG's perspective
                return bitsRequired;
            }
        };
    }


    static class RandomStoreEntropySourceProvider implements EntropySourceProvider {
        private final RandomStore store; // your custom buffer of random bytes

        public RandomStoreEntropySourceProvider() {
            this.store = QryptSingleQueueRandomStore.getInstance();
        }

        @Override
        public EntropySource get(int bitsRequired) {
            final int numBytes = (bitsRequired + 7) / 8;
            return new EntropySource() {
                @Override
                public boolean isPredictionResistant() {
                    return true;
                }

                @Override
                public byte[] getEntropy() {
                    logger.info("getting entropy from RandomStoreSourceProvider: " + numBytes + " bytes");
                    return store.getBytes(numBytes);
                }

                @Override
                public int entropySize() {
                    return bitsRequired;
                }
            };
        }
    }

    private static class ThreadedEntropySourceProvider implements EntropySourceProvider {

        private final ThreadedSeedGenerator seedGenerator;

        ThreadedEntropySourceProvider() {
            this.seedGenerator = new ThreadedSeedGenerator();
        }

        @Override
        public EntropySource get(int bitsRequired) {
            final int numBytes = (bitsRequired + 7) / 8;
            return new EntropySource() {

                @Override
                public boolean isPredictionResistant() {
                    return true; //is it?
                }

                @Override
                public byte[] getEntropy() {
                    logger.info("getting Entropy from ThreadedEntropySourceProvider:" + numBytes + " bytes");
                    return seedGenerator.generateSeed(numBytes, true);
                }

                @Override
                public int entropySize() {
                    return bitsRequired;
                }
            };
        }
    }

}
