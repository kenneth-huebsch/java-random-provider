package com.qrypt.randomprovider.servlet;

import com.qrypt.randomprovider.QryptProvider;
import com.qrypt.randomprovider.QryptSingleQueueRandomStore;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

@WebListener
public class ProviderPreloadListener implements ServletContextListener {

    private static final String STRONG_ALGS = "securerandom.strongAlgorithms";
    private static final String QRYPT_ALG = "QRNGRestAPI:QryptProvider";
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Force loading of your provider class
            Class.forName("com.qrypt.randomprovider.QryptProvider");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load provider class", e);
        }

        Provider p = new QryptProvider();
        Security.insertProviderAt(p, 1);

        //let's add QryptProvider's QRNGRestAPI to the list of strong algorithms
        String strongAlgs=Security.getProperty(STRONG_ALGS);
        if (strongAlgs==null)
            strongAlgs=QRYPT_ALG;
        else
            strongAlgs=QRYPT_ALG+","+strongAlgs;

        Security.setProperty("securerandom.strongAlgorithms", strongAlgs);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        QryptSingleQueueRandomStore.getInstance().destroy();
    }
}

