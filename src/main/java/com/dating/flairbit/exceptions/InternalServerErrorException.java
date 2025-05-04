package com.dating.flairbit.exceptions;

public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String m) {
        super(m);
    }
}