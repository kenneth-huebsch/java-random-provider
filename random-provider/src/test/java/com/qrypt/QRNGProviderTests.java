package com.qrypt;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

public class QRNGProviderTests {
    @Test
    @DisplayName("Test Provider is properly set and named")
    public void testProviderName() {
        Provider provider = new QryptProvider();
        Assertions.assertEquals("QryptProvider", provider.getName());
    }

    @Test
    @DisplayName("Test Algorithm is properly set and named")
    public void testAlgorithmName() {
        SecureRandom qrng = new SecureRandom();
        Provider provider = new QryptProvider();
        Security.addProvider(provider);
        
        // Test QRNG 
        try {
            qrng = SecureRandom.getInstance("QRNGRestAPI", provider);
            
        }
        catch (Exception e) {}

        Assertions.assertEquals(qrng.getAlgorithm(), "QRNGRestAPI");
    }

    @Test
    @DisplayName("Test engineSetSeed")
    public void testPRNGName() {
        SecureRandom qrng = new SecureRandom();
        Provider provider = new QryptProvider();
        Security.addProvider(provider);
        try {
            qrng = SecureRandom.getInstance("QRNGRestAPI", provider);
        }
        catch (Exception e) {}

        Assertions.assertEquals(qrng.getAlgorithm(), "QRNGRestAPI");
    }    
}
