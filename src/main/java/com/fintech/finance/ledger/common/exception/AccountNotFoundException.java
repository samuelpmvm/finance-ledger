package com.fintech.finance.ledger.common.exception;

import java.io.Serial;

public class AccountNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 274314356903346438L;

    public AccountNotFoundException(String message) {
        super(message);
    }
}
