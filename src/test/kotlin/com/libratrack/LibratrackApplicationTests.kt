package com.libratrack

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import com.libratrack.LibratrackApplication

@SpringBootTest(classes = [LibratrackApplication::class])
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LibratrackApplicationTests {

	@Test
	fun contextLoads() {
	}

}
