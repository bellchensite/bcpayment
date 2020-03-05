package com.bc.pay.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {
	
	@Autowired
	private PaymentService paymentService;
	
	@Test
	public void testLoadPaymentFile() throws IOException {
		paymentService.loadPaymentFile();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();
		assertTrue(paymentMap != null);
		assertTrue(!paymentMap.isEmpty());
	}
	
	@Test
	public void testAddNewCurreny() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();
		assertTrue(paymentMap != null && paymentMap.isEmpty() );

		//add new currency into map
		paymentService.addAmountLine("USD 1000");
		
		assertTrue(paymentMap.containsKey("USD"));
		
		assertTrue(new BigDecimal("1000").compareTo(paymentMap.get("USD")) == 0);
	}
	
	@Test
	public void testAddPositiveAmountToCurrency() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();
		paymentService.addAmountLine("TWD 1000");

		paymentService.addAmountLine("TWD 200");

		assertTrue(paymentMap.containsKey("TWD"));
		
		assertTrue(new BigDecimal("1200").compareTo(paymentMap.get("TWD")) == 0);
	}
	
	@Test
	public void testAddNegativeAmountToCurrency() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();
		paymentService.addAmountLine("USD 1000");

		//USD 1000 for now, minus $200
		paymentService.addAmountLine("USD -200");

		assertTrue(paymentMap.containsKey("USD"));
		
		assertTrue(new BigDecimal("800").compareTo(paymentMap.get("USD")) == 0);
	}
	
	@Test
	public void testCurrencyDigits() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();
		paymentService.addAmountLine("EUR 1000.91");

		assertTrue(paymentMap.containsKey("EUR"));

		assertTrue(new BigDecimal("1000.91").compareTo(paymentMap.get("EUR")) == 0);

	}

	@Test
	public void testMultiCurrency() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();

		paymentService.addAmountLine("HKD 200");
		paymentService.addAmountLine("CNY 300");
		
		assertTrue(paymentMap.size() == 2);
		assertTrue(paymentMap.containsKey("HKD"));
		
		assertThat(paymentMap, IsMapContaining.hasEntry("HKD", new BigDecimal(200)));
		assertThat(paymentMap, IsMapContaining.hasEntry("CNY", new BigDecimal("300")));

		paymentService.addAmountLine("CNY -150");
		assertTrue(paymentMap.size() == 2);
		assertThat(paymentMap, IsMapContaining.hasEntry("CNY", new BigDecimal("150")));

	}
	
	@Test
	public void testNetToZero() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();

		paymentService.addAmountLine("GBP 200");

		assertTrue(paymentMap.size() == 1);
		assertThat(paymentMap, IsMapContaining.hasEntry("GBP", new BigDecimal("200")));
		
		paymentService.addAmountLine("GBP -200");
		assertTrue(paymentMap.size() == 1);
		assertThat(paymentMap, IsMapContaining.hasEntry("GBP", new BigDecimal("0")));
		
		//test printing content
		assertTrue(paymentService.printPayments().isEmpty());
	}
	
	@Test
	public void testPrinting() {
		clearPaymentMap();
		assertNull(paymentService.printPayments());

		paymentService.addAmountLine("SGD 200");
		String result = paymentService.printPayments();
		
		assertTrue(result.indexOf("SGD 200") == 0);
		
		paymentService.addAmountLine("CNY 100");
		result = paymentService.printPayments();
		
		assertTrue(result.indexOf("CNY 100") > 0);
	}
	
	@Test
	public void testExchangeRate() {
		clearPaymentMap();
		assertNull(paymentService.printPayments());
		
		paymentService.addAmountLine("CNY 100");
		String result = paymentService.printPayments();
		
		assertTrue(result.indexOf("CNY 100") == 0);

		Map<String, BigDecimal> exchangeRateMap = paymentService.getExchangeRateMap();
		assertThat(exchangeRateMap, IsMapContaining.hasKey("CNY"));

		BigDecimal cnyExRate = exchangeRateMap.get("CNY");
		BigDecimal usdAmount = new BigDecimal(100).divide(cnyExRate, 2, BigDecimal.ROUND_HALF_UP);
		
		assertTrue(result.indexOf("CNY 100 (USD " + usdAmount + ")") == 0);
	}
	
	@Test
	public void testInvalidCurrency() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();

		String wrongformat = "USd 1000.91";
		assertThrows(RuntimeException.class, () -> {paymentService.addAmountLine(wrongformat);}, 
				"Sorry, detected an illegal format: " + wrongformat 
										+ ", this line would be ignored. The correct format should be like: USD 100") ;
		assertTrue(paymentMap != null && paymentMap.isEmpty() );
		
	}

	@Test
	public void testInvalidInputFormat() {
		clearPaymentMap();
		Map<String, BigDecimal> paymentMap = paymentService.getContainer().getPaymentMap();

		String wrongformat = "USD1000";
		assertThrows(RuntimeException.class, () -> {paymentService.addAmountLine(wrongformat);}, 
				"Sorry, detected an illegal format: " + wrongformat 
										+ ", this line would be ignored. The correct format should be like: <USD 100>") ;
		assertTrue(paymentMap != null && paymentMap.isEmpty() );
	}
	
	private void clearPaymentMap() {//PaymentContainer designed to be singleton, need to clear up
		paymentService.getContainer().getPaymentMap().clear();
	}
}
