package io.cimpress.tagliatelle.exceptions;

public class UnauthorizedException extends Exception {

    public UnauthorizedException(String error) {
        super(error);
    }
}
