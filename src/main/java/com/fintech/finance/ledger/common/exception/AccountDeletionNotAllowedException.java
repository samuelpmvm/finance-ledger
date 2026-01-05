package com.fintech.finance.ledger.common.exception;

public class AccountDeletionNotAllowedException extends RuntimeException {
    public AccountDeletionNotAllowedException(String message) {
        super(message);
    }
}
