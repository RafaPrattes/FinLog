package com.ucb.finlog;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;


import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinlogBackendApplicationTests {

	@Test
	void applicationClassExists() {
		assertNotNull(FinlogBackendApplication.class);
	}

	@Test
	void deveInstanciarAplicacao() {
		assertNotNull(new FinlogBackendApplication());
	}

	@Test
	void mainDeveDelegarParaSpringApplicationRun() {
		String[] args = {"--server.port=0"};

		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			FinlogBackendApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(FinlogBackendApplication.class, args));
		}
	}
}
