package bettinger.gedcomviewer.model;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;

public class Fact extends Substructure implements NoteContainer, MediaContainer, SourceCitationContainer {

	private static final List<String> BAPTISM_TAGS = Arrays.asList("BAP", "BAPM", "BAPT", "BAPTISM", "ADULT_CHRISTNG", "CHR", "CHRA", "CHRISTENING");
	private static final List<String> MARRIAGE_TAGS = Arrays.asList("MARC", "MARR", "MARRIAGE");
	private static final List<String> DEATH_TAGS = Arrays.asList("DEAT", "DEATH");

	private final NoteManager noteManager;
	private final MediaManager mediaManager;
	private final SourceCitationManager sourceCitationManager;

	private final org.folg.gedcom.model.EventFact wrappedFact;

	private final Date date;
	private final Location location;

	Fact(final GEDCOM gedcom, final org.folg.gedcom.model.EventFact eventFact, final Structure parentStructure) {
		super(gedcom, eventFact.getTag(), eventFact, parentStructure);

		this.noteManager = new NoteManager(this, gedcom, eventFact.getNoteRefs());
		this.mediaManager = new MediaManager(this, gedcom, eventFact.getMediaRefs());
		this.sourceCitationManager = new SourceCitationManager(this, gedcom);

		this.wrappedFact = eventFact;

		this.date = new Date(eventFact.getDate());

		final var locationTag = getFirstExtensionTag(Location.TAG);
		this.location = locationTag == null ? null : (Location) gedcom.getRecord(locationTag.getRef());
		if (this.location != null) {
			this.location.addReference(this);
		}
	}

