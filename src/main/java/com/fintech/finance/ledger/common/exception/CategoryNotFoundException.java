package com.fintech.finance.ledger.common.exception;

import java.io.Serial;

public class CategoryNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 6439854896755197306L;

    public CategoryNotFoundException(String message) {
        super(message);
    }
}

