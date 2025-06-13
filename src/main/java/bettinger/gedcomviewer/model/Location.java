package bettinger.gedcomviewer.model;

import java.awt.Rectangle;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.folg.gedcom.model.GedcomTag;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.utils.DateTimeUtils;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Location extends Structure implements Record, NoteContainer, MediaContainer {

	static final String TAG = "_LOC";
	static final String TAG_PLACE = "PLAC";

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

	private final boolean isStructure;

	Location(final GEDCOM gedcom, final GedcomTag tag) {
		super(gedcom, tag.getId(), null);

		this.recordManager = new RecordManager(this, gedcom, DateTimeUtils.parseLastChange(tag));
		this.noteManager = new NoteManager(this, gedcom, tag);
		this.mediaManager = new MediaManager(this, gedcom, tag);

		this.name = parseName(tag);

		final var mapTag = TagUtils.getChildTag(tag, "MAP");
		this.latitude = parseLatitude(mapTag);
		this.longitude = parseLongitude(mapTag);

		final var image = getPrimaryImage(false);
		this.imageURL = image != null && image.exists() && !image.getURL().isEmpty() ? image.getURL() : "";

		this.isStructure = true;
	}

	Location(final GEDCOM gedcom, final String place, final float latitude, final float longitude) {
		super(gedcom, constructId(TAG_PLACE, place), null);

		this.recordManager = new RecordManager(this, gedcom, LocalDateTime.now());
		this.noteManager = new NoteManager(this, gedcom, new ArrayList<>());
		this.mediaManager = new MediaManager(this, gedcom, new ArrayList<>());

		this.name = place;

		this.latitude = latitude;
		this.longitude = longitude;

		final var image = getPrimaryImage(false);
		this.imageURL = image != null && image.exists() && !image.getURL().isEmpty() ? image.getURL() : "";

		this.isStructure = false;
	}

	/* #region container */
	@Override
	public GEDCOM getGEDCOM() {
		return recordManager.getGEDCOM();
	}

	@Override
	public boolean hasXRef() {
		return isStructure;
	}

	@Override
	public int getNumber() {
		return recordManager.getNumber();
	}

	@Override
	public LocalDateTime getLastChange() {
		var result = recordManager.getLastChange();

		if (!isStructure) {
			final Structure newestReference = recordManager.getReferences().stream().filter(Fact.class::isInstance).max(Comparator.comparing(f -> ((Fact) f).getLastChange())).orElse(null);
			if (newestReference instanceof Fact f) {
				result = f.getLastChange();
			}
		}

		return result;
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
	public Media getPrimaryImage(final boolean onlyPhoto) {
		return mediaManager.getPrimaryImage(onlyPhoto);
	}

	@Override
	public Rectangle getImageClip(final Media image) {
		return mediaManager.getImageClip(image);
	}
	/* #endregion */

	/* #region getter & setter */
	boolean isStructure() {
		return isStructure;
	}

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
			mediaManager.appendPrimaryImage(sb);
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

	static float parseLatitude(final GedcomTag mapTag) {
		try {
			return Float.parseFloat(parseMapCoordinate(mapTag, "LATI"));
		} catch (final NumberFormatException _) {
			return 0;
		}
	}

	static float parseLongitude(final GedcomTag mapTag) {
		try {
			return Float.parseFloat(parseMapCoordinate(mapTag, "LONG"));
		} catch (final NumberFormatException _) {
			return 0;
		}
	}

	private static String parseMapCoordinate(final GedcomTag mapTag, final String dimension) {
		if (mapTag != null) {
			var value = TagUtils.parseChildTagValue(mapTag, dimension);

			if (value == null) {
				return UNKNOWN_STRING;
			} else if (value.startsWith("N")) {
				value = value.replaceFirst("N", "");
			} else if (value.startsWith("S")) {
				value = value.replaceFirst("S", "-");
			} else if (value.startsWith("W")) {
				value = value.replaceFirst("W", "-");
			} else if (value.startsWith("E")) {
				value = value.replaceFirst("E", "");
			}

			return value;
		}

		return UNKNOWN_STRING;
	}
}
