package com.freeing.rpc.common.exception;

/**
 * @author yanggy
 */
public class SerializerException extends RuntimeException {
    private static final long serialVersionUID = -6783134254669118520L;

    public SerializerException(final Throwable e) {
        super(e);
    }

    public SerializerException(final String message) {
        super(message);
    }

    public SerializerException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
