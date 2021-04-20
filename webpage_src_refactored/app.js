// Global variables for putting markers and drawing lines
let markers = [];
let simplePath=null;

// Initiates the Google Map and adds event listeners to buttons
function initMap(){
	const map = new google.maps.Map(document.getElementById('map'), {
	center: {lat: 55.0, lng: 55.0},
	zoom: 3
		});
		
   map.addListener("click", (event) => {
    addMarker(event.latLng,map);
  });

		
	const geocoder = new google.maps.Geocoder();
	const infowindow = new google.maps.InfoWindow();
	
	document.getElementById("reverse-geocode").addEventListener("click", () => {
		geocodeLatLng(geocoder, map, infowindow);
	});
	
	document.getElementById("city-current-distance").addEventListener("click", () => {
		geocodeCity(geocoder, map, infowindow);
	});
	
	document.getElementById("earth-center-dist-btn").addEventListener("click", () => {
		getDistanceToEarthCenter();
	});
}

//Add marker to the Google Map
function addMarker(location, map) {
  deleteMarkers();
  if (simplePath != null){
	removePath();
  }
  const marker = new google.maps.Marker({
    position: location,
    map: map,
  });
  markers.push(marker);
  document.getElementById("latlng").value= marker.position.lat() + "," + marker.position.lng();
}

//Utility function for clearMarkers funtion
function setMapOnAll(map) {
  for (let i = 0; i < markers.length; i++) {
    markers[i].setMap(map);
  }
}

//Remove the markers from the map, but keep them in array
function clearMarkers() {
  setMapOnAll(null);
}

//Remove all markers permanently
function deleteMarkers() {
  clearMarkers();
  markers = [];
}

// Add a simple drawing to the Google Map
function addPath(map){
	if (simplePath != null){
		removePath();
	}
	simplePath.setMap(map);
}

// Remove a simple drawing from Google Map
function removePath() {
	
	simplePath.setMap(null);
	
}

//Find city information from given coordinates
function geocodeLatLng(geocoder, map, infowindow) {
	
	// Retrieve the coordinates
  let input = document.getElementById("latlng").value;
  let latlngStr = input.split(",", 2);
  
  let latlng = {
    lat: parseFloat(latlngStr[0]),
    lng: parseFloat(latlngStr[1]),
  };
  
  // Check some errornous cases
  if (isNaN(latlng.lat) || latlng.lat === ""){
	  document.getElementById("latlng").value = "Latitude Value is not valid!";
	  return;
  }
  if (isNaN(latlng.lng) || latlng.lng === ""){
	  document.getElementById("latlng").value = "Longitude Value is not valid!";
	  return;
  }
  
  // Add a market for current coordinates
	addMarker(latlng,map);
	map.setZoom(7);
    map.panTo(markers[markers.length-1].position);
	  
	/* Find city from coordinates 
		Results is the returned JSON string from Google API.
		Status is the http status returned from Google API
	*/
  geocoder.geocode({ location: latlng }, (results, status) => {
	  console.log(results);
	  
    if (status === "OK") {
		  if (results[0]) {
			//Extract city name from the JSON
			let city = getCityNameFromJSON(results);
			
			// Create a little box above the marker
			infowindow.setContent(city);
			infowindow.open(map, markers[markers.length-1]);
			document.getElementById("current-city-result").value=city;
		  } else {
			window.alert("No results found");
		  }
    } else {
      document.getElementById("current-city-result").value="There is no city";
    }
  });
  
}

// Utility function to extract city information from JSON
function getCityNameFromJSON(results){
	
	let city = "";
	
	for (let i = 0; i < results.length; i++){
		
		let parts = results[i].address_components;
		// Google API stores city info in "administrative_area_level_1"
		parts.forEach( part => {
			if (part.types.includes("administrative_area_level_1")){
				city = part.long_name;
			}
		});
	}
	
	return city;
}

