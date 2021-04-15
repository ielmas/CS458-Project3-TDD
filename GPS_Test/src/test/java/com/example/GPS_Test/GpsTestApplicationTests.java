package com.example.GPS_Test;

import java.util.HashMap;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@SpringBootConfiguration
class GpsTestApplicationTests {

	private static WebDriver webDriver;
	private static WebDriverWait waiter;

	private static String hostUrl = "C:\\Users\\OrduLou\\Documents\\Üniversite\\cs458\\projects\\projects3\\ApplicationLogic\\src\\index.html";
	//private static String hostUrl =  "file:///Users/admin/workspace/TestProject3/src/index.html";

	private final String realCity = "İstanbul";
	private final double realDistanceToIstanbulCenter = 20701.604472640214;
	private final double realLat = 40.9829376;
	private final double realLng = 28.734259199999997;
	private final double epsilon = 0.00000001;
	private final double realDstToEarthCenter = 6369044.824489601;



	@BeforeAll
	static void setup() {
		// init driver
		HashMap<String, Object> prefs = new HashMap<>();
		prefs.put("profile.default_content_setting_values.geolocation", 1); // 1:allow 2:block

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", prefs);

		System.setProperty("webdriver.chrome.driver", "src/test/java/com/example/GPS_Test/chromedriver_windows/chromedriver.exe");
		webDriver = new ChromeDriver(options);
		webDriver.manage().window().maximize();
		webDriver.get(hostUrl);
		waiter = new WebDriverWait(webDriver, 20);
	}



	@AfterAll
	static void tearDown() throws InterruptedException {
		// quit driver
		Thread.sleep(250L);
		webDriver.quit();
	}

	@Test
	void tryManualCoordinates() {
		clearThePage();
		tryCity("37.82", "32.59", "Konya");
		tryCity("40.5", "35", "Çorum");
		tryCity("38.7", "28.8", "Manisa");
		tryCity("37.77", "27.15", "Aydın");
		tryCity("38.43", "26.84", "İzmir");
	}

	public void tryCity(String latitude, String longitude, String expectedCity) {
		WebElement latlngElement = webDriver.findElement(By.id("latlng"));
		WebElement getCurCityBtn = webDriver.findElement(By.id("reverse-geocode"));
		WebElement cityTextElement = webDriver.findElement(By.id("current-city-result"));

		String defaultCity = cityTextElement.getAttribute("value");

		latlngElement.clear();
		latlngElement.sendKeys(latitude + ", " + longitude);

		getCurCityBtn.click();

		waiter.until(createConditionForValueChange(By.id("current-city-result"), defaultCity));

		Assert.assertEquals(cityTextElement.getAttribute("value"), expectedCity);
	}

	@Test
	void tryInvalidCoordinates() throws InterruptedException {
		clearThePage();
		tryInvalid("abc", "37.82", InvalidType.INVALID_LATITUDE);
		tryInvalid("37.82", "abc", InvalidType.INVALID_LONGITUDE);
		tryInvalid("", "45", InvalidType.INVALID_LATITUDE);
		tryInvalid("0", "", InvalidType.INVALID_LONGITUDE);
		tryInvalid("", "", InvalidType.INVALID_LATITUDE);
	}

	public void tryInvalid(String latitude, String longitude, InvalidType invalidType) throws InterruptedException {
		clearThePage();
		WebElement latLngElement = webDriver.findElement(By.id("coordinates"));
		WebElement getCurCityBtn = webDriver.findElement(By.id("reverse-geocode"));

		latLngElement.clear();
		latLngElement.sendKeys(latitude + ", " + longitude);

		getCurCityBtn.click();

		if (invalidType.equals(InvalidType.INVALID_LATITUDE)) {
			Assert.assertEquals(webDriver.findElement(By.id("latitude_error")).getAttribute("value"), "Latitude Value is not valid!");
		} else {
			Assert.assertEquals(webDriver.findElement(By.id("longitude_error")).getAttribute("value"), "Longitude Value is not valid!");
		}
	}

	@Test
	void tryNearestCityWithSeaCoordinates(){
		trySeaCoordinates("37.4", "19.6", "There is no city");
		trySeaCoordinates("44.203099933665044", "-34.50106282087862", "");
		trySeaCoordinates("43.608459936060044","-37.20369953962862", "");
		trySeaCoordinates("33.20840421649107","29.52684444782201", "There is no city");
		trySeaCoordinates("0","0", "There is no city");
		trySeaCoordinates("90","0", "There is no city");
	}

