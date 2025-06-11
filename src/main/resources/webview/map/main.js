function getExports() { return { showLocations, showLineage, showAncestors, showDescendants }; }

const LINE_COLOR = "#FF2262";

let map = null;
let bounds = null;
let layer = null;
let locationsBounds = null;

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

	let voyagerLayer = L.tileLayer("https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png", {
		attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> | &copy; <a href="https://carto.com/attributions">CARTO</a>',
		maxZoom: 19,
	});

	let osmLayer = L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
		maxZoom: 19,
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
	});

	let waterColorBaseLayer = L.tileLayer("https://stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.jpg", {
		attribution: '&copy; <a href="http://stamen.com">Stamen Design</a> (<a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>)',
		maxZoom: 14,
		ext: "jpg",
	});

	let waterColorLabelLayer = L.tileLayer("https://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}{r}.png", {
		attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> | &copy; <a href="https://carto.com/attributions">CARTO</a>',
		maxZoom: 14,
	});

	L.control.layers({
		MapBox: mapBoxLayer,
		Voyager: voyagerLayer,
		OSM: osmLayer,
		Watercolor: L.layerGroup([waterColorBaseLayer, waterColorLabelLayer]),
	}).addTo(map);
}

function showLocations(json) {
	let locations = JSON.parse(json);

	reset(locations.length ?  L.markerClusterGroup({
		showCoverageOnHover: false,
	}) : L.layerGroup());

	if (locations.length) {
		for (let location of locations) {
			if (location.latitude && location.longitude) {
				createMarker(location, location.references);
			}
		}

		show();

		if (bounds?.isValid()) {
			locationsBounds = bounds;
		}
	}
}

function showLineage(json) {
	let individuals = JSON.parse(json);

	reset();

	if (individuals.length) {
		let locationInfos = new Map();
		let linePoints = [];
		let number = 1;

		for (let individual of individuals) {
			let birthLocation = individual.birthLocation;
			if (birthLocation) {
				if (birthLocation.latitude && birthLocation.longitude) {
					if (!locationInfos.has(birthLocation.id)) {
						locationInfos.set(birthLocation.id, { location: birthLocation, entries: [] });
					}
					locationInfos.get(birthLocation.id).entries.push({ number: number++, text: `<span class="sign">*</span> ${individual.name}` });
					linePoints.push([birthLocation.latitude, birthLocation.longitude]);
				}
			}

			let parentsMarriageLocation = individual?.parents?.marriageLocation;
			if (parentsMarriageLocation) {
				if (parentsMarriageLocation.latitude && parentsMarriageLocation.longitude) {
					if (!locationInfos.has(parentsMarriageLocation.id)) {
						locationInfos.set(parentsMarriageLocation.id, { location: parentsMarriageLocation, entries: [] });
					}
					locationInfos.get(parentsMarriageLocation.id).entries.push({ number: number++, text: `<span class="sign">⚭</span> ${individual.parents.name}` });
					linePoints.push([parentsMarriageLocation.latitude, parentsMarriageLocation.longitude]);
				}
			}
		}

		for (let locationInfo of locationInfos.values()) {
			createNumberedMarker(locationInfo.location, locationInfo.entries.map(e => `${locationInfo.entries.length === 1 ? "" : `<span class="number">${e.number}:</span>`}${e.text}`), locationInfo.entries.length === 1 ? locationInfo.entries[0].number : "…");
		}

		addPolyline(linePoints);

		show();
	}
}

