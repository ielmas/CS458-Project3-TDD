package com.example.GPS_Test;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GpsTestApplicationTests {

	private WebDriver webDriver;
	private WebDriverWait waiter;
	private String hostUrl = "C:\\Users\\OrduLou\\Documents\\Üniversite\\cs458\\projects\\projects3\\ApplicationLogic\\src\\index.html";
	private final double realLat = 40.9829376;
	private final double realLng = 28.734259199999997;
	private final double epsilon = 0.00000001;

	@BeforeEach
	void setup() throws InterruptedException {
		// init driver
		System.out.println("setup is called");
		System.setProperty("webdriver.chrome.driver", "src/test/java/com/example/GPS_Test/chromedriver_windows/chromedriver.exe");
		webDriver = new ChromeDriver();
		webDriver.manage().window().maximize();
		webDriver.get(hostUrl);
		waiter = new WebDriverWait(webDriver, 10);
		Thread.sleep(100L);
	}

	@AfterEach
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
	void tryInvalidCoordinates() throws InterruptedException {
		tryInvalid("abc", "37.82", InvalidType.INVALID_LATITUDE);
		tryInvalid("37.82", "abc", InvalidType.INVALID_LONGITUDE);
		tryInvalid("", "45", InvalidType.INVALID_LATITUDE);
		tryInvalid("0", "", InvalidType.INVALID_LONGITUDE);
		tryInvalid("", "", InvalidType.INVALID_LATITUDE);
	}

	public void tryInvalid(String latitude, String longitude, InvalidType invalidType) throws InterruptedException {
		webDriver.findElement(By.id("coordinates")).clear();
		webDriver.findElement(By.id("coordinates")).sendKeys(latitude + ", " + longitude);
		webDriver.findElement(By.id("sendCoordinatesBtn")).click();

		Thread.sleep(100L);

		if (invalidType.equals(InvalidType.INVALID_LATITUDE)) {
			Assert.assertEquals(webDriver.findElement(By.id("latitude_error")).getText(), "Latitude Value is not valid!");
		} else {
			Assert.assertEquals(webDriver.findElement(By.id("longitude_error")).getText(), "Longitude Value is not valid!");
		}
	}

	@Test
	void tryGetCurrentCoordinates() throws InterruptedException {
		WebElement getCurLocBut = webDriver.findElement(By.id("getCurLoc"));
		WebElement distanceTextElement = webDriver.findElement(By.id("latlng"));
		String oldLatLong = distanceTextElement.getAttribute("value");


		getCurLocBut.click();
		// waiting until the latitude and longtitude is calculated
		waiter.until(createConditionForValueChange(By.id("latlng"), oldLatLong));

		String latLngOnPage = distanceTextElement.getAttribute("value");
		checkLatLng(latLngOnPage);
		tearDown();
	}


	@Test
	void tryGetNearestCityDistance() {

	}

	@Test
	void tryGetEarthCenterDistance() {

	}
	private ExpectedCondition<Boolean> createConditionForValueChange(By locator, String oldValue){
		return new ExpectedCondition<Boolean>() {
			@NullableDecl
			@Override
			public Boolean apply(@NullableDecl WebDriver webDriver) {
				WebElement element = webDriver.findElement(locator);
				if (element != null && !element.getAttribute("value").equals(oldValue)){
					return true;
				}
				return false;
			}
		};
	}
	private enum InvalidType {
		INVALID_LATITUDE, INVALID_LONGITUDE
	}
	private void checkLatLng(String latLngOnPage) {
		// getting the latitude and longtitude that is on the page
		int commaIndex = latLngOnPage.indexOf(',');
		String currLatStr = latLngOnPage.substring(0, commaIndex);
		String currLngStr = latLngOnPage.substring(commaIndex+1);

		// conversion
		double currLat = Double.parseDouble(currLatStr);
		double currLng = Double.parseDouble(currLngStr);

		// finding the difference
		double latDiff = Math.abs(currLat - realLat);
		double lngDiff = Math.abs(currLng - realLng);

		// checking whether it fullfills the precision
		boolean latDiffIsIgnorable = latDiff < epsilon;
		boolean lngDiffIsIgnorable = lngDiff < epsilon;

		Assert.assertTrue(latDiffIsIgnorable);
		Assert.assertTrue(lngDiffIsIgnorable);
	}
}
