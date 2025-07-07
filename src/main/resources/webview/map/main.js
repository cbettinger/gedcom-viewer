function getExports() { return { showLocations, showFacts }; }

const DEFAULT_COLOR = "#ff2262";
const OPACITY = 0.7;
const LINE_OPTIONS = { color: DEFAULT_COLOR, opacity: OPACITY };
const CIRCLE_MARKER_OPTIONS = { ...LINE_OPTIONS, radius: 10, stroke: false, fillOpacity: OPACITY };

const COLOR_PALETTE = ["maroon", "olive", "teal", "orange", "navy", "green", "blue", "purple"];

let nextColor = 0;

let map = null;

let bounds = null;
let container = null;

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

function showFacts(json, paths = false) {
	let facts = JSON.parse(json);

	resetMap();

	if (facts.length) {
		let locationInfos = new Map();

		for (let factsOfIndividual of facts) {
			let points = [];

			for (let fact of factsOfIndividual) {
				if (fact.location?.id && fact.location?.lat && fact.location?.lng) {
					if (!locationInfos.has(fact.location.id)) {
						locationInfos.set(fact.location.id, { location: fact.location, references: [] });
					}
					locationInfos.get(fact.location.id).references.push(fact.toString);

					points.push(fact.location.id);
				}
			}

			if (paths) {
				addPolyline(points.map(id => locationInfos.get(id).location), getNextColor());
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

	nextColor = 0;
}

function addLine(location1, location2, color = null) {
	addPolyline([location1, location2], color);
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
}

function fitMap() {
	if (bounds?.isValid()) {
		map?.fitBounds(bounds);
	} else {
		map?.fitWorld();
	}
}

function log(obj) {
	alert(JSON.stringify(obj));
}
