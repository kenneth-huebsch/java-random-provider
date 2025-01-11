package com.qrypt.randomprovider;

public interface RandomStore {
    @Deprecated
    void nextBytes(byte[] array);

    byte[] getBytes(int numBytes);

    void destroy();

    class StorePopulationException extends RuntimeException {
        public StorePopulationException(String message) {
            super(message);
        }
        public StorePopulationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
