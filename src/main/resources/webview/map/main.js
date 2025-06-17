function getExports() { return { showLocations, showLineage, showAncestors, showDescendants }; }

const COLOR = "#FF2262";
const OPACITY = 0.7;
const CIRCLE_MARKER_RADIUS = 10;

const ANIMATION_YEARS_PER_SECOND = 40.0;

let map = null;

let bounds = null;
let container = null;

let animation = null;
let animationTimer = null;
let yearLabel = document.getElementById("yearLabel");

addEventListener("DOMContentLoaded", () => { addMap(); });

function addMap() {
	map = L.map("map");

	let mapBoxLayer = L.tileLayer("https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}", {
		attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> | &copy; <a href="https://www.mapbox.com">Mapbox</a>',
		maxZoom: 18,
		id: "mapbox/streets-v11",
		tileSize: 512,
		zoomOffset: -1,
		accessToken: "pk.eyJ1IjoiY2JldHRpbmdlciIsImEiOiJja2Q0cnc3aGUxdTF1MnNvNzRnN2pvbmpxIn0.wDahEhYH1fzw7OmTrs9OZw"
	}).addTo(map);

	let osmLayer = L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
		maxZoom: 19,
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
	});

	let voyagerLayer = L.tileLayer("https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png", {
		attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> | &copy; <a href="https://carto.com/attributions">CARTO</a>',
		maxZoom: 19,
	});

	let positronLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
		attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
		subdomains: 'abcd',
		maxZoom: 20,
	});

	L.control.layers({
		MapBox: mapBoxLayer,
		OSM: osmLayer,
		Voyager: voyagerLayer,
		Positron: positronLayer,
	}).addTo(map);
}

function showLocations(json) {
	let locations = JSON.parse(json);

	resetMap(locations.length ? L.markerClusterGroup({ showCoverageOnHover: true }) : L.layerGroup());

	if (locations.length) {
		for (let location of locations) {
			if (location.latitude && location.longitude) {
				addMarker(location, location.facts);
			}
		}

		showMap();
	}
}

function showLineage(json, paths = false, animate = false) {
	let individuals = JSON.parse(json);

	resetMap();

	if (individuals.length) {
		let locationInfos = new Map();
		let linePoints = [];

		for (let individual of individuals) {
			let birthOrBaptism = individual.birthOrBaptism;
			let birthOrBaptismLocation = birthOrBaptism?.location;
			if (birthOrBaptismLocation?.latitude && birthOrBaptismLocation?.longitude) {
				if (!locationInfos.has(birthOrBaptismLocation.id)) {
					locationInfos.set(birthOrBaptismLocation.id, { location: birthOrBaptismLocation, entries: [] });
				}
				locationInfos.get(birthOrBaptismLocation.id).entries.push(birthOrBaptism.toString);
				linePoints.push([birthOrBaptismLocation.latitude, birthOrBaptismLocation.longitude, birthOrBaptism.date.year]);
			}

			let parentsMarriage = individual.parents?.marriage;
			let parentsMarriageLocation = parentsMarriage?.location;	// TODO: could be after birth of child!
			if (parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
				if (!locationInfos.has(parentsMarriageLocation.id)) {
					locationInfos.set(parentsMarriageLocation.id, { location: parentsMarriageLocation, entries: [] });
				}
				locationInfos.get(parentsMarriageLocation.id).entries.push(parentsMarriage.toString);
				linePoints.push([parentsMarriageLocation.latitude, parentsMarriageLocation.longitude, parentsMarriage.date.year]);
			}
		}

		if (animate) {
			addAnimation(linePoints.toReversed());
		} else {
			if (paths) {
				addPolyline(linePoints);
			}

			for (let locationInfo of locationInfos.values()) {
				addMarker(locationInfo.location, locationInfo.entries);
			}
		}

		showMap();
	}
}

