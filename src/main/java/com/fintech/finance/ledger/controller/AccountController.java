package com.fintech.finance.ledger.controller;

import com.api.accounts.AccountControllerApi;
import com.fintech.finance.ledger.service.AccountService;
import com.model.accounts.AccountDto;
import com.model.accounts.AccountPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController implements AccountControllerApi {

    private final AccountService accountService;

    @Override
    public ResponseEntity<AccountDto> createAccount(AccountDto accountDto) {
        var createdAccount = accountService.createAccount(accountDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @Override
    public ResponseEntity<Void> deleteAccountById(UUID accountId) {
        accountService.deleteAccountById(accountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteAllAccounts() {
        accountService.deleteAllAccounts();
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AccountDto> getAccountById(UUID accountId) {
        var accountDto = accountService.getAccountById(accountId);
        return ResponseEntity.ok(accountDto);
    }

    @Override
    public ResponseEntity<AccountPage> getAllAccounts(Integer page, Integer size) {
        var pageNumber = page != null ? page : 0;
        var pageSize = size != null ? size : 10;
        var pageRequest = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(accountService.getAllAccounts(pageRequest));
    }

    @Override
    public ResponseEntity<AccountDto> updateAccount(AccountDto accountDto) {
        var updatedAccount = accountService.createAccount(accountDto);
        return ResponseEntity.ok(updatedAccount);
    }
}
