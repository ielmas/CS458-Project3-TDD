let markers = [];
let simplePath=null;
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

function setMapOnAll(map) {
  for (let i = 0; i < markers.length; i++) {
    markers[i].setMap(map);
  }
}

function clearMarkers() {
  setMapOnAll(null);
}


function deleteMarkers() {
  clearMarkers();
  markers = [];
}

function addPath(map){
	if (simplePath != null){
		removePath();
	}
	simplePath.setMap(map);
}

function removePath() {
	
	simplePath.setMap(null);
	
}



function geocodeLatLng(geocoder, map, infowindow) {
  let input = document.getElementById("latlng").value;
  let latlngStr = input.split(",", 2);
  
  let latlng = {
    lat: parseFloat(latlngStr[0]),
    lng: parseFloat(latlngStr[1]),
  };
  
  if (isNaN(latlng.lat) || latlng.lat === ""){
	  document.getElementById("latlng").value = "Latitude Value is not valid!";
	  return;
  }
  if (isNaN(latlng.lng) || latlng.lng === ""){
	  document.getElementById("latlng").value = "Longitude Value is not valid!";
	  return;
  }
  
	addMarker(latlng,map);
	map.setZoom(7);
    map.panTo(markers[markers.length-1].position);
	  
  geocoder.geocode({ location: latlng }, (results, status) => {
	  console.log(results);
	  
    if (status === "OK") {
		  if (results[0]) {
			//Find city from returned JSON
			let city = getCityNameFromJSON(results);
			infowindow.setContent(city); //results[0].formatted_address
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

function getCityNameFromJSON(results){
	
	let city = "";
	
	for (let i = 0; i < results.length; i++){
		
		let parts = results[i].address_components;
		parts.forEach( part => {
			if (part.types.includes("administrative_area_level_1")){
				city = part.long_name;
			}
		});
	}
	
	return city;
}

function geocodeCity(geocoder, map, infowindow){
	
	let cityname = document.getElementById("current-city-result").value;
	
	
	geocoder.geocode({'address': cityname}, (results, status) => {
    if (status === "OK") {
		if (results[0]) {
		  var cityLatStr = results[0].geometry.location.lat();
		  var cityLngStr = results[0].geometry.location.lng();
		  var lat1 = parseFloat(cityLatStr);
		  var lng1 = parseFloat(cityLngStr);
		  
		  let coords = document.getElementById("latlng").value;
		  let coordsStr = coords.split(",", 2);
	      var lat2= parseFloat(coordsStr[0]);
	      var lng2= parseFloat(coordsStr[1]);
		  

		  
		  var dist = google.maps.geometry.spherical.computeDistanceBetween (new google.maps.LatLng(lat1, lng1), new google.maps.LatLng(lat2, lng2));
		  document.getElementById("city-center-dist").value = dist + " meters";
		  
		  let pathCoords = [
			{ lat: lat1, lng: lng1 },
			{ lat: lat2, lng: lng2 },
			];
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

 function getDistanceToEarthCenter(){
	let input = document.getElementById("latlng").value;
	let latlngStr = input.split(",", 2);
	let location = {
		lat: parseFloat(latlngStr[0]),
		lng: parseFloat(latlngStr[1]),
	};
	var earthRadius = getEarthRadiusInMeters(location.lat);
	var altitude;
	const elevator = new google.maps.ElevationService();
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





