package com.qrypt.randomprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;

public class QryptSecureRandomProvider extends Provider {

    static final Logger logger = LoggerFactory.getLogger(QryptSecureRandomProvider.class.getName());

    public QryptSecureRandomProvider() {

        super("QryptProvider", "1.0", "SecureRandom Provider v1.0");
        // Register the SecureRandom implementation
        putService(new Provider.Service(this, "SecureRandom", "QryptSecureRandomSpi", QryptSecureRandomSpi.class.getName(), null, null));
        logger.info("...Initialized Qrypt SecureRandom Provider...");
    }
}
