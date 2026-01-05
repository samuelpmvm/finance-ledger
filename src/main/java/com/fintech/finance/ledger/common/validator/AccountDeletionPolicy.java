package com.fintech.finance.ledger.common.validator;

import com.fintech.finance.ledger.common.exception.AccountDeletionNotAllowedException;
import com.fintech.finance.ledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountDeletionPolicy {

    private final TransactionRepository transactionRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDeletionPolicy.class);

    public void validateAccountDeletion(UUID tenantId, UUID accountId) {
        LOGGER.info("Validating deletion policy for account ID: {} under tenant ID: {}", accountId, tenantId);
        boolean hasTransactions = transactionRepository.existsByTenantIdAndAccountId(tenantId, accountId);
        if (hasTransactions) {
            throw new AccountDeletionNotAllowedException("Account with ID: " + accountId + " cannot be deleted as it has associated transactions.");
        }
    }
}
