L.NumberedDivIcon = L.Icon.extend({
	options: {
		iconUrl: 'leaflet/images/marker-icon-number.png',
		number: '',
		shadowUrl: null,
		iconSize: new L.Point(25, 41),
		iconAnchor: new L.Point(13, 41),
		tooltipAnchor: new L.Point(15, -27),
		popupAnchor: new L.Point(0, -33),
		className: 'leaflet-div-icon'
	},

	createIcon: function () {
		let div = document.createElement('div');
		let img = this._createImg(this.options['iconUrl']);
		let numDiv = document.createElement('div');
		numDiv.setAttribute("class", "number");
		numDiv.innerHTML = this.options['number'] || '';
		div.appendChild(img);
		div.appendChild(numDiv);
		this._setIconStyles(div, 'icon');
		return div;
	},

	createShadow: function () {
		return null;
	}
});
