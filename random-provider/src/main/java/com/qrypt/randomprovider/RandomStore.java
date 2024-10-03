package com.qrypt.randomprovider;

public interface RandomStore {

    void nextBytes(byte[] array);

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
