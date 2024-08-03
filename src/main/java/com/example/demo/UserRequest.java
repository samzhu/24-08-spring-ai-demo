package com.example.demo;

public record UserRequest(String message, RequestType type) {
    public enum RequestType {
        PRODUCT_INQUIRY,
        SHIPPING_ISSUE,
        OTHER
    }
}
