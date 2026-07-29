package com.ticketguard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketGuardApplication {

	private static final Logger log = LoggerFactory.getLogger(TicketGuardApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TicketGuardApplication.class, args);
		log.info("Welcome to TicketGuard");
	}

}
