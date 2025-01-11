package com.qrypt.protocols.quantum;

import com.qrypt.randomprovider.QryptSingleQueueRandomStore;
import com.qrypt.randomprovider.RandomStore;

import java.io.IOException;
import java.io.InputStream;

public class QuantumEntropyInputStream extends InputStream {

    private RandomStore cache;

    public QuantumEntropyInputStream() {
        this.cache = QryptSingleQueueRandomStore.getInstance();
    }

    @Override
    public int read() throws IOException {
        // Read a single byte from the cache
        return cache.readByte();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // Read 'len' bytes into the buffer 'b' starting at offset 'off'
        return cache.readBytes(b, off, len);
    }
}