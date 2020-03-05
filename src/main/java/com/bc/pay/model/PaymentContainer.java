package com.bc.pay.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Bell Chen
 * Domain Driven model, designed to be singleton */
public class PaymentContainer {
	
	private static PaymentContainer instance = new PaymentContainer();

	
	//Map that keeps all currency and their amounts, threadsafe
	private static ConcurrentHashMap<String, BigDecimal> paymentMap = new ConcurrentHashMap<String, BigDecimal>();

	private PaymentContainer() {//Construct function disabled
	}
	
	public static PaymentContainer getInstance() {
		return instance;
	}
	
	//Add amount value into the Map, with specific currency and amount in number
	public void addAmountItem(String currency, BigDecimal amount) {

		paymentMap.merge(currency, amount, (original, newAmt) -> original.add(newAmt));
	}
	
	//Add amount value via original line format, e.g. <USD 100>
	public void addAmountItem(String amountLine) {
		try {
			//line format validation
			if(amountLine == null || amountLine.isEmpty()) {
				return;
			}
			if(amountLine.trim().isEmpty()) {
				throw new RuntimeException("Sorry, this is an empty line. :)");
			}
			if(!amountLine.matches("^[A-Z]{3}\\s((\\-?)(([1-9][0-9]{0,14})|([0]{1})|(([0]\\.\\d{1,2}|[1-9][0-9]{0,14}\\.\\d{1,2}))))$")) {
				throw new RuntimeException("Sorry, detected an illegal format: " + amountLine
						+ ", this line would be ignored. The correct format should be like: <USD 100>");
			}
			//Parse currency and amount
			String[] amountLineItems = amountLine.split("\\s");
			addAmountItem(amountLineItems[0], new BigDecimal(amountLineItems[1]));
		}catch(RuntimeException e) {
			throw e;
		}catch(Exception e) {
			throw new RuntimeException("Line could not be read, please check: " + amountLine);
		}
		
	}
	
	public String getPaymentString(Map<String, BigDecimal> exchangeRateMap) {
		if(paymentMap.isEmpty()) return null;
		StringBuffer payments = new StringBuffer("");
		paymentMap.entrySet().forEach(ent ->{
			if(BigDecimal.ZERO.compareTo(ent.getValue()) == 0) {
				return;
			}
			payments.append(ent.getKey() + " " + ent.getValue());
			if(!"USD".equals(ent.getKey()) && exchangeRateMap!= null && exchangeRateMap.containsKey(ent.getKey())) {
				payments.append(" (USD ");
				BigDecimal exchangeRate = exchangeRateMap.get(ent.getKey());
				//ExchangeRate convention
				payments.append(ent.getValue().divide(exchangeRate, 2, BigDecimal.ROUND_HALF_UP));
				payments.append(")");
			}
			payments.append("\n");
		});
		return payments.toString();
	}
	
	public ConcurrentHashMap<String, BigDecimal> getPaymentMap() {
		return paymentMap;
	}

}
