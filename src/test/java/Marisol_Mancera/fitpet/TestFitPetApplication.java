package Marisol_Mancera.fitpet;

import org.springframework.boot.SpringApplication;

public class TestFitPetApplication {

	public static void main(String[] args) {
		SpringApplication.from(FitPetApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
