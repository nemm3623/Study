package io.github.nemm.study.inventory_reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventoryReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryReservationApplication.class, args);
	}

}
