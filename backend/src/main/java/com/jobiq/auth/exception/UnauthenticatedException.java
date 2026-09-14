package com.jobiq.auth.exception;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("Unauthenticated");
    }
}