function showAncestors(json) {
	let ancestors = JSON.parse(json);

	reset();

	if (1 in ancestors) {
		let locationInfos = new Map();

		let visited = new Set();
		visited.add(1);

		let queue = [1];

		while (queue.length) {
			let childKekule = queue.shift();
			let child = ancestors[childKekule];

			let birthLocation = child?.birthLocation;
			if (birthLocation) {
				if (birthLocation.latitude && birthLocation.longitude) {
					if (!locationInfos.has(birthLocation.id)) {
						locationInfos.set(birthLocation.id, { location: birthLocation, entries: [] });
					}
					locationInfos.get(birthLocation.id).entries.push(`<span class="sign">*</span> ${child.name}`);
				}
			}

			let parentsMarriageLocation = child?.parents?.marriageLocation;
			if (parentsMarriageLocation) {
				if (parentsMarriageLocation.latitude && parentsMarriageLocation.longitude) {
					if (!locationInfos.has(parentsMarriageLocation.id)) {
						locationInfos.set(parentsMarriageLocation.id, { location: parentsMarriageLocation, entries: [] });
					}
					locationInfos.get(parentsMarriageLocation.id).entries.push(`<span class="sign">⚭</span> ${child.parents.name}`);

					if (child?.birthLocation?.latitude && child?.birthLocation?.longitude && parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
						addLine(child.birthLocation, parentsMarriageLocation);
					}
				}
			}

			let fatherKekule = childKekule * 2;
			let father = ancestors[fatherKekule];
			if (father && !visited.has(fatherKekule)) {
				let father = ancestors[fatherKekule];
				if (father?.birthLocation?.latitude && father?.birthLocation?.longitude) {
					if (parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
						addLine(parentsMarriageLocation, father.birthLocation);
					} else if (child?.birthLocation?.latitude && child?.birthLocation?.longitude) {
						addLine(child.birthLocation, father.birthLocation);
					}
				}
				queue.push(fatherKekule);
				visited.add(fatherKekule);
			}

			let motherKekule = (childKekule * 2) + 1;
			let mother = ancestors[motherKekule];
			if (mother && !visited.has(motherKekule)) {
				if (mother?.birthLocation?.latitude && mother?.birthLocation?.longitude) {
					if (parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
						addLine(parentsMarriageLocation, mother.birthLocation);
					} else if (child?.birthLocation?.latitude && child?.birthLocation?.longitude) {
						addLine(child.birthLocation, mother.birthLocation);
					}
				}
				queue.push(motherKekule);
				visited.add(motherKekule);
			}
		}

		for (let locationInfo of locationInfos.values()) {
			createMarker(locationInfo.location, locationInfo.entries);
		}

		show();
	}
}

function showDescendants(json) {
	let individuals = JSON.parse(json);

	reset();

	if (individuals.length) {
		let locationInfos = new Map();

		for (let individual of individuals) {
			let birthLocation = individual.birthLocation;
			if (birthLocation) {
				if (birthLocation.latitude && birthLocation.longitude) {
					if (!locationInfos.has(birthLocation.id)) {
						locationInfos.set(birthLocation.id, { location: birthLocation, entries: [] });
					}
					locationInfos.get(birthLocation.id).entries.push(`<span class="sign">*</span> ${individual.name}`);

					if (individuals.indexOf(individual) > 0) {
						let parentsMarriageLocation = individual?.parents?.marriageLocation;
						if (birthLocation?.latitude && birthLocation?.longitude && parentsMarriageLocation?.latitude && parentsMarriageLocation?.longitude) {
							addLine(birthLocation, parentsMarriageLocation);
						}
					}
				}
			}

			for (let family of individual.families) {
				let marriageLocation = family?.marriageLocation;
				if (marriageLocation) {
					if (marriageLocation.latitude && marriageLocation.longitude) {
						if (!locationInfos.has(marriageLocation.id)) {
							locationInfos.set(marriageLocation.id, { location: marriageLocation, entries: [] });
						}
						locationInfos.get(marriageLocation.id).entries.push(`<span class="sign">⚭</span> ${family.name}`);

						if (birthLocation?.latitude && birthLocation?.longitude && marriageLocation?.latitude && marriageLocation?.longitude) {
							addLine(birthLocation, marriageLocation);
						}
					}
				}
			}
		}

		for (let locationInfo of locationInfos.values()) {
			createMarker(locationInfo.location, locationInfo.entries);
		}

		show();
	}
}

function reset(newLayer = L.layerGroup()) {
	if (map) {
		bounds = new L.LatLngBounds();

		if (layer) {
			map.removeLayer(layer);
		}
		layer = newLayer;
	}
}

function addLine(location1, location2) {
	layer.addLayer(L.polyline([[location1.latitude, location1.longitude], [location2.latitude, location2.longitude]], { color: LINE_COLOR }));
}

function addPolyline(linePoints) {
	layer.addLayer(L.polyline(linePoints, { color: LINE_COLOR }));
}

function createNumberedMarker(location, entries = null, number = "?") {
	let marker = createMarker(location, entries);
	marker.setIcon(new L.NumberedDivIcon({ number }));
	return marker;
}

function createMarker(location, entries = null) {
	let marker = L.marker([location.latitude, location.longitude]);
	marker.bindTooltip(`<h1>${location.name}</h1>${entries ? entries.join("<br>") : ""}<br />${location.imageURL ? `<img src="${location.imageURL}" />`: ""}`, { className: "map-marker-tooltip" });
	bounds.extend(marker.getLatLng());
	layer.addLayer(marker);
	return marker;
}

function show() {
	if (map) {
		if (layer) {
			map.addLayer(layer);
		}

		if (bounds) {
			if (bounds.isValid()) {
				map.fitBounds(bounds);
			}
			else if (locationsBounds?.isValid()) {
				map.fitBounds(locationsBounds);
			}
			else {
				map.setView([0, 0], 0);
			}
		}
	}
}