// This function finds a city center coordinates and calculates the distance between city center and a location
function geocodeCity(geocoder, map, infowindow){
	
	// Get a city name
	let cityname = document.getElementById("current-city-result").value;
	
	/* Find coordinates from city information 
		Results is the returned JSON string from Google API.
		Status is the http status returned from Google API
	*/
	geocoder.geocode({'address': cityname}, (results, status) => {
    if (status === "OK") {
		if (results[0]) {
			// Extract coordinates of the city
		  var cityLatStr = results[0].geometry.location.lat();
		  var cityLngStr = results[0].geometry.location.lng();
		  var lat1 = parseFloat(cityLatStr);
		  var lng1 = parseFloat(cityLngStr);
		  
		  // Current coordinates
		  let coords = document.getElementById("latlng").value;
		  let coordsStr = coords.split(",", 2);
	      var lat2= parseFloat(coordsStr[0]);
	      var lng2= parseFloat(coordsStr[1]);
		  

		  // Calculate the distance between current coordinates and city center
		  var dist = google.maps.geometry.spherical.computeDistanceBetween (new google.maps.LatLng(lat1, lng1), new google.maps.LatLng(lat2, lng2));
		  document.getElementById("city-center-dist").value = dist + " meters";
		  
		  let pathCoords = [
			{ lat: lat1, lng: lng1 },
			{ lat: lat2, lng: lng2 },
			];
			
			// Draw a line between current coordinates and city center
		  simplePath = new google.maps.Polyline({
			 path: pathCoords,
			 geodesic: true,
			 strokeColor: "#000000",
			 strokeOpacity: 1.0,
			 strokeWeight: 2,
		   });
		   
		   addPath(map);
		}
		else {
			window.alert("No results found");
		}
    } else {
      window.alert("Geocoder failed due to: " + status);
    }
  });
}

// This function finds the coordinates from GPS.
function getCurrentLocation(){
	
	
	if('geolocation' in navigator) {
		navigator.geolocation.getCurrentPosition((position) => {
			let str = position.coords.latitude + "," + position.coords.longitude;
			document.getElementById("latlng").value = str;
		});
	}else {
		console.log("Not available");	
	}
	
}

// This function calculates the distance from earth center and given coordinate
 function getDistanceToEarthCenter(){
	 
	 // Get uesr input
	let input = document.getElementById("latlng").value;
	let latlngStr = input.split(",", 2);
	let location = {
		lat: parseFloat(latlngStr[0]),
		lng: parseFloat(latlngStr[1]),
	};
	
	// Find radius of a coordinate
	var earthRadius = getEarthRadiusInMeters(location.lat);
	var altitude;
	const elevator = new google.maps.ElevationService();
	/* Find altitude of a coordinate and calculate the distance from the coordinate to earth center 
		Results is the returned JSON string from Google API.
		Status is the http status returned from Google API
	*/
	elevator.getElevationForLocations({locations: [location]},(results, status) => {
		if (status === "OK" && results) {
			if (results[0]) {
				altitude = results[0].elevation;
				var distance = altitude + earthRadius;
				document.getElementById("earth-center-dist-text").value=distance;
			}
			else{
				document.getElementById("Invalid input").value=distance;
			}
		}
		else {
			document.getElementById("Invalid input").value=distance;
		}
	});
}

// This function finds the horizontal distance between earth center and given coordinate
function getEarthRadiusInMeters(latitudeDegrees)
{
	var pi = Math.PI;
	latitudeRadians = latitudeDegrees * (pi/180);

	// latitudeRadians is geodetic, i.e. that reported by GPS.
	// http://en.wikipedia.org/wiki/Earth_radius
	var a = 6378137.0;  // equatorial radius in meters
	var b = 6356752.3;  // polar radius in meters
	var cos = Math.cos(latitudeRadians);
	var sin = Math.sin(latitudeRadians);
	var t1 = a * a * cos;
	var t2 = b * b * sin;
	var t3 = a * cos;
	var t4 = b * sin;
	return Math.sqrt((t1*t1 + t2*t2) / (t3*t3 + t4*t4));
}





