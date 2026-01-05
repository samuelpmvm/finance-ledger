package com.fintech.finance.ledger.controller;

import com.api.transaction.TransactionControllerApi;
import com.fintech.finance.ledger.service.TransactionService;
import com.model.transaction.TransactionDto;
import com.model.transaction.TransactionPage;
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
public class TransactionController implements TransactionControllerApi {

    private final TransactionService transactionService;

    @Override
    public ResponseEntity<TransactionDto> createTransaction(TransactionDto transactionDto) {
        var createdTransaction = transactionService.createTransaction(transactionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @Override
    public ResponseEntity<Void> deleteTransactionById(UUID transactionId) {
        transactionService.deleteTransactionById(transactionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteAllTransactions() {
        transactionService.deleteAllTransactions();
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TransactionDto> getTransactionById(UUID transactionId) {
        var transactionDto = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transactionDto);
    }

    @Override
    public ResponseEntity<TransactionPage> getAllTransactions(Integer page, Integer size) {
        var pageNumber = page != null ? page : 0;
        var pageSize = size != null ? size : 10;
        var pageRequest = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(transactionService.getAllTransactions(pageRequest));
    }

    @Override
    public ResponseEntity<TransactionDto> updateTransaction(TransactionDto transactionDto) {
        var updatedTransaction = transactionService.updateTransaction(transactionDto);
        return ResponseEntity.ok(updatedTransaction);
    }
}

