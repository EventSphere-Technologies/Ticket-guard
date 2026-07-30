package com.ticketguard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketGuardApplication extends SpringBootServletInitializer {

	private static final Logger log = LoggerFactory.getLogger(TicketGuardApplication.class);

	/**
	 * Entry point for external servlet container (Tomcat 10) WAR deployment.
	 * Overrides SpringBootServletInitializer#configure() to register the application.
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(TicketGuardApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(TicketGuardApplication.class, args);
		log.info("Welcome to TicketGuard");
	}

}
