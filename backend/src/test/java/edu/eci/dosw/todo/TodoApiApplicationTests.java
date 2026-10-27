package edu.eci.dosw.todo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-secret-for-unit-tests-only-0123456789")
class TodoApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
