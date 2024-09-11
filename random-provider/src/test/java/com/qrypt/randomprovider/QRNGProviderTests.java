package com.qrypt.randomprovider;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class QRNGProviderTests {
    static final Logger logger = Logger.getLogger(QRNGProviderTests.class);


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
