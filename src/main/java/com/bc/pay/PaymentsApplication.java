package com.bc.pay;

import java.io.IOException;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.bc.pay.service.PaymentService;


/**
 * @author Bell Chen
 * @email cwxlk@163.com
 * Built up at 2020 Mar 4
 * SpringBoot main executor
 * */
@SpringBootApplication
@EnableScheduling
public class PaymentsApplication {
	

	public static void main(String[] args) throws IOException {
		ConfigurableApplicationContext context = SpringApplication.run(PaymentsApplication.class, args);
		PaymentService paymentService = context.getBean(PaymentService.class);
		
		//read payments from file
		paymentService.loadPaymentFile();
		
		//read payment from console input
		Scanner sc = new Scanner(System.in);
		System.out.println("You could start to input payments, format <CURRENCY AMOUNT>: ");
		while (true) {
			String line = sc.nextLine();
			if ("quit".equalsIgnoreCase(line)) {
				System.out.println("Shutting down, see you again :)");
				break;
			}
			try {
				paymentService.addAmountLine(line);
			} catch (Exception e) {
				System.err.println("Error reading user input: " + line + "\n" + e.getMessage());
			}
		}
		sc.close();
		SpringApplication.exit(context, () -> 0);
	}

}
