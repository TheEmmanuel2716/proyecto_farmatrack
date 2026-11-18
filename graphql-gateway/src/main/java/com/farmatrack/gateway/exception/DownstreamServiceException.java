package com.farmatrack.gateway.exception;

public class DownstreamServiceException extends RuntimeException {

    private final String service;
    private final int httpStatus;
    private final String remoteCode;

    public DownstreamServiceException(
            String service, int httpStatus, String remoteCode, String message, Throwable cause) {
        super(message, cause);
        this.service = service;
        this.httpStatus = httpStatus;
        this.remoteCode = remoteCode;
    }

    public DownstreamServiceException(
            String service, int httpStatus, String remoteCode, String message) {
        this(service, httpStatus, remoteCode, message, null);
    }

    public String getService() {
        return service;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getRemoteCode() {
        return remoteCode;
    }
}
