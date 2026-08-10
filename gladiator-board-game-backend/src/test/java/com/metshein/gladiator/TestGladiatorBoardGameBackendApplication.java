package com.metshein.gladiator;

import org.springframework.boot.SpringApplication;

public class TestGladiatorBoardGameBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(GladiatorBoardGameBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
