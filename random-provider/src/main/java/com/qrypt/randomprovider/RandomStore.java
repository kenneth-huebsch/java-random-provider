package com.qrypt.randomprovider;

public interface RandomStore {
    /**
     * returns next byte from the random store
     * @return
     */
    void nextBytes(byte[] array);

}
