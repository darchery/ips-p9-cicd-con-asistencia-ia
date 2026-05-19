package com.uma.example.springuma.model;

/**
 * Exception thrown when an external API returns an error or unexpected response.
 */
public class ApiException extends Exception {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
