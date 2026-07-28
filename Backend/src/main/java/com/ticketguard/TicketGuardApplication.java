package com.ticketguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketGuardApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketGuardApplication.class, args);
		System.out.println("wellcome to Ticket guard");
	}

}