function showAncestors(json, paths = false, animate = false) {
	let ancestors = JSON.parse(json);

	resetMap();

	if (1 in ancestors) {
		let locationInfos = new Map();

		let visited = new Set();
		visited.add(1);

		let queue = [1];

		while (queue.length) {
			let childKekule = queue.shift();
			let child = ancestors[childKekule];

			let birthOrBaptism = child?.birthOrBaptism;
			let birthOrBaptismLocation = birthOrBaptism?.location;
			if (birthOrBaptismLocation?.latitude && birthOrBaptismLocation?.longitude) {
				if (!locationInfos.has(birthOrBaptismLocation.id)) {
					locationInfos.set(birthOrBaptismLocation.id, { location: birthOrBaptismLocation, entries: [] });
				}
				locationInfos.get(birthOrBaptismLocation.id).entries.push(birthOrBaptism.toString);
			}

			let parentsMarriage = child?.parents?.marriage;
			let parentsMarriageLocation = parentsMarriage?.location;
			if (parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
				if (!locationInfos.has(parentsMarriageLocation.id)) {
					locationInfos.set(parentsMarriageLocation.id, { location: parentsMarriageLocation, entries: [] });
				}
				locationInfos.get(parentsMarriageLocation.id).entries.push(parentsMarriage.toString);

				if (paths && birthOrBaptismLocation?.latitude && birthOrBaptismLocation?.longitude) {
					addLine(birthOrBaptismLocation, parentsMarriageLocation);
				}
			}

			let fatherKekule = childKekule * 2;
			let father = ancestors[fatherKekule];
			if (father && !visited.has(fatherKekule)) {
				let fathersBirthOrBaptism = father.birthOrBaptism;
				let fathersBirthOrBaptismLocation = fathersBirthOrBaptism?.location;
				if (fathersBirthOrBaptismLocation?.latitude && fathersBirthOrBaptismLocation?.longitude) {
					if (paths && parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
						addLine(parentsMarriageLocation, fathersBirthOrBaptismLocation);
					} else if (paths && birthOrBaptismLocation?.latitude && birthOrBaptismLocation?.longitude) {
						addLine(birthOrBaptismLocation, fathersBirthOrBaptismLocation);
					}
				}
				queue.push(fatherKekule);
				visited.add(fatherKekule);
			}

			let motherKekule = (childKekule * 2) + 1;
			let mother = ancestors[motherKekule];
			if (mother && !visited.has(motherKekule)) {
				let mothersBirthOrBaptism = mother.birthOrBaptism;
				let mothersBirthOrBaptismLocation = mothersBirthOrBaptism?.location;
				if (mothersBirthOrBaptismLocation?.latitude && mothersBirthOrBaptismLocation?.longitude) {
					if (paths && parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
						addLine(parentsMarriageLocation, mothersBirthOrBaptismLocation);
					} else if (paths && birthOrBaptismLocation?.latitude && birthOrBaptismLocation?.longitude) {
						addLine(birthOrBaptismLocation, mothersBirthOrBaptismLocation);
					}
				}
				queue.push(motherKekule);
				visited.add(motherKekule);
			}
		}

		for (let locationInfo of locationInfos.values()) {
			addMarker(locationInfo.location, locationInfo.entries);
		}

		showMap();
	}
}

function showDescendants(json, paths = false, animate = false) {
	let individuals = JSON.parse(json);

	resetMap();

	if (individuals.length) {
		let locationInfos = new Map();

		for (let individual of individuals) {
			let birthOrBaptism = individual.birthOrBaptism;
			let birthOrBaptismLocation = birthOrBaptism?.location;
			if (birthOrBaptismLocation?.latitude && birthOrBaptismLocation?.longitude) {
				if (!locationInfos.has(birthOrBaptismLocation.id)) {
					locationInfos.set(birthOrBaptismLocation.id, { location: birthOrBaptismLocation, entries: [] });
				}
				locationInfos.get(birthOrBaptismLocation.id).entries.push(birthOrBaptism.toString);

				if (individuals.indexOf(individual) > 0) {
					let parentsMarriage = individual.parents?.marriage;
					let parentsMarriageLocation = parentsMarriage?.location;
					if (paths && parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
						addLine(birthOrBaptismLocation, parentsMarriageLocation);
					}
				}
			}

			for (let family of individual.families) {
				let marriage = family.marriage;
				let marriageLocation = marriage?.location;
				if (marriageLocation?.latitude && marriageLocation?.longitude) {
					if (!locationInfos.has(marriageLocation.id)) {
						locationInfos.set(marriageLocation.id, { location: marriageLocation, entries: [] });
					}
					locationInfos.get(marriageLocation.id).entries.push(marriage.toString);

					if (paths && birthOrBaptismLocation?.latitude && birthOrBaptismLocation?.longitude) {
						addLine(birthOrBaptismLocation, marriageLocation);
					}
				}
			}
		}

		for (let locationInfo of locationInfos.values()) {
			addMarker(locationInfo.location, locationInfo.entries);
		}

		showMap();
	}
}

