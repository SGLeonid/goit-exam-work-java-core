package org.forestwizard.goitjavacoreexamproject.http;

public class HttpServiceException extends Exception {
    public HttpServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public HttpServiceException(String msg) {
        super(msg);
    }
}