	private void trySeaCoordinates(String latitude, String longitude, String expectedText){
		clearThePage();
		WebElement latlngElement = webDriver.findElement(By.id("latlng"));
		WebElement getCurCityBtn = webDriver.findElement(By.id("reverse-geocode"));
		WebElement cityTextElement = webDriver.findElement(By.id("current-city-result"));

		String defaultCity = cityTextElement.getAttribute("value");

		latlngElement.clear();
		latlngElement.sendKeys(latitude + ", " + longitude);

		getCurCityBtn.click();

		waiter.until(createConditionForValueChange(By.id("current-city-result"), defaultCity));

		Assert.assertEquals(cityTextElement.getAttribute("value"), expectedText);
	}
	@Test
	void tryGetCurrentCoordinates() {
		clearThePage();
		WebElement getCurLocBut = webDriver.findElement(By.id("getCurLoc"));
		WebElement latLngTextElement = webDriver.findElement(By.id("latlng"));
		String defaultLatLng = latLngTextElement.getAttribute("value");

		getCurLocBut.click();
		// waiting until the latitude and longtitude is calculated
		waiter.until(createConditionForValueChange(By.id("latlng"), defaultLatLng));

		String latLngOnPage = latLngTextElement.getAttribute("value");
		checkLatLng(latLngOnPage);
	}

	@Test
	void tryGetNearestCityDistanceWithCurrentCoordinates() throws InterruptedException {
		clearThePage();
		WebElement getCurLocBut = webDriver.findElement(By.id("getCurLoc"));
		WebElement getNearestCityCenterDistanceBut = webDriver.findElement(By.id("city-current-distance"));
		WebElement nearestCityTextElement = webDriver.findElement(By.id("city-center-dist"));
		WebElement latLngTextElement = webDriver.findElement(By.id("latlng"));
		WebElement getCurCityBtn = webDriver.findElement(By.id("reverse-geocode"));
		WebElement cityTextElement = webDriver.findElement(By.id("current-city-result"));

		String defaultDistance = nearestCityTextElement.getAttribute("value");
		String defaultLatLng = latLngTextElement.getAttribute("value");
		String defaultCity = cityTextElement.getAttribute("value");

		getCurLocBut.click();
		// waiting until the latitude and longtitude is calculated
		waiter.until(createConditionForValueChange(By.id("latlng"), defaultLatLng));

		getCurCityBtn.click();

		// waiting until the city is shown on the page
		waiter.until(createConditionForValueChange(By.id("current-city-result"), defaultCity));

		getNearestCityCenterDistanceBut.click();
		// waiting until the distance to nearest city center is shown on the page
		waiter.until(createConditionForValueChange(By.id("city-center-dist"), defaultDistance));

		String distanceOnPageStr = nearestCityTextElement.getAttribute("value");
		double distanceOnPage = Double.parseDouble(distanceOnPageStr.substring(0, distanceOnPageStr.indexOf(' ')));

		System.out.println("distance: "  + distanceOnPage);
		boolean distanceIsCorrect = Math.abs(distanceOnPage - realDistanceToIstanbulCenter) < epsilon;
		Assert.assertTrue("Distance to city center: " + distanceOnPage, distanceIsCorrect);
	}
	@Test
	void tryGetNearestCityDistanceWithCurrentCoordinatesWithoutGettingNearestCity() throws InterruptedException {
		clearThePage();
		WebElement getCurLocBut = webDriver.findElement(By.id("getCurLoc"));
		WebElement getNearestCityCenterDistanceBut = webDriver.findElement(By.id("city-current-distance"));
		WebElement nearestCityTextElement = webDriver.findElement(By.id("city-center-dist"));
		WebElement latLngTextElement = webDriver.findElement(By.id("latlng"));

		String defaultDistance = nearestCityTextElement.getAttribute("value");
		String defaultLatLng = latLngTextElement.getAttribute("value");

		getCurLocBut.click();
		// waiting until the latitude and longtitude is calculated
		waiter.until(createConditionForValueChange(By.id("latlng"), defaultLatLng));

		getNearestCityCenterDistanceBut.click();
		Thread.sleep(1000);

		Alert geocoderAlert  = webDriver.switchTo().alert();
		String alertText = geocoderAlert.getText();
		System.out.println(alertText);

		boolean alertIsCorrect = alertText.equals("Geocoder failed due to: ZERO_RESULTS");
		Assert.assertTrue(alertIsCorrect);
	}

