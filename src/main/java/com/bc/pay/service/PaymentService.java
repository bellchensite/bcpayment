package com.bc.pay.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bc.pay.model.PaymentContainer;


/***
 * @author Bell Chen
 *
 */
@Service
@ConfigurationProperties(prefix = "payment.service")
public class PaymentService {
	
	private Resource paymentFile;
	
	private Map<String, BigDecimal> exchangeRateMap;
	
	private PaymentContainer container = PaymentContainer.getInstance();

	public void addAmountLine(String line) {
		container.addAmountItem(line);
	}

	//Schedule to print out the payment results in frequency
	@Scheduled(fixedDelayString = "${payment.service.printingInterval}")
	public String printPayments() {
		String result = container.getPaymentString(exchangeRateMap);
		if(result != null)
			System.out.println("|======Payment Results=======\n"+result+"=======Payment Results======|\n");
		return result;
	}
	
	/**
	 * Load Payments from initial payment file
	 * @throws IOException
	 */
	public void loadPaymentFile() throws IOException {
		if(paymentFile == null) {
			System.out.println("Payment file not existing.");
			return;
		}
		List<String> lines = IOUtils.readLines(paymentFile.getInputStream(), "UTF-8");
		if(lines == null || lines.isEmpty()) {
			System.out.println("Payment file is empty.");
			return;
		}
		lines.forEach(line ->{
			container.addAmountItem(line);
		});
		System.out.println("Payment record load from file:");
		printPayments();
	}

	public Resource getPaymentFile() {
		return paymentFile;
	}

	public void setPaymentFile(Resource paymentFile) {
		this.paymentFile = paymentFile;
	}

	public Map<String, BigDecimal> getExchangeRateMap() {
		return exchangeRateMap;
	}

	public void setExchangeRateMap(Map<String, BigDecimal> exchangeRateMap) {
		this.exchangeRateMap = exchangeRateMap;
	}

	public PaymentContainer getContainer() {
		return container;
	}
}
