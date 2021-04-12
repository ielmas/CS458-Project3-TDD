package com.example.GPS_Test;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GpsTestApplicationTests {

	@Before
	void setup() {
		// init driver
	}

	@After
	void tearDown() {
       // quit driver
	}

	@Test
	void tryManualCoordinates() {

	}

	@Test
	void tryInvalidCoordinates() {

	}

	@Test
	void tryGetCurrentCoordinates() {

	}

	@Test
	void tryGetNearestCityDistance() {

	}

	@Test
	void tryGetEarthCenterDistance() {
	}


}
