package com.emr.gds.features.bone.domain;

public class DexaPersistenceException extends RuntimeException {
    public DexaPersistenceException(String message) {
        super(message);
    }

    public DexaPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
