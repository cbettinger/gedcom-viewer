package bettinger.gedcomviewer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

class SourceCitationManager {

	private static final String SIGN = "※";

	private final Structure referencingStructure;
	private final GEDCOM gedcom;

	private List<SourceCitation> sourceCitations;

	SourceCitationManager(final Structure referencingStructure, final GEDCOM gedcom) {
		this.referencingStructure = referencingStructure;
		this.gedcom = gedcom;

		this.sourceCitations = new ArrayList<>();
	}

	void setSources() {
		setSources(TagUtils.getChildTags(gedcom.getTag(referencingStructure.getId()), "SOUR").stream().map(tag -> {
			final var sourceCitation = new org.folg.gedcom.model.SourceCitation();
			sourceCitation.setRef(tag.getRef());
			final var page = TagUtils.parseChildTagValue(tag, "PAGE");
			if (page != null) {
				sourceCitation.setPage(page);
			}
			final var quay = TagUtils.parseChildTagValue(tag, "QUAY");
			if (quay != null) {
				sourceCitation.setQuality(quay);
			}
			return sourceCitation;
		}).toList());
	}

	void setSources(final List<org.folg.gedcom.model.SourceCitation> sourceCitations) {
		this.sourceCitations = sourceCitations.stream().map(sc -> new SourceCitation(gedcom, sc, referencingStructure)).toList();

		for (final var sr : this.sourceCitations) {
			final var source = sr.getSource();
			if (source != null) {
				source.addReference(sr);
			}
		}
	}

	public List<SourceCitation> getSourceCitations() {
		return getSourceCitations(false);
	}

	public List<SourceCitation> getSourceCitations(final boolean excludeConfidential) {
		return excludeConfidential ? sourceCitations.stream().filter(Predicate.not(Structure::isConfidential)).toList() : sourceCitations;
	}

	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		if (!sourceCitations.isEmpty()) {
			if (options.contains(HTMLOption.COMMONS_HEADINGS)) {
				HTMLUtils.appendH2(sb, I18N.get("Sources"));
			} else {
				HTMLUtils.appendLineBreaks(sb, 2);
			}

			HTMLUtils.appendText(sb, HTMLUtils.createList(sourceCitations, SourceCitation::getLink, options.contains(HTMLOption.COMMONS_HEADINGS) ? null : SIGN));
		}

		return sb.toString();
	}
}
