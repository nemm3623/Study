package io.github.nemm.study.invertory_reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InvertoryReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvertoryReservationApplication.class, args);
	}

}
