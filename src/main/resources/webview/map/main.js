function getExports() { return { showLocations, showLineage, showAncestors, showDescendants }; }

const DEFAULT_COLOR = "#ff2262";
const OPACITY = 0.7;
const LINE_OPTIONS = { color: DEFAULT_COLOR, opacity: OPACITY };
const CIRCLE_MARKER_OPTIONS = { ...LINE_OPTIONS, radius: 10, stroke: false, fillOpacity: OPACITY };

const COLOR_PALETTE = ["#7bc17f", "#6dab8f", "#578886", "#b6587c", "#cf53cf", "#ae45d1", "#9a3dcc", "#7c30c8", "#fffd95", "#d2d29c", "#b2b2a4", "#78789e", "#aec2bb", "#97a8bd", "#8494c0", "#6975b2", "#4c549b", "#ffc337", "#feae34", "#e39a3b", "#be8042", "#9e6a43", "#ff964e", "#ff8643", "#d15842", "#b14a50"];	// TODO: improve palette

let nextColor = 0;

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
	let facts = JSON.parse(json);

	resetMap();

	if (facts.length) {
		let locationInfos = new Map();

		for (let factsOfIndividual of facts) {
			let points = [];

			for (let fact of factsOfIndividual) {
				if (fact.date?.start && fact.location?.id && fact.location?.lat && fact.location?.lng) {
					if (!locationInfos.has(fact.location.id)) {
						locationInfos.set(fact.location.id, { location: fact.location, references: [] });
					}
					locationInfos.get(fact.location.id).references.push(fact.toString);

					points.push({ date: fact.date, location: fact.location.id });
				}
			}

			if (paths) {
				addPolyline(points.map(p => locationInfos.get(p.location).location), getNextColor());
			}
		}

		if (animate) {
			addAnimation(facts, locationInfos);
		} else {
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

function addPolyline(locations, color = null) {
	if (locations.length > 1) {
		let options = { ...LINE_OPTIONS };
		if (color) {
			options.color = color;
		}

		let polyline = L.polyline(locations, options);
		bounds.extend(polyline.getBounds());
		container.addLayer(polyline);
	}
}

function getNextColor() {
	if (nextColor === COLOR_PALETTE.length) {
		nextColor = 0;
	}
	return COLOR_PALETTE[nextColor++];
}

function addAnimation(facts, locationInfos) {
	if (facts.length) {
		let geoJSON = { type: "FeatureCollection", features: [] };

		for (let factsOfIndividual of facts) {
			for (let fact of factsOfIndividual) {
				let location = fact.location;
				let locationInfo = locationInfos.get(location.id);

				bounds.extend(location);

				geoJSON.features.push({
					type: "Feature",
					properties: {
						location,
						references: locationInfo.references,
						start: fact.date.start,
						end: fact.date.end || "2026-01-01"	// TODO: improve
					},
					geometry: {
						type: "Point",
						coordinates: [location.lng, location.lat]
					}
				});

				log(fact.date.start);
				//log(fact.date.end);
			}
			log("-----------");
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
