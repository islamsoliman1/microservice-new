package com.programmingtech.inventory_service.exception;

public class InventoryServiceException extends RuntimeException {
    public InventoryServiceException(String message) { super(message); }
    public InventoryServiceException(String message, Throwable cause) { super(message, cause); }
}


