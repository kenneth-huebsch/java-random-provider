package com.qrypt.randomprovider;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.util.concurrent.ConcurrentLinkedQueue;


public class QRNGSecureRandomSpi extends SecureRandomSpi {
    private static final String DEFAULT_API_URL = "https://api-eus.qrypt.com/api/v1/entropy";
    private static final String DEFAULT_TOKEN = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImY0NjEzODdkOTg2ZjQ2OTliOTQzOGI5MTA1MTYwYTliIn0.eyJleHAiOjE3NTUxOTM5NjUsIm5iZiI6MTcyMzY1Nzk2NSwiaXNzIjoiQVVUSCIsImlhdCI6MTcyMzY1Nzk2NSwiZ3JwcyI6WyJQVUIiXSwiYXVkIjpbIlJQUyJdLCJybHMiOlsiUk5EVVNSIl0sImNpZCI6IjBqX2N0cFF3UW9YT0NkLVhaeEZvRiIsImR2YyI6IjNlM2NlZTBlYjVlMDRjNmZiNjM0OWViZDIxNjFmNGE1IiwianRpIjoiMzM5ZWMzNmVkMTlmNGE2YWI3ZWZkMTFiNGI1YzcxMWMiLCJ0eXAiOjN9.Tr_0vh4u0GpnRUFYjsy0Adg_VckMrhssrzfCrS9wmjNZ6PSk8B0xhinO4TCIKVW3xYn7ztssthmWYCj-pA3_NA";
    //private static final int DEFAULT_QUEUE_SIZE = 500000;

    private ConcurrentLinkedQueue<Byte> randomQueue = new ConcurrentLinkedQueue<>();
    private RandomStore randomStore;

    public QRNGSecureRandomSpi() {
        String apiUrl = System.getProperty("api.url");
        String token = System.getProperty("api.token");
        if (apiUrl == null)
            apiUrl = DEFAULT_API_URL;
        if (token == null)
            token = DEFAULT_TOKEN;

        randomStore = new QryptSingleQueueRandomStore(
                new RestAPIClient(apiUrl, token),null, null
        );
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