	/* #region container */
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
		return mediaManager.getMedia();
	}

	@Override
	public List<Media> getMedia(final boolean excludeConfidential) {
		return mediaManager.getMedia(excludeConfidential);
	}

	@Override
	public String getPrimaryImageURL(final boolean onlyPhoto) {
		return mediaManager.getPrimaryImageURL(onlyPhoto);
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
	public void setSources() {
		sourceCitationManager.setSources(wrappedFact.getSourceCitations());
	}

	@Override
	public List<SourceCitation> getSourceCitations() {
		return sourceCitationManager.getSourceCitations();
	}

	@Override
	public List<SourceCitation> getSourceCitations(boolean excludeConfidential) {
		return sourceCitationManager.getSourceCitations(excludeConfidential);
	}
	/* #endregion */

	/* #region getter & setter */
	public String getTag() {
		final var tag = wrappedFact.getTag();
		return tag == null ? "" : tag;
	}

	public String getType() {
		final var type = wrappedFact.getType();
		return type == null ? "" : type;
	}

	public String getValue() {
		final var value = wrappedFact.getValue();
		return value == null ? "" : value;
	}

	public Date getDate() {
		return date;
	}

	public String getPlace() {
		final var place = wrappedFact.getPlace();
		return place == null ? "" : place;
	}

	public Location getLocation() {
		return location;
	}

	public String getCause() {
		final var cause = wrappedFact.getCause();
		return cause == null ? "" : cause;
	}

	public String getAgency() {
		return getFirstExtensionTagValue("AGNC");
	}

	public Quality getQuality() {
		var qualityValue = Quality.UNKNOWN.getValue();

		final var factSourcesQuality = getQuality(getSourceCitations()).getValue();
		if (factSourcesQuality > qualityValue) {
			qualityValue = factSourcesQuality;
		}

		final var media = getMedia();
		for (final var medium : media) {
			final var mediumSourcesQuality = getQuality(medium.getSourceCitations()).getValue();
			if (mediumSourcesQuality > qualityValue) {
				qualityValue = mediumSourcesQuality;
			}
		}

		return Quality.fromValue(qualityValue);
	}

	private Quality getQuality(final List<SourceCitation> sources) {
		var qualityValue = Quality.UNKNOWN.getValue();

		for (final var source : sources) {
			final var sourceQualityValue = source.getQuality().getValue();
			if (sourceQualityValue > qualityValue) {
				qualityValue = sourceQualityValue;
			}
		}

		return Quality.fromValue(qualityValue);
	}

	public String getLabel() {
		return toString();
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		final var sb = new StringBuilder(String.format(Format.PADDED_PIPE_SEPARATED, parentStructure.toString(), getLocaleTag()));

		if (!getValue().isEmpty()) {
			sb.append(String.format(Format.TRAILING_SPACE_COLON_WITH_SUFFIX, getValue()));
		}

		return sb.toString();
	}

	@SuppressWarnings("java:S6541")
	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var tag = getTag();
		final var localeTag = getLocaleTag();
		final var type = getType();
		final var value = getValue();
		final var dateStr = getDate() == null ? "" : getDate().toString();
		final var place = getPlace();
		final var agency = getAgency();

		final var sb = new StringBuilder();

		if ("EVEN".equals(tag) && !type.isEmpty()) {
			sb.append(type);
		} else {
			sb.append(localeTag);

			if (!type.isEmpty()) {
				HTMLUtils.appendText(sb, " (");
				HTMLUtils.appendText(sb, type);
				HTMLUtils.appendText(sb, ")");
			}
		}

		HTMLUtils.appendText(sb, Format.TRAILING_SPACE_COLON);

		var wasAppended = false;

		if (!value.isEmpty()) {

			if (Occupation.TAG.equals(tag)) {
				HTMLUtils.appendText(sb, Occupation.getLinkFromFact(this));
			} else if ("SEX".equals(tag)) {
				HTMLUtils.appendText(sb, getSexSign(value));
			} else {
				HTMLUtils.appendText(sb, value);
			}

			wasAppended = true;
		}

		if (!dateStr.isEmpty()) {
			if (wasAppended) {
				HTMLUtils.appendSeparator(sb);
			}
			HTMLUtils.appendText(sb, dateStr);
			wasAppended = true;
		}

		if (!place.isEmpty()) {
			if (wasAppended) {
				HTMLUtils.appendSeparator(sb);
			}
			HTMLUtils.appendText(sb, location == null ? place : location.getLink());
		}

		if (!agency.isEmpty()) {
			HTMLUtils.appendTextAfterLineBreaks(sb, String.format(Format.KEY_VALUE, I18N.get("Agency"), agency), 2);
		}

		if (BAPTISM_TAGS.contains(tag)) {
			final var godParents = getExtensionTags("_GODP");
			if (!godParents.isEmpty()) {
				HTMLUtils.appendTextAfterLineBreaks(sb, String.join(HTMLUtils.LINE_BREAK, godParents.stream().map(t -> String.format(Format.KEY_VALUE, I18N.get("Godparents"), t.getValue())).toList()), 2);
			}
		} else if (MARRIAGE_TAGS.contains(tag)) {
			final var witnesses = getExtensionTags("_WITN");
			if (!witnesses.isEmpty()) {
				HTMLUtils.appendTextAfterLineBreaks(sb, String.join(HTMLUtils.LINE_BREAK, witnesses.stream().map(t -> String.format(Format.KEY_VALUE, I18N.get("Witnesses"), t.getValue())).toList()), 2);
			}
		} else if (DEATH_TAGS.contains(tag)) {
			final var cause = getCause();
			if (!cause.isEmpty()) {
				HTMLUtils.appendTextAfterLineBreaks(sb, String.format(Format.KEY_VALUE, I18N.get("Cause"), cause), 2);
			}
		}

		final var commonsOptions = HTMLOption.with(HTMLOption.without(options, HTMLOption.COMMONS_HEADINGS), HTMLOption.MEDIA_SOURCES);
		HTMLUtils.appendText(sb, noteManager.toHTML(commonsOptions));
		HTMLUtils.appendText(sb, mediaManager.toHTML(commonsOptions));
		HTMLUtils.appendText(sb, sourceCitationManager.toHTML(commonsOptions));

		return sb.toString();
	}

	private String getLocaleTag() {
		return I18N.get(getTag());
	}
	/* #endregion */

	@Override
	public String getLink() {
		return String.format(Format.PADDED_PIPE_SEPARATED, parentStructure.getLink(), getLocaleTag());
	}
}
