package com.qrypt.protocols.quantum;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class QuantumURLConnection extends URLConnection {

    protected QuantumURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        // No actual connection is needed since we'll be reading from the cache.
        // If you need to perform initialization, you can do it here.
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // Return an InputStream that reads from your entropy cache
        return new QuantumEntropyInputStream();
    }
}
