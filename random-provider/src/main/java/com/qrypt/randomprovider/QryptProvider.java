package com.qrypt.randomprovider;

import java.security.Provider;
import org.apache.log4j.Logger;

public class QryptProvider extends Provider {
    static final Logger logger = Logger.getLogger(QryptProvider.class);

    public QryptProvider() {

        super("QryptProvider", "1.0", "SecureRandom Provider v1.0");
        // Register the SecureRandom implementation
        putService(new Provider.Service(this, "SecureRandom", "QRNGRestAPI", QRNGSecureRandomSpi.class.getName(), null, null));
        logger.debug("Initialized Qrypt Provider");
    }
}
