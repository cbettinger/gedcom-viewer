function getExports() { return { setBody, setScrollTo }; }

function setBody(innerHTML) {
	document.body.innerHTML = innerHTML;
}

function setScrollTo(x, y) {
	window.scrollTo(x, y);
}
