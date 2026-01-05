package com.fintech.finance.ledger.mapper;

import com.fintech.finance.ledger.entity.Transaction;
import com.model.transaction.TransactionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionDto toDto(Transaction transaction);

    Transaction toEntity(TransactionDto dto);
}

