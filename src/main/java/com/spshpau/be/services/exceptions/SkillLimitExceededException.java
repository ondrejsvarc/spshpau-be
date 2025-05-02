package com.spshpau.be.services.exceptions;

public class SkillLimitExceededException extends RuntimeException {
    public SkillLimitExceededException(String message) {
        super(message);
    }
}
