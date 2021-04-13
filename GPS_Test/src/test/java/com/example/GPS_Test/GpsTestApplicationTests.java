package com.example.GPS_Test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GpsTestApplicationTests {

	public WebDriver webDriver;
	public static String hostUrl = "";


	@Before
	void setup() {
		// init driver
		System.setProperty("webdriver.chrome.driver", "com/example/GPS_Test/chromedriver_windows/chromedriver.exe");
		webDriver = new ChromeDriver();
		webDriver.manage().window().maximize();
		webDriver.get(hostUrl);
	}

	@After
	void tearDown() throws InterruptedException {
       // quit driver
		Thread.sleep(250L);
		webDriver.quit();
	}

	@Test
	void tryManualCoordinates() throws InterruptedException {
		tryCity("37.82", "32.59", "Konya");
		tryCity("40.5", "35", "Çorum");
		tryCity("38.7", "28.8", "Manisa");
		tryCity("37.7", "27.1", "Aydın");
		tryCity("38.43", "26.84", "İzmir");
	}

	public void tryCity(String latitude, String longitude, String expectedCity) throws InterruptedException {
		webDriver.findElement(By.id("coordinates")).clear();
		webDriver.findElement(By.id("coordinates")).sendKeys(latitude + ", " + longitude);
		webDriver.findElement(By.id("sendCoordinatesBtn")).click();

		Thread.sleep(100L);

		Assert.assertEquals(webDriver.findElement(By.id("city")).getText(), expectedCity);
	}

	@Test
	void tryInvalidCoordinates() {

	}

	public void tryInvalid(String latitude, String longitude, InvalidType invalidType) {
		webDriver.findElement(By.id("coordinates")).clear();
		webDriver.findElement(By.id("coordinates")).sendKeys(latitude, longitude);
		webDriver.findElement(By.id("sendCoordinatesBtn")).click();



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


	private enum InvalidType {
		INVALID_LATITUDE, INVALID_LONGITUDE
	}
}
