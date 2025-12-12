package com.fintech.finance_ledger;

import org.springframework.boot.SpringApplication;

public class TestFinanceLedgerApplication {

	public static void main(String[] args) {
		SpringApplication.from(FinanceLedgerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
