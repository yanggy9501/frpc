package com.freeing.rpc.common.exception;

public class RegistryException extends RuntimeException {
    private static final long serialVersionUID = -6783134254669118520L;

    public RegistryException(final Throwable e) {
        super(e);
    }

    public RegistryException(final String message) {
        super(message);
    }

    public RegistryException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
