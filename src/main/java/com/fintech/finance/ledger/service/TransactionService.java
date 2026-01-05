package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.common.exception.ApiErrorMessage;
import com.fintech.finance.ledger.common.exception.TransactionNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.mapper.PageDtoMapper;
import com.fintech.finance.ledger.mapper.TransactionMapper;
import com.fintech.finance.ledger.repository.TransactionRepository;
import com.model.transaction.TransactionDto;
import com.model.transaction.TransactionPage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    public TransactionDto createTransaction(TransactionDto transactionDto) {
        var transaction = transactionMapper.toEntity(transactionDto);
        transaction.setTenantId(UserContext.getUserContextData().tenantId());
        LOGGER.info("Creating transaction: {}", transaction);
        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    public TransactionDto getTransactionById(UUID transactionId) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetch Transaction with ID: {}", transactionId);
        var transaction = transactionRepository.findByIdAndTenantId(transactionId, tenantId)
                .orElseThrow(() -> new TransactionNotFoundException(ApiErrorMessage.TRANSACTION_NOT_FOUND.getErrorMessage(transactionId)));
        return transactionMapper.toDto(transaction);
    }

    public TransactionPage getAllTransactions(Pageable pageable) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Fetching all transactions for tenant ID: {}", tenantId);
        var transactionPage = transactionRepository.findAllByTenantId(tenantId, pageable);
        return PageDtoMapper.toTransactionPage(transactionPage.map(transactionMapper::toDto));
    }

    public void deleteAllTransactions() {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Deleting all transactions for tenant ID: {}", tenantId);
        transactionRepository.deleteAllByTenantId(tenantId);
    }

    public void deleteTransactionById(UUID transactionId) {
        var tenantId = UserContext.getUserContextData().tenantId();
        LOGGER.info("Deleting transaction with ID: {} for tenant ID: {}", transactionId, tenantId);
        int deletedCount = transactionRepository.deleteByIdAndTenantId(transactionId, tenantId);
        if (deletedCount == 0) {
            throw new TransactionNotFoundException(ApiErrorMessage.TRANSACTION_NOT_FOUND.getErrorMessage(transactionId));
        }
    }

    public TransactionDto updateTransaction(TransactionDto transactionDto) {
        var tenantId = UserContext.getUserContextData().tenantId();
        var transactionId = transactionDto.getId();
        LOGGER.info("Updating transaction with ID: {} for tenant ID: {}", transactionId, tenantId);
        var existingTransaction = transactionRepository.findByIdAndTenantId(transactionId, tenantId)
                .orElseThrow(() -> new TransactionNotFoundException(ApiErrorMessage.TRANSACTION_NOT_FOUND.getErrorMessage(transactionId)));
        var updatedTransaction = transactionMapper.toEntity(transactionDto);
        updatedTransaction.setTenantId(tenantId);
        updatedTransaction.setId(existingTransaction.getId());
        return transactionMapper.toDto(transactionRepository.save(updatedTransaction));
    }
}

