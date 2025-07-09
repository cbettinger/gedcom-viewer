package bettinger.gedcomviewer.model;

import java.awt.Image;
import java.awt.Rectangle;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;

public abstract class IndividualFamilyCommonStructure extends Structure implements Record, NoteContainer, MediaContainer, SourceCitationContainer {
	protected final RecordManager recordManager;
	protected final NoteManager noteManager;
	protected final MediaManager mediaManager;
	protected final SourceCitationManager sourceCitationManager;

	protected final org.folg.gedcom.model.PersonFamilyCommonContainer wrappedPersonFamilyCommonContainer;

	protected final List<Fact> facts;

	IndividualFamilyCommonStructure(final GEDCOM gedcom, final org.folg.gedcom.model.PersonFamilyCommonContainer personFamilyCommonContainer, final String id) {
		super(gedcom, id, personFamilyCommonContainer);

		this.recordManager = new RecordManager(this, gedcom, personFamilyCommonContainer.getChange());
		this.noteManager = new NoteManager(this, gedcom, personFamilyCommonContainer.getNoteRefs());
		this.mediaManager = new MediaManager(this, gedcom, personFamilyCommonContainer.getMediaRefs());
		this.sourceCitationManager = new SourceCitationManager(this, gedcom);

		this.wrappedPersonFamilyCommonContainer = personFamilyCommonContainer;

		this.facts = new ArrayList<>();
		this.facts.addAll(personFamilyCommonContainer.getEventsFacts().stream().map(eventFact -> new Fact(gedcom, eventFact, this)).toList());
		this.facts.addAll(getExtensionTags().stream().filter(tag -> !tag.getTag().isEmpty() && !tag.getValue().isEmpty()).map(tag -> new Fact(gedcom, tag, this)).toList());
	}

	/* #region container */
	@Override
	public GEDCOM getGEDCOM() {
		return recordManager.getGEDCOM();
	}

	@Override
	public boolean hasXRef() {
		return true;
	}

	@Override
	public int getNumber() {
		return recordManager.getNumber();
	}

	@Override
	public LocalDateTime getLastChange() {
		return recordManager.getLastChange();
	}

	@Override
	public void addReference(final Structure referencingStructure) {
		recordManager.addReference(referencingStructure);
	}

	@Override
	public List<Structure> getReferences() {
		return recordManager.getReferences();
	}

	@Override
	public List<Note> getNotes() {
		return noteManager.getNotes();
	}

	@Override
	public List<Note> getNotes(final boolean excludeConfidential) {
		return noteManager.getNotes(excludeConfidential);
	}

	@Override
	public List<Media> getMedia() {
		return getMedia(false);
	}

	@Override
	public List<Media> getMedia(final boolean excludeConfidential) {
		final var result = new ArrayList<>(mediaManager.getMedia(excludeConfidential));

		final var publicFacts = getFacts(excludeConfidential);
		for (final var f : publicFacts) {
			result.addAll(f.getMedia(excludeConfidential));
		}
		return new ArrayList<>(new HashSet<>(result));
	}

	@Override
	public Media getPrimaryImage(final boolean onlyPhoto) {
		return mediaManager.getPrimaryImage(onlyPhoto);
	}

	@Override
	public Rectangle getImageClip(final Media image) {
		return mediaManager.getImageClip(image);
	}

	@Override
	public Image getClippedImage(final Media image, final int width, final int height) {
		return mediaManager.getClippedImage(image, width, height);
	}

	@Override
	public void setSources() {
		sourceCitationManager.setSources(wrappedPersonFamilyCommonContainer.getSourceCitations());

		for (final var fact : facts) {
			fact.setSources();
		}
	}

	@Override
	public List<SourceCitation> getSourceCitations() {
		return getSourceCitations(false);
	}

	@Override
	public List<SourceCitation> getSourceCitations(boolean excludeConfidential) {
		final var result = new ArrayList<>(sourceCitationManager.getSourceCitations(excludeConfidential));

		final var publicFacts = getFacts(excludeConfidential);
		for (final var f : publicFacts) {
			result.addAll(f.getSourceCitations(excludeConfidential));
		}
		return new ArrayList<>(new HashSet<>(result));
	}
	/* #endregion */

	/* #region getter & setter */
	public List<Location> getLocations() {
		return getLocations(false);
	}

	public List<Location> getLocations(final boolean excludeConfidential) {
		final var result = new HashSet<Location>();

		final var publicFacts = getFacts(excludeConfidential);
		for (final var f : publicFacts) {
			final var location = f.getLocation();
			if (location != null) {
				result.add(location);
			}
		}
		return new ArrayList<>(result);
	}

	public Quality getQuality(final String tag) {
		var qualityValue = Quality.UNKNOWN.getValue();

		final var factsOfTag = getFacts(tag);
		for (final var fact : factsOfTag) {
			final var factQualityValue = fact.getQuality().getValue();
			if (factQualityValue > qualityValue) {
				qualityValue = factQualityValue;
			}
		}

		return Quality.fromValue(qualityValue);
	}

	public Fact getBestFact(final String tag) {
		final var quality = getQuality(tag);
		final var bestFacts = getFacts(tag).stream().filter(fact -> fact.getQuality() == quality).toList();
		return bestFacts.isEmpty() ? null : bestFacts.get(0);
	}

	public List<Fact> getFacts(final String tag) {
		return getFacts().stream().filter(fact -> fact.getTag().equals(tag)).toList();
	}

	public List<Fact> getFacts() {
		return getFacts(false);
	}

	public List<Fact> getFacts(final boolean excludeConfidential) {
		var result = facts;

		if (excludeConfidential) {
			if (isConfidential(this)) {
				result = new ArrayList<>();
			} else {
				result = facts.stream().filter(Predicate.not(Structure::isConfidential)).toList();
			}
		}

		return result;
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var publicFacts = getFacts(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
		if (!publicFacts.isEmpty()) {
			if (options.contains(HTMLOption.COMMONS_HEADINGS)) {
				HTMLUtils.appendH2(sb, I18N.get("Facts"));
			} else {
				HTMLUtils.appendLineBreaks(sb, 2);
			}

			HTMLUtils.appendText(sb, HTMLUtils.createList(publicFacts, f -> f.toHTML(options), true));
		}

		HTMLUtils.appendText(sb, noteManager.toHTML(options));
		HTMLUtils.appendText(sb, mediaManager.toHTML(options));
		HTMLUtils.appendText(sb, sourceCitationManager.toHTML(options));
		HTMLUtils.appendText(sb, recordManager.toHTML(options));

		return sb.toString();
	}
	/* #endregion */
}
