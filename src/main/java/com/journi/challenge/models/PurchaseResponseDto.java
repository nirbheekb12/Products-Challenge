package com.journi.challenge.models;




public class PurchaseResponseDto {

    private String status;
    private String message;

    public PurchaseResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