function resetMap(newContainer = L.layerGroup()) {
	bounds = new L.LatLngBounds();

	if (map && container) {
		map.removeLayer(container);
	}

	container = newContainer;

	resetAnimation();
}

function addLine(location1, location2) {
	let line = L.polyline([[location1.latitude, location1.longitude], [location2.latitude, location2.longitude]], { color: COLOR, opacity: OPACITY });
	bounds.extend(line.getBounds());
	container.addLayer(line);
}

function addPolyline(linePoints) {
	if (linePoints.length > 1) {
		let polyline = L.polyline(linePoints, { color: COLOR, opacity: OPACITY });
		bounds.extend(polyline.getBounds());
		container.addLayer(polyline);
	}
}

function addAnimation(linePoints) {
	if (linePoints.length > 1) {
		animation = L.motion.seq([]);

		let firstPoint = linePoints[0];
		let lastPoint = linePoints[linePoints.length - 1];

		animation.firstYear = firstPoint.length > 2 ? firstPoint[2] : null;
		animation.lastYear = lastPoint.length > 2 ? lastPoint[2] : null;

		for (let i = 0; i < linePoints.length - 1; i++) {
			let point1 = linePoints[i];
			let point2 = linePoints[i + 1];

			if (point1.length > 2 && point2.length > 2) {
				let years = parseInt(point2[2]) - parseInt(point1[2]);

				if (Number.isInteger(years)) {
					let duration = years / ANIMATION_YEARS_PER_SECOND * 1000;

					let line = L.motion.polyline([point1, point2], { color: COLOR, opacity: OPACITY }, { duration });
					line.years = years;
					bounds.extend(line.getBounds());
					animation.addLayer(line, true);
				}
			}
		}

		container.addLayer(animation);
	}
}

function addMarker(location, entries = null) {
	let marker = L.circleMarker([location.latitude, location.longitude], { radius: CIRCLE_MARKER_RADIUS, color: COLOR, fillOpacity: OPACITY, stroke: false});
	marker.bindTooltip(`<h1>${location.name}</h1>${entries ? entries.join("<br />") : ""}<br />${location.imageURL ? `<img src="${location.imageURL}" />` : ""}`, { className: "map-marker-tooltip" });
	bounds.extend(marker.getLatLng());
	container.addLayer(marker);
	return marker;
}

function showMap() {
	if (map && container) {
		map.addLayer(container);
	}

	fitMap();

	startAnimation();
}

function fitMap() {
	if (bounds?.isValid()) {
		map?.fitBounds(bounds);
	} else {
		map?.fitWorld();
	}
}

function startAnimation() {
	if (animation && Number.isInteger(animation.firstYear) && Number.isInteger(animation.lastYear) && animation.lastYear >= animation.firstYear) {
		let year = animation.firstYear;

		animationTimer = setInterval(() => {
			showYearLabel(year);
			if (year === animation.lastYear) {
				stopAnimation();
			}
			year++;
		}, 1000.0 / ANIMATION_YEARS_PER_SECOND);

		animation.motionStart();
	}
}

function resetAnimation() {
	stopAnimation();
	hideYearLabel();
}

function stopAnimation() {
	if (animation) {
		animation.motionStop();
		animation = null;
	}

	if (animationTimer) {
		clearInterval(animationTimer);
		animationTimer = null;
	}
}

function showYearLabel(textContent = "") {
	yearLabel.textContent = textContent;
	yearLabel.style.display = "block";
}

function hideYearLabel() {
	yearLabel.style.display = "none";
}

function log(obj) {
	alert(JSON.stringify(obj));
}
