package bettinger.gedcomviewer.model;

import java.awt.Rectangle;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.folg.gedcom.model.GedcomTag;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;

public class Submitter extends Structure implements Record, NoteContainer, MediaContainer {

	static final String TAG = "SUBM";
	static final String TAG_LANGUAGE = "LANG";

	private final RecordManager recordManager;
	private final NoteManager noteManager;
	private final MediaManager mediaManager;

	private final org.folg.gedcom.model.Submitter wrappedSubmitter;

	private final Address address;

	private List<String> languages;

	Submitter(final GEDCOM gedcom, final org.folg.gedcom.model.Submitter submitter) {
		super(gedcom, submitter.getId(), submitter);

		this.recordManager = new RecordManager(this, gedcom, submitter.getChange());
		this.noteManager = new NoteManager(this, gedcom);
		this.mediaManager = new MediaManager(this, gedcom);

		this.wrappedSubmitter = submitter;

		this.address = submitter.getAddress() != null ? new Address(gedcom, submitter, this) : null;

		parse();
	}

	private void parse() {
		languages = new ArrayList<>();

		if (wrappedSubmitter.getLanguage() != null && !wrappedSubmitter.getLanguage().isEmpty()) {
			languages.add(wrappedSubmitter.getLanguage());
		}

		final var langs = getExtensionTags(TAG_LANGUAGE);
		if (!langs.isEmpty()) {
			langs.stream().forEach(t -> {
				final var l = t.getValue();
				if (l != null && !l.isEmpty()) {
					languages.add(l);
				}
			});
		}
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
	public String getName() {
		final var name = wrappedSubmitter.getName();
		if (name != null && !name.isEmpty()) {
			return name;
		}
		return getId();
	}

	public Address getAddress() {
		return address;
	}

	public String getLanguage() {
		return wrappedSubmitter.getLanguage() == null ? "" : wrappedSubmitter.getLanguage();
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(final List<String> values) {
		if (!values.isEmpty()) {
			wrappedSubmitter.setLanguage(values.get(0));

			if (values.size() > 1) {
				replaceExtensionTags(TAG_LANGUAGE, values.stream().skip(1).map(s -> {
					final var tag = new GedcomTag(null, TAG_LANGUAGE, null);
					tag.setValue(s);
					return tag;
				}).toList());
			}

			parse();
		}
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		final var sb = new StringBuilder();

		sb.append(getName());

		if (getAddress() != null) {
			final var addressText = getAddress().toString();
			if (!addressText.isEmpty()) {
				sb.append(Format.TRAILING_SPACE_COMMA);

				final var lines = List.of(addressText.replace("\r\n", "\n").split("\n")).stream().filter(s -> !s.isEmpty()).toList();
				sb.append(String.join(Format.TRAILING_SPACE_COMMA, lines));
			}
		}

		return sb.toString();
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		HTMLUtils.appendH1(sb, getName());

		if (!options.contains(HTMLOption.EXPORT)) {
			mediaManager.appendPortrait(sb);
			HTMLUtils.appendLineBreaks(sb, 2);
		}

		if (getAddress() != null) {
			final var addressElement = getAddress().toHTML(options);
			if (!addressElement.isEmpty()) {
				HTMLUtils.appendText(sb, addressElement);
			}
		}

		if (!getLanguages().isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Languages"));
			HTMLUtils.appendText(sb, String.join(HTMLUtils.LINE_BREAK, getLanguages()));
		}

		HTMLUtils.appendText(sb, noteManager.toHTML(options));

		return sb.toString();
	}
	/* #endregion */
}
