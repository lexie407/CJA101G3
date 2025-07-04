package com.toiukha.forum.article.exception;

public class ImageUploadException extends RuntimeException {
    private final String errorCode;

    public ImageUploadException(String message) {
        super(message);
        this.errorCode = "IMG_READ_FAIL";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
