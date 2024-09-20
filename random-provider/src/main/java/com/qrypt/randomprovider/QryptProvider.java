package com.qrypt.randomprovider;

import java.security.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QryptProvider extends Provider {

    static final Logger logger = LoggerFactory.getLogger(QryptProvider.class.getName());

    public QryptProvider() {

        super("QryptProvider", "1.0", "SecureRandom Provider v1.0");
        // Register the SecureRandom implementation
        putService(new Provider.Service(this, "SecureRandom", "QRNGRestAPI", QRNGSecureRandomSpi.class.getName(), null, null));
        logger.info("...Initialized Qrypt Provider...");
        //System.out.println("...Initialized com.qrypt.randomprovider.QryptProvider...");
    }
}
