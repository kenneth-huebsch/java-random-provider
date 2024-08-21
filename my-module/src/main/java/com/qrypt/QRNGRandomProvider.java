package com.qrypt;

import java.security.Provider;

public class QRNGRandomProvider extends Provider {
    
    public QRNGRandomProvider() {
        super("QRNGRandomProvider", "1.0", "Custom SecureRandom Provider v1.0");

        // Register the SecureRandom implementation
        putService(new Provider.Service(this, "SecureRandom", "QRNGSecureRandomSpi", QRNGSecureRandomSpi.class.getName(), null, null));
    }
}
