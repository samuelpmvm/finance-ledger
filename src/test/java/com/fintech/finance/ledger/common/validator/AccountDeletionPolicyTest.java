package com.fintech.finance.ledger.common.validator;

import com.fintech.finance.ledger.common.exception.AccountDeletionNotAllowedException;
import com.fintech.finance.ledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AccountDeletionPolicyTest {

    @InjectMocks
    private AccountDeletionPolicy accountDeletionPolicy;

    @Mock
    private TransactionRepository transactionRepository;

    private UUID tenantId;
    private UUID accountId;

    @BeforeEach
    void setup() {
        tenantId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void validateAccountDeletion_ShouldPass_WhenAccountHasNoTransactions() {
        Mockito.when(transactionRepository.existsByTenantIdAndAccountId(tenantId, accountId))
                .thenReturn(false);

        assertDoesNotThrow(() -> accountDeletionPolicy.validateAccountDeletion(tenantId, accountId));

        Mockito.verify(transactionRepository, Mockito.times(1))
                .existsByTenantIdAndAccountId(tenantId, accountId);
    }

    @Test
    void validateAccountDeletion_ShouldThrowException_WhenAccountHasTransactions() {
        Mockito.when(transactionRepository.existsByTenantIdAndAccountId(tenantId, accountId))
                .thenReturn(true);

        AccountDeletionNotAllowedException exception = assertThrows(
                AccountDeletionNotAllowedException.class,
                () -> accountDeletionPolicy.validateAccountDeletion(tenantId, accountId)
        );

        assertTrue(exception.getMessage().contains(accountId.toString()));
        assertTrue(exception.getMessage().contains("cannot be deleted"));
        assertTrue(exception.getMessage().contains("associated transactions"));
        Mockito.verify(transactionRepository, Mockito.times(1))
                .existsByTenantIdAndAccountId(tenantId, accountId);
    }
}

