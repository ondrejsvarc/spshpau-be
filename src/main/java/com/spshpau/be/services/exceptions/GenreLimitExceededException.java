package com.spshpau.be.services.exceptions;

public class GenreLimitExceededException extends RuntimeException {
    public GenreLimitExceededException(String message) {
        super(message);
    }
}


