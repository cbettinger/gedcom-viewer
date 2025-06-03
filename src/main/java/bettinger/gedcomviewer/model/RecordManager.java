package bettinger.gedcomviewer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.folg.gedcom.model.Change;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.DateTimeUtils;
import bettinger.gedcomviewer.utils.HTMLUtils;

class RecordManager {

	private final Record r;

	private final GEDCOM gedcom;

	private final int number;
	private final LocalDateTime lastChange;
	private final List<Structure> references;

	RecordManager(final Record r, final GEDCOM gedcom, final Change lastChange) {
		this(r, gedcom, DateTimeUtils.parseDateTime(lastChange));
	}

	RecordManager(final Record r, final GEDCOM gedcom, final LocalDateTime lastChange) {
		this.r = r;

		this.gedcom = gedcom;

		this.number = gedcom.claimNumber(r.getClass());
		this.lastChange = lastChange;
		this.references = new ArrayList<>();
	}

	GEDCOM getGEDCOM() {
		return gedcom;
	}

	String getId() {
		return r.getId();
	}

	int getNumber() {
		return number;
	}

	LocalDateTime getLastChange() {
		return lastChange;
	}

	void addReference(final Structure referencingStructure) {
		references.add(referencingStructure);
	}

	List<Structure> getReferences() {
		return references;
	}

	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		if (!references.isEmpty() && options.contains(HTMLOption.REFERENCES)) {
			if (options.contains(HTMLOption.COMMONS_HEADINGS)) {
				HTMLUtils.appendH2(sb, I18N.get("References"));
			}

			HTMLUtils.appendText(sb, HTMLUtils.createList(references, Structure::getLink));
		}

		return sb.toString();
	}
}
