package com.fintech.finance.ledger.common.exception;

import lombok.Getter;

import java.text.MessageFormat;

public enum ApiErrorMessage {
    ACCOUNT_NOT_FOUND("Account not found. Check documentation for more details.", "Account not found with Id {0}"),
    CATEGORY_NOT_FOUND("Category not found. Check documentation for more details.", "Category not found with Id {0}");

    @Getter
    private final String error;
    private final String errorMessage;


    ApiErrorMessage(String error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage (Object... args) {
        return new MessageFormat(errorMessage).format(args);
    }

    public String getErrorMessage () {
        return errorMessage;
    }
}
