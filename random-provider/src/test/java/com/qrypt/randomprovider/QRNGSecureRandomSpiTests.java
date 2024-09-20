package com.qrypt.randomprovider;
import static org.mockito.Mockito.*;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

@ExtendWith(MockitoExtension.class)
public class QRNGSecureRandomSpiTests {
    final byte[] simpleExpectedAnswer = {2, 2, 2, 2};
    byte[] simpleExpectedAnswerSHA512 = {2, 2, 2, 2};

    APIClient mockApiClient = Mockito.mock(APIClient.class);

    RandomStore actualRandomStore;
    RandomStore simpleMockRandomStore = new RandomStore() {
        @Override
        public void nextBytes(byte[] array) {
            //copy expectedAnswer into array
            System.arraycopy(simpleExpectedAnswer, 0, array, 0, array.length);
        }

        @Override
        public void destroy() {
        }
    };

    public QRNGSecureRandomSpiTests () {
        actualRandomStore = QryptSingleQueueRandomStore.getInstance(mockApiClient, 1000, 100);
        QRNGSecureRandomSpi.sha512digest(simpleExpectedAnswerSHA512);
    }

    //a private method that returns a byte[] of size 1000 with randomly populated bytes
    private byte[] generateRandomBytes(int size) {
        byte[] randomBytes = new byte[size];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

    @Test
    @DisplayName("Test calling the engineSetSeed method")
    public void testSimpleEngineSetSeed() {
        QRNGSecureRandomSpi qrngSpi = new QRNGSecureRandomSpi();

        qrngSpi.setRandomStore(simpleMockRandomStore);

        byte[] testBytes = new byte[simpleExpectedAnswer.length];
        qrngSpi.engineSetSeed(testBytes);

        Assertions.assertArrayEquals(simpleExpectedAnswerSHA512, testBytes);
    }    

    @Test
    @DisplayName("Test calling the engineSetSeed method")
    public void testSimpleEngineGenerateSeed() {
        QRNGSecureRandomSpi qrngSpi = new QRNGSecureRandomSpi();
        qrngSpi.setRandomStore(simpleMockRandomStore);

        byte[] testBytes = qrngSpi.engineGenerateSeed(simpleExpectedAnswer.length);

        Assertions.assertArrayEquals(simpleExpectedAnswerSHA512, testBytes);
    }

    @Test
    public void testReadCacheSequentialWithMockRestAPI() {

        int currentStoreSize = 1000;
        try {
            when(mockApiClient.getRandom(currentStoreSize))
                    .thenReturn(generateRandomBytes(currentStoreSize));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate random bytes", e);
        }
        QRNGSecureRandomSpi qrngSpi = new QRNGSecureRandomSpi();
        qrngSpi.setRandomStore(actualRandomStore);
        //byte[] res;
        //32x40 > 1000, so somewhere in the middle we should get another cache replenishment
        for (int i=0; i<40; i++) {
            final byte[] res=qrngSpi.engineGenerateSeed(32);
            //I want to ensure res byte[] is of size 32, not all zeroes and not all ones
            Assertions.assertEquals(32, res.length);
            Assertions.assertTrue(java.util.stream.IntStream.range(0, res.length)
                            .mapToObj(j -> res[j])
                            .anyMatch(bYte->bYte!=0 && bYte!=1));
        }

        //Also expect mockApiClient to be called exactly 2 times: once in the beginning and once when the queue is drained
        verify(mockApiClient, times(2)).getRandom(currentStoreSize);
    }


}
