package com.fintech.finance.ledger.service;

import com.fintech.finance.ledger.TenantTestExtension;
import com.fintech.finance.ledger.common.exception.TransactionNotFoundException;
import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.entity.Transaction;
import com.fintech.finance.ledger.mapper.TransactionMapper;
import com.fintech.finance.ledger.repository.TransactionRepository;
import com.model.transaction.TransactionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, TenantTestExtension.class})
@Tag("unit")
class TransactionServiceTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(150.50);
    private static final LocalDate DATE = LocalDate.of(2026, 1, 5);
    private static final String DESCRIPTION = "Grocery shopping";
    private static final boolean IMPORTED = false;

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

    @BeforeEach
    void setup() {
        transactionService = new TransactionService(transactionRepository, transactionMapper);
    }

    @Test
    void createTransactionSuccess() {
        var transactionDto = createTransactionDto();
        ArgumentCaptor<Transaction> transactionEntityArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        transactionService.createTransaction(transactionDto);
        Mockito.verify(transactionRepository, Mockito.times(1)).save(transactionEntityArgumentCaptor.capture());
        var transaction = transactionEntityArgumentCaptor.getValue();
        assertEquals(ACCOUNT_ID, transaction.getAccountId());
        assertEquals(AMOUNT, transaction.getAmount());
        assertEquals(DATE, transaction.getDate());
        assertEquals(DESCRIPTION, transaction.getDescription());
        assertEquals(CATEGORY_ID, transaction.getCategoryId());
        assertEquals(IMPORTED, transaction.isImported());
    }

    @Test
    void getTransactionByIdSuccess() {
        var transactionId = UUID.randomUUID();
        var transaction = getTransaction();
        Mockito.when(transactionRepository.findByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.of(transaction));
        var transactionDto = transactionService.getTransactionById(transactionId);
        assertEquals(ACCOUNT_ID, transactionDto.getAccountId());
        assertEquals(AMOUNT.doubleValue(), transactionDto.getAmount());
        assertEquals(DATE, transactionDto.getDate());
        assertEquals(DESCRIPTION, transactionDto.getDescription());
    }

    @Test
    void getTransactionByIdFails() {
        var transactionId = UUID.randomUUID();
        Mockito.when(transactionRepository.findByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.empty());
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById(transactionId));
    }

    @Test
    void getAllTransactionsSuccess() {
        Page<Transaction> page = new PageImpl<>(List.of(getTransaction()), PageRequest.of(0, 10), 1);
        Mockito.when(transactionRepository.findAllByTenantId(ArgumentMatchers.eq(UserContext.getUserContextData().tenantId()), ArgumentMatchers.any())).thenReturn(page);
        var transactionPage = transactionService.getAllTransactions(Mockito.mock(Pageable.class));
        Mockito.verify(transactionRepository, Mockito.times(1)).findAllByTenantId(ArgumentMatchers.eq(UserContext.getUserContextData().tenantId()), ArgumentMatchers.any());
        assertEquals(1, transactionPage.getTotalElements());
        assertEquals(1, transactionPage.getTotalPages());

        var transactionDto = transactionPage.getContent().get(0);
        assertEquals(ACCOUNT_ID, transactionDto.getAccountId());
        assertEquals(AMOUNT.doubleValue(), transactionDto.getAmount());
    }

    @Test
    void testDeleteTransactionByIdSuccess() {
        var transactionId = UUID.randomUUID();
        Mockito.when(transactionRepository.deleteByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId())).thenReturn(1);
        transactionService.deleteTransactionById(transactionId);
        Mockito.verify(transactionRepository, Mockito.times(1)).deleteByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId());
    }

    @Test
    void testDeleteTransactionByIdFails() {
        var transactionId = UUID.randomUUID();
        Mockito.when(transactionRepository.deleteByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId())).thenReturn(0);
        assertThrows(TransactionNotFoundException.class, () -> transactionService.deleteTransactionById(transactionId));
        Mockito.verify(transactionRepository, Mockito.times(1)).deleteByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId());
    }

    @Test
    void testDeleteAllTransactions() {
        transactionService.deleteAllTransactions();
        Mockito.verify(transactionRepository, Mockito.times(1)).deleteAllByTenantId(UserContext.getUserContextData().tenantId());
    }

    @Test
    void testUpdateTransactionSuccess() {
        var transactionId = UUID.randomUUID();
        var existingTransaction = getTransaction();
        existingTransaction.setId(transactionId);
        Mockito.when(transactionRepository.findByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.of(existingTransaction));
        var updatedTransactionDto = new TransactionDto();
        updatedTransactionDto.setId(transactionId);
        updatedTransactionDto.setAccountId(ACCOUNT_ID);
        updatedTransactionDto.setAmount(200.0);
        updatedTransactionDto.setDate(LocalDate.of(2026, 1, 6));
        updatedTransactionDto.setDescription("Updated description");
        updatedTransactionDto.setCategoryId(CATEGORY_ID);
        updatedTransactionDto.setImported(true);

        transactionService.updateTransaction(updatedTransactionDto);

        ArgumentCaptor<Transaction> transactionEntityArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        Mockito.verify(transactionRepository, Mockito.times(1)).save(transactionEntityArgumentCaptor.capture());
        var savedTransaction = transactionEntityArgumentCaptor.getValue();
        assertEquals(BigDecimal.valueOf(200.0), savedTransaction.getAmount());
        assertEquals(LocalDate.of(2026, 1, 6), savedTransaction.getDate());
        assertEquals("Updated description", savedTransaction.getDescription());
        assertTrue(savedTransaction.isImported());
    }

    @Test
    void testUpdateTransactionFails() {
        var transactionId = UUID.randomUUID();
        Mockito.when(transactionRepository.findByIdAndTenantId(transactionId, UserContext.getUserContextData().tenantId())).thenReturn(Optional.empty());
        var updatedTransactionDto = new TransactionDto();
        updatedTransactionDto.setId(transactionId);
        assertThrows(TransactionNotFoundException.class, () -> transactionService.updateTransaction(updatedTransactionDto));
    }

    private TransactionDto createTransactionDto() {
        var dto = new TransactionDto();
        dto.setAccountId(ACCOUNT_ID);
        dto.setAmount(AMOUNT.doubleValue());
        dto.setDate(DATE);
        dto.setDescription(DESCRIPTION);
        dto.setCategoryId(CATEGORY_ID);
        dto.setImported(IMPORTED);
        return dto;
    }

    private Transaction getTransaction() {
        var transaction = new Transaction();
        transaction.setAccountId(ACCOUNT_ID);
        transaction.setAmount(AMOUNT);
        transaction.setDate(DATE);
        transaction.setDescription(DESCRIPTION);
        transaction.setCategoryId(CATEGORY_ID);
        transaction.setImported(IMPORTED);
        return transaction;
    }
}