	@Test
	void tryGetEarthCenterDistanceWithCurrenCoordinates() {
		clearThePage();
		WebElement getCurLocBut = webDriver.findElement(By.id("getCurLoc"));
		WebElement getDstToEarctCenterBut = webDriver.findElement(By.id("earth-center-dist-btn"));
		WebElement earthCenterDistanceTextElement = webDriver.findElement(By.id("earth-center-dist-text"));
		WebElement latLngTextElement = webDriver.findElement(By.id("latlng"));

		String defaultCenterDistance = earthCenterDistanceTextElement.getAttribute("value");
		String defaultLatLng = latLngTextElement.getAttribute("value");

		getCurLocBut.click();
		// waiting until the latitude and longtitude is calculated.
		waiter.until(createConditionForValueChange(By.id("latlng"), defaultLatLng));

		getDstToEarctCenterBut.click();

		// waiting until the distance is calculated.
		waiter.until(createConditionForValueChange(By.id("earth-center-dist-text"), defaultCenterDistance));

		String distanceOnPageStr = earthCenterDistanceTextElement.getAttribute("value");
		double distanceOnPage = Double.parseDouble(distanceOnPageStr);

		boolean diffIsIgnorable = Math.abs(distanceOnPage - realDstToEarthCenter) < epsilon;

		Assert.assertTrue(diffIsIgnorable);
	}

	@Test
	void tryGetNearestCityDistanceWithManuelCoordinates() {
		tryNearestCityManuel("37.82", "32.59", 10462.844570284435);
		tryNearestCityManuel("40.5", "35", 6798.574666380996);
		tryNearestCityManuel("38.7", "28.8", 119513.7991236266);
		tryNearestCityManuel("37.77", "27.15", 61644.51041191391);
		tryNearestCityManuel("38.43", "26.84", 26418.024861855563);
	}


	@Test
	void tryGetEarthCenterDistanceWithManuelCoordinates() {
		tryEarthCenterDistanceManuel("37.82", "32.59", 6371142.175960994);
		tryEarthCenterDistanceManuel("40.5", "35", 6370111.971642147);
		tryEarthCenterDistanceManuel("38.7", "28.8", 6370566.660799777);
		tryEarthCenterDistanceManuel("37.77", "27.15", 6369951.54910314);
		tryEarthCenterDistanceManuel("38.43", "26.84", 6369863.888128061);
	}

	private void tryNearestCityManuel(String lat, String lng, double realDistanceToCityCenter){
		clearThePage();
		WebElement getNearestCityCenterDistanceBut = webDriver.findElement(By.id("city-current-distance"));
		WebElement nearestCityTextElement = webDriver.findElement(By.id("city-center-dist"));
		WebElement latLngTextElement = webDriver.findElement(By.id("latlng"));
		WebElement getCurCityBtn = webDriver.findElement(By.id("reverse-geocode"));
		WebElement cityTextElement = webDriver.findElement(By.id("current-city-result"));

		String defaultDistance = nearestCityTextElement.getAttribute("value");
		String defaultCity = cityTextElement.getAttribute("value");


		latLngTextElement.clear();
		latLngTextElement.sendKeys(lat+","+lng);
		getCurCityBtn.click();

		// waiting until the city is shown on the page
		waiter.until(createConditionForValueChange(By.id("current-city-result"), defaultCity));

		getNearestCityCenterDistanceBut.click();
		// waiting until the distance to nearest city center is shown on the page
		waiter.until(createConditionForValueChange(By.id("city-center-dist"), defaultDistance));

		String distanceOnPageStr = nearestCityTextElement.getAttribute("value");
		double distanceOnPage = Double.parseDouble(distanceOnPageStr.substring(0, distanceOnPageStr.indexOf(' ')));


		boolean distanceIsCorrect = Math.abs(distanceOnPage - realDistanceToCityCenter) < epsilon;
		Assert.assertTrue("Distance to city center: " + distanceOnPage, distanceIsCorrect);
	}

	private void tryEarthCenterDistanceManuel(String lat, String lng, double distanceToEarthCenter){
		clearThePage();
		WebElement getDstToEarctCenterBut = webDriver.findElement(By.id("earth-center-dist-btn"));
		WebElement earthCenterDistanceTextElement = webDriver.findElement(By.id("earth-center-dist-text"));
		WebElement latLngTextElement = webDriver.findElement(By.id("latlng"));

		String defaultCenterDistance = earthCenterDistanceTextElement.getAttribute("value");

		latLngTextElement.clear();
		latLngTextElement.sendKeys(lat+"," +lng);

		getDstToEarctCenterBut.click();
		// waiting until the latitude and longtitude is calculated.
		waiter.until(createConditionForValueChange(By.id("earth-center-dist-text"), defaultCenterDistance));


		String distanceOnPageStr = earthCenterDistanceTextElement.getAttribute("value");
		double distanceOnPage = Double.parseDouble(distanceOnPageStr);

		boolean diffIsIgnorable = Math.abs(distanceOnPage - distanceToEarthCenter) < epsilon;

		Assert.assertTrue(diffIsIgnorable);
	}

	static void clearThePage(){
		webDriver.get(hostUrl);
		waiter.until(ExpectedConditions.presenceOfElementLocated(By.id("getCurLoc")));
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
