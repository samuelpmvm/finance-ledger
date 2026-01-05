package com.fintech.finance.ledger.common.exception;

public class CategoryDeletionNotAllowedException extends RuntimeException {
    public CategoryDeletionNotAllowedException(String message) {
        super(message);
    }
}
