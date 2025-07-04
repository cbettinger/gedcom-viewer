package bettinger.gedcomviewer.model;

import java.awt.Rectangle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.folg.gedcom.model.GedcomTag;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.utils.DateTimeUtils;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Location extends Structure implements RegularRecord, NoteContainer, MediaContainer {

	static final String TAG = "_LOC";

	private final RecordManager recordManager;
	private final NoteManager noteManager;
	private final MediaManager mediaManager;

	@JsonProperty
	private final String name;
	@JsonProperty
	private final float latitude;
	@JsonProperty
	private final float longitude;
	@JsonProperty
	private final String imageURL;

	Location(final GEDCOM gedcom, final GedcomTag tag) {
		super(gedcom, tag.getId(), null);

		this.recordManager = new RecordManager(this, gedcom, DateTimeUtils.parseLastChange(tag));
		this.noteManager = new NoteManager(this, gedcom, tag);
		this.mediaManager = new MediaManager(this, gedcom, tag);

		this.name = parseName(tag);
		this.latitude = parseLatitude(tag);
		this.longitude = parseLongitude(tag);
		this.imageURL = getPrimaryImageURL(false);
	}

	/* #region container */
	@Override
	public GEDCOM getGEDCOM() {
		return recordManager.getGEDCOM();
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
	/* #endregion */

	/* #region getter & setter */
	public String getName() {
		return name;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	@JsonProperty("references")
	private List<String> getFactLabels() {
		return recordManager.getReferences().stream().filter(Fact.class::isInstance).map(f -> ((Fact) f).getLabel()).toList();
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		HTMLUtils.appendH1(sb, getName());

		if (!options.contains(HTMLOption.EXPORT)) {
			mediaManager.appendPrimaryImage(sb, false, Constants.PREVIEW_IMAGE_WIDTH);
		}

		if (Math.signum(latitude) != 0 && Math.signum(longitude) != 0) {
			HTMLUtils.appendH2(sb, I18N.get("Coordinates"));

			final var latitudeSuffix = latitude < 0 ? "S" : "N";
			final var longitudeSuffix = longitude < 0 ? "W" : "E";
			HTMLUtils.appendLine(sb, String.format(Format.SPACED, Math.abs(latitude), latitudeSuffix));
			HTMLUtils.appendText(sb, String.format(Format.SPACED, Math.abs(longitude), longitudeSuffix));
		}

		String urlEncodedName = HTMLUtils.encode(name);

		HTMLUtils.appendH2(sb, I18N.get("Maps"));
		if (Math.signum(latitude) != 0 && Math.signum(longitude) != 0) {
			HTMLUtils.appendLine(sb, String.format("<a target=\"blank\" href=\"https://www.google.com/maps/place/%s+%s/@%s,%s,12z\">Google Maps</a>", latitude, longitude, latitude, longitude));
			HTMLUtils.appendLine(sb, String.format("<a target=\"blank\" href=\"https://www.openstreetmap.org/?mlat=%s&mlon=%s#map=12\">OpenStreetMap</a>", latitude, longitude));
			HTMLUtils.appendText(sb, String.format("<a target=\"blank\" href=\"https://www.opentopomap.org/#marker=12/%s/%s\">OpenTopoMap</a>", latitude, longitude));
		} else {
			HTMLUtils.appendText(sb, String.format("<a target=\"blank\" href=\"https://www.google.com/maps/search/?api=1&query=%s\">Google Maps</a>", urlEncodedName));
		}

		if (!options.contains(HTMLOption.EXPORT)) {
			HTMLUtils.appendH2(sb, I18N.get("Research"));
			HTMLUtils.appendLine(sb, String.format("<a target=\"blank\" href=\"https://www.google.com/search?q=%s\">Google</a>", urlEncodedName));
			HTMLUtils.appendLine(sb, String.format("<a target=\"blank\" href=\"https://duckduckgo.com/?q=%s\">DuckDuckGo</a>", urlEncodedName));
			HTMLUtils.appendLine(sb, String.format("<a target=\"blank\" href=\"https://%s.wikipedia.org/wiki/%s\">Wikipedia</a>", Preferences.getLanguageTag(), urlEncodedName));
			HTMLUtils.appendLine(sb, String.format("<a target=\"blank\" href=\"https://gov.genealogy.net/search/name?name=%s\">CompGen GOV</a>", urlEncodedName));
			HTMLUtils.appendText(sb, String.format("<a target=\"blank\" href=\"https://s.meyersgaz.org/search?search=%s\">Meyers Gazetteer</a>", urlEncodedName));
		}

		HTMLUtils.appendText(sb, noteManager.toHTML(options));
		HTMLUtils.appendText(sb, mediaManager.toHTML(options));
		HTMLUtils.appendText(sb, recordManager.toHTML(options));

		return sb.toString();
	}
	/* #endregion */

	private static String parseName(final GedcomTag tag) {
		final var name = TagUtils.parseChildTagValue(tag, "NAME");
		return name == null ? UNKNOWN_STRING : name;
	}

	private static float parseLatitude(final GedcomTag tag) {
		try {
			return Float.parseFloat(parseMapCoordinate(tag, "LATI"));
		} catch (final NumberFormatException _) {
			return 0;
		}
	}

	private static float parseLongitude(final GedcomTag tag) {
		try {
			return Float.parseFloat(parseMapCoordinate(tag, "LONG"));
		} catch (final NumberFormatException _) {
			return 0;
		}
	}

	private static String parseMapCoordinate(final GedcomTag tag, final String dimension) {
		final var mapTag = TagUtils.getChildTag(tag, "MAP");
		if (mapTag != null) {
			final var value = TagUtils.parseChildTagValue(mapTag, dimension);
			return value == null ? UNKNOWN_STRING : value;
		}

		return UNKNOWN_STRING;
	}
}
