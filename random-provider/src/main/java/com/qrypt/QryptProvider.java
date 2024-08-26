package com.qrypt;

import java.security.Provider;

public class QryptProvider extends Provider {
    
    public QryptProvider() {
        super("Qrypt", "1.0", "SecureRandom Provider v1.0");

        // Register the SecureRandom implementation
        putService(new Provider.Service(this, "SecureRandom", "QRNGRestAPI", QRNGRestAPI.class.getName(), null, null));
    }
}
