package com.qrypt;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QRNGRestAPITests {

    @Test
    @DisplayName("Test calling the engineSetSeed method")
    public void testEngineSetSeed() {
        QRNGRestAPI qrngSpi = new QRNGRestAPI();
       
        RestAPIClient mockApiClient = Mockito.mock(RestAPIClient.class);
        byte[] expectedAnswer = {2, 2, 2, 2};

        try {
            when(mockApiClient.getRandom(expectedAnswer.length)).thenReturn(expectedAnswer);
        } catch (Exception e) {
            fail("Exception was thrown while mocking getRandom: " + e.getMessage());
        }
        qrngSpi.setRestAPIClient(mockApiClient);

        byte[] testBytes = new byte[expectedAnswer.length];
        qrngSpi.engineSetSeed(testBytes);
        Assertions.assertArrayEquals(expectedAnswer, testBytes);
    }    

    @Test
    @DisplayName("Test calling the engineSetSeed method")
    public void testEngineGenerateSeed() {
        QRNGRestAPI qrngSpi = new QRNGRestAPI();
       
        RestAPIClient mockApiClient = Mockito.mock(RestAPIClient.class);
        byte[] expectedAnswer = {2, 2, 2, 2};

        try {
            when(mockApiClient.getRandom(expectedAnswer.length)).thenReturn(expectedAnswer);
        } catch (Exception e) {
            fail("Exception was thrown while mocking getRandom: " + e.getMessage());
        }
        qrngSpi.setRestAPIClient(mockApiClient);

        byte[] testBytes = qrngSpi.engineGenerateSeed(expectedAnswer.length);
        Assertions.assertArrayEquals(expectedAnswer, testBytes);
    }        
}
