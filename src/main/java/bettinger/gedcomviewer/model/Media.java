package bettinger.gedcomviewer.model;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

public class Media extends Structure implements Record, NoteContainer, SourceCitationContainer {

	static final String TAG = "OBJE";

	private static final List<String> DISPLAYABLE_IMAGE_FORMATS = Arrays.asList("jpg", "jpeg", "png", "gif");
	private static final String STG_PREFIX = "stg:";

	private final RecordManager recordManager;
	private final NoteManager noteManager;
	private final SourceCitationManager sourceCitationManager;

	private String title;
	private String filePath;
	private String format;
	private Type type;

	Media(final GEDCOM gedcom, final org.folg.gedcom.model.Media media) {
		super(gedcom, media.getId(), media);

		this.recordManager = new RecordManager(this, gedcom, media.getChange());
		this.noteManager = new NoteManager(this, gedcom, media.getNoteRefs());
		this.sourceCitationManager = new SourceCitationManager(this, gedcom);

		this.title = media.getTitle();
		if (this.title == null) {
			this.title = getFirstExtensionTagValue("TITL");
		}

		final var mediaFilePath = media.getFile();
		if (mediaFilePath.startsWith(STG_PREFIX)) {
			final String gedcomFileBaseName = FileUtils.getBaseName(gedcom.getFileName());
			this.filePath = FileUtils.getPath(gedcom.getDirectoryPath(), gedcomFileBaseName, mediaFilePath.replaceFirst(STG_PREFIX, ""));
		} else {
			this.filePath = FileUtils.getPath(gedcom.getDirectoryPath(), mediaFilePath);
		}

		final var formatTag = getFirstExtensionTag("FORM");

		this.format = media.getFormat();
		if (this.format == null) {
			this.format = formatTag == null ? "" : formatTag.getValue();
		}

		this.type = Type.fromValue(media.getType());
		if (this.type == null) {
			this.type = formatTag == null ? null : Type.fromValue(TagUtils.parseChildTagValue(formatTag, "TYPE"));
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
	public void setSources() {
		sourceCitationManager.setSources();
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
	public String getTitle() {
		if (title != null && !title.isEmpty()) {
			return title;
		}
		return getId();
	}

	public String getURL() {
		try {
			return getFile().toURI().toURL().toString();
		} catch (final Exception _) {
			return "";
		}
	}

	public boolean exists() {
		return FileUtils.exists(getFile());
	}

	public File getFile() {
		return new File(filePath);
	}

	public Type getType() {
		return type;
	}

	public Image getImage() {
		Image result = null;

		if (isImage()) {
			try {
				result = ImageIO.read(getFile());
			} catch (final IOException _) {
				// intentionally left blank
			}
		}

		return result;
	}

	public boolean isImage() {
		return Media.DISPLAYABLE_IMAGE_FORMATS.contains(getFormat().toLowerCase());
	}

	public String getFormat() {
		return format.toUpperCase();
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		HTMLUtils.appendH1(sb, getTitle());

		if (exists()) {
			if (isImage()) {
				HTMLUtils.appendH2(sb, I18N.get("Preview"));
				if (options.contains(HTMLOption.EXPORT)) {
					HTMLUtils.appendImage(sb, getURL());
				} else {
					HTMLUtils.appendImage(sb, getURL(), Constants.PREVIEW_IMAGE_WIDTH);
				}
				HTMLUtils.appendLineBreak(sb);
			}

			if (!options.contains(HTMLOption.NO_OPEN_MEDIA_LINK)) {
				if (options.contains(HTMLOption.EXPORT)) {
					HTMLUtils.appendText(sb, HTMLUtils.createDownloadLink(getURL(), I18N.get("OpenFile")));
				} else {
					HTMLUtils.appendText(sb, HTMLUtils.createLink(getURL(), I18N.get("OpenFile")));
				}
			}
		}

		HTMLUtils.appendText(sb, noteManager.toHTML(options));
		HTMLUtils.appendText(sb, sourceCitationManager.toHTML(options));
		HTMLUtils.appendText(sb, recordManager.toHTML(options));

		return sb.toString();
	}
	/* #endregion */

	public enum Type {
		AUDIO, BOOK, CARD, ELECTRONIC, FICHE, FILM, MAGAZINE, MANUSCRIPT, MAP, NEWSPAPER, PHOTO, TOMBSTONE, VIDEO;

		private static final Map<String, Type> VALUES = new HashMap<>();

		static {
			for (final var v : values()) {
				VALUES.put(v.toString().toLowerCase(), v);
			}
		}

		public static Type fromValue(final String value) {
			return value == null ? null : VALUES.get(value.toLowerCase());
		}
	}
}
