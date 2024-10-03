package com.qrypt.randomprovider;
import java.security.SecureRandom;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ExtendWith(MockitoExtension.class)
public class QRNGProviderTests {
    static final Logger logger = LoggerFactory.getLogger(QRNGProviderTests.class);


    @Test
    @DisplayName("Test Provider And Algorithm is properly set and named")
    public void testUsingQryptProviderName() {
        SecureRandom secureRandom = new SecureRandom();

        logger.info("Using algorithm: " + secureRandom.getAlgorithm());
        Assertions.assertEquals("QryptProvider", secureRandom.getProvider().getName());
        Assertions.assertEquals("QRNGRestAPI", secureRandom.getAlgorithm());
    }

//    @Test
//    @DisplayName("Test Algorithm is properly set and named")
//    public void testAlgorithmName() {
//        SecureRandom qrng = new SecureRandom();
//        Provider provider = new QryptProvider();
//        Security.addProvider(provider);
//
//        // Test QRNG
//        try {
//            qrng = SecureRandom.getInstance("QRNGRestAPI", provider);
//
//        }
//        catch (Exception e) {}
//
//        Assertions.assertEquals(qrng.getAlgorithm(), "QRNGRestAPI");
//    }

//    @Test
//    @DisplayName("Test engineSetSeed")
//    public void testPRNGName() {
//        SecureRandom qrng = new SecureRandom();
//        Provider provider = new QryptProvider();
//        Security.addProvider(provider);
//        try {
//            qrng = SecureRandom.getInstance("QRNGRestAPI", provider);
//        }
//        catch (Exception e) {}
//
//        Assertions.assertEquals(qrng.getAlgorithm(), "QRNGRestAPI");
//    }
}
