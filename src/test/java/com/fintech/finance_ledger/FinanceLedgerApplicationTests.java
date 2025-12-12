package com.fintech.finance_ledger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FinanceLedgerApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true);
	}

}
