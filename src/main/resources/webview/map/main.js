function getExports() { return { showLocations, showLineage, showAncestors, showDescendants }; }

const COLOR = "#FF2262";
const OPACITY = 0.7;
const LINE_OPTIONS = { color: COLOR, opacity: OPACITY };
const CIRCLE_MARKER_OPTIONS = { ...LINE_OPTIONS, radius: 10, stroke: false, fillOpacity: OPACITY };

let map = null;

let bounds = null;
let container = null;

let timeline = null;
let timelineControl = null;

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
			addMarker(location, location.facts);
		}

		showMap();
	}
}

function showLineage(json, paths = false, animate = false) {
	let individuals = JSON.parse(json);

	resetMap();

	if (individuals.length) {
		let points = [];
		let locationInfos = new Map();

		for (let individual of individuals) {
			let birthOrBaptism = individual.birthOrBaptism;
			let birthOrBaptismLocation = birthOrBaptism?.location;
			if (birthOrBaptismLocation?.lat && birthOrBaptismLocation?.lng) {
				points.push({ date: birthOrBaptism.date, location: birthOrBaptismLocation.id });

				if (!locationInfos.has(birthOrBaptismLocation.id)) {
					locationInfos.set(birthOrBaptismLocation.id, { location: birthOrBaptismLocation, references: [] });
				}
				locationInfos.get(birthOrBaptismLocation.id).references.push(birthOrBaptism.toString);
			}

			let parentsMarriage = individual.parents?.marriage;
			let parentsMarriageLocation = parentsMarriage?.location;
			if (parentsMarriageLocation?.lat && parentsMarriageLocation?.lng) {
				points.push({ date: parentsMarriage.date, location: parentsMarriageLocation.id });

				if (!locationInfos.has(parentsMarriageLocation.id)) {
					locationInfos.set(parentsMarriageLocation.id, { location: parentsMarriageLocation, references: [] });
				}
				locationInfos.get(parentsMarriageLocation.id).references.push(parentsMarriage.toString);
			}
		}

		if (animate) {
			addAnimation(points.toReversed(), locationInfos);
		} else {
			if (paths) {
				addPolyline(points.map(p => locationInfos.get(p.location).location));
			}

			for (let locationInfo of locationInfos.values()) {
				addMarker(locationInfo.location, locationInfo.references);
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
			if (birthOrBaptismLocation?.lat && birthOrBaptismLocation?.lng) {
				if (!locationInfos.has(birthOrBaptismLocation.id)) {
					locationInfos.set(birthOrBaptismLocation.id, { location: birthOrBaptismLocation, references: [] });
				}
				locationInfos.get(birthOrBaptismLocation.id).references.push(birthOrBaptism.toString);
			}

			let parentsMarriage = child?.parents?.marriage;
			let parentsMarriageLocation = parentsMarriage?.location;
			if (parentsMarriageLocation?.lat && parentsMarriageLocation?.lng) {
				if (!locationInfos.has(parentsMarriageLocation.id)) {
					locationInfos.set(parentsMarriageLocation.id, { location: parentsMarriageLocation, references: [] });
				}
				locationInfos.get(parentsMarriageLocation.id).references.push(parentsMarriage.toString);

				if (paths && birthOrBaptismLocation?.lat && birthOrBaptismLocation?.lng) {
					addLine(birthOrBaptismLocation, parentsMarriageLocation);
				}
			}

			let fatherKekule = childKekule * 2;
			let father = ancestors[fatherKekule];
			if (father && !visited.has(fatherKekule)) {
				let fathersBirthOrBaptism = father.birthOrBaptism;
				let fathersBirthOrBaptismLocation = fathersBirthOrBaptism?.location;
				if (fathersBirthOrBaptismLocation?.lat && fathersBirthOrBaptismLocation?.lng) {
					if (paths && parentsMarriageLocation?.lat && parentsMarriageLocation?.lng) {
						addLine(parentsMarriageLocation, fathersBirthOrBaptismLocation);
					} else if (paths && birthOrBaptismLocation?.lat && birthOrBaptismLocation?.lng) {
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
				if (mothersBirthOrBaptismLocation?.lat && mothersBirthOrBaptismLocation?.lng) {
					if (paths && parentsMarriageLocation?.lat && parentsMarriageLocation?.lng) {
						addLine(parentsMarriageLocation, mothersBirthOrBaptismLocation);
					} else if (paths && birthOrBaptismLocation?.lat && birthOrBaptismLocation?.lng) {
						addLine(birthOrBaptismLocation, mothersBirthOrBaptismLocation);
					}
				}
				queue.push(motherKekule);
				visited.add(motherKekule);
			}
		}

		for (let locationInfo of locationInfos.values()) {
			addMarker(locationInfo.location, locationInfo.references);
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
			if (birthOrBaptismLocation?.lat && birthOrBaptismLocation?.lng) {
				if (!locationInfos.has(birthOrBaptismLocation.id)) {
					locationInfos.set(birthOrBaptismLocation.id, { location: birthOrBaptismLocation, references: [] });
				}
				locationInfos.get(birthOrBaptismLocation.id).references.push(birthOrBaptism.toString);

				if (individuals.indexOf(individual) > 0) {
					let parentsMarriage = individual.parents?.marriage;
					let parentsMarriageLocation = parentsMarriage?.location;
					if (paths && parentsMarriageLocation?.lat && parentsMarriageLocation?.lng) {
						addLine(birthOrBaptismLocation, parentsMarriageLocation);
					}
				}
			}

			for (let family of individual.families) {
				let marriage = family.marriage;
				let marriageLocation = marriage?.location;
				if (marriageLocation?.lat && marriageLocation?.lng) {
					if (!locationInfos.has(marriageLocation.id)) {
						locationInfos.set(marriageLocation.id, { location: marriageLocation, references: [] });
					}
					locationInfos.get(marriageLocation.id).references.push(marriage.toString);

					if (paths && birthOrBaptismLocation?.lat && birthOrBaptismLocation?.lng) {
						addLine(birthOrBaptismLocation, marriageLocation);
					}
				}
			}
		}

		for (let locationInfo of locationInfos.values()) {
			addMarker(locationInfo.location, locationInfo.references);
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

	if (timeline && timelineControl) {
		timelineControl.removeTimelines(timeline);

		timelineControl.remove();
		timelineControl = null;

		timeline.remove();
		timeline = null;
	}
}

function addLine(location1, location2) {
	addPolyline([location1, location2]);
}

function addPolyline(locations) {
	if (locations.length > 1) {
		let polyline = L.polyline(locations, LINE_OPTIONS);
		bounds.extend(polyline.getBounds());
		container.addLayer(polyline);
	}
}

function addAnimation(points, locationInfos) {
	if (points.length > 1) {
		let geoJSON = { type: "FeatureCollection", features: [] };

		//let lastPoint = linePoints[linePoints.length - 1];	// TODO: improve
		let lastYear = 2026; //lastPoint.length > 2 ? parseInt(lastPoint[2]) : null;

		if (Number.isInteger(lastYear)) {
			for (let point of points) {
				let locationInfo = locationInfos.get(point.location);
				let location = locationInfo.location;

				bounds.extend(location);

				geoJSON.features.push({
					type: "Feature",
					properties: {
						start: point.date.isoFormattedTimestamp,
						end: `${lastYear}`,
						location,
						references: locationInfo.references
					},
					geometry: {
						type: "Point",
						coordinates: [location.lng, location.lat]
					}
				});
			}

			timelineControl = new L.TimelineSliderControl({
				position: "bottomright",
				formatOutput: date => new Date(date).getFullYear().toString()
			});

			timeline = new L.Timeline(geoJSON, { pointToLayer: data => addMarker(data.properties.location, data.properties.references) });

			timelineControl.addTo(map);
			timelineControl.addTimelines(timeline);
			timeline.addTo(map);
		}
	}
}

function addMarker(location, references = null) {
	if (!location.lat || !location.lng) {
		return null;
	}

	let marker = L.circleMarker(location, CIRCLE_MARKER_OPTIONS);
	bounds.extend(marker.getLatLng());
	container.addLayer(marker);

	if (location.name) {
		marker.bindTooltip(`<h1>${location.name}</h1>${Array.isArray(references) ? references.join("<br />") : ""}<br />${location.imageURL ? `<img src="${location.imageURL}" />` : ""}`, { className: "map-marker-tooltip" });
	}

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
	if (timelineControl) {
		timelineControl.play();
	}
}

function log(obj) {
	alert(JSON.stringify(obj));
}
