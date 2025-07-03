package bettinger.gedcomviewer.model;

import java.awt.Rectangle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;

public class Source extends Structure implements Record, NoteContainer, MediaContainer {

	static final String TAG = "SOUR";

	private final RecordManager recordManager;
	private final NoteManager noteManager;
	private final MediaManager mediaManager;

	final org.folg.gedcom.model.Source wrappedSource;

	private Repository repository;

	public Source(final GEDCOM gedcom, final org.folg.gedcom.model.Source source) {
		super(gedcom, source.getId(), source);

		this.recordManager = new RecordManager(this, gedcom, source.getChange());
		this.noteManager = new NoteManager(this, gedcom, source.getNoteRefs());
		this.mediaManager = new MediaManager(this, gedcom, source.getMediaRefs());

		this.wrappedSource = source;

		final var repositoryRef = source.getRepositoryRef();
		this.repository = repositoryRef == null ? null : (Repository) gedcom.getRecord(repositoryRef.getRef());
		if (this.repository != null) {
			this.repository.addReference(this);
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
	public String getAbbreviation() {
		return wrappedSource.getAbbreviation() == null ? "" : wrappedSource.getAbbreviation();
	}

	public void setAbbreviation(final String value) {
		wrappedSource.setAbbreviation(value == null ? "" : value);
	}

	public String getAuthor() {
		return wrappedSource.getAuthor() == null ? "" : wrappedSource.getAuthor();
	}

	public void setAuthor(final String value) {
		wrappedSource.setAuthor(value == null ? "" : value);
	}

	public String getTitle() {
		final var title = wrappedSource.getTitle();
		if (title != null && !title.isEmpty()) {
			return title;
		}
		return getId();
	}

	public void setTitle(final String value) {
		wrappedSource.setTitle(value == null ? "" : value);
	}

	public String getPublication() {
		return wrappedSource.getPublicationFacts() == null ? "" : wrappedSource.getPublicationFacts();
	}

	public void setPublication(final String value) {
		wrappedSource.setPublicationFacts(value == null ? "" : value);
	}

	public String getText() {
		return wrappedSource.getText() == null ? "" : wrappedSource.getText();
	}

	public void setText(final String value) {
		wrappedSource.setText(value == null ? "" : value);
	}

	public void setRepository(final Repository value) {
		if (value == null) {
			wrappedSource.setRepositoryRef(null);
			repository = null;
		} else if (getGEDCOM().getRepositories().contains(value)) {
			final var repositoryRef = new org.folg.gedcom.model.RepositoryRef();
			repositoryRef.setRef(value.getId());
			wrappedSource.setRepositoryRef(repositoryRef);
			repository = value;
		}
	}

	public Repository getRepository() {
		return repository;
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

		if (!getTitle().isEmpty()) {
			HTMLUtils.appendH1(sb, getTitle());
		}

		if (!getAbbreviation().isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Abbreviation"));
			HTMLUtils.appendText(sb, HTMLUtils.convertStringToHTML(getAbbreviation()));
		}

		if (!getAuthor().isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Author"));
			HTMLUtils.appendText(sb, HTMLUtils.convertStringToHTML(getAuthor()));
		}

		if (!getPublication().isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Publication"));
			HTMLUtils.appendText(sb, HTMLUtils.convertStringToHTML(getPublication()));
		}

		if (!getText().isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Text"));
			HTMLUtils.appendText(sb, HTMLUtils.convertStringToHTML(getText()));
		}

		if (getRepository() != null) {
			HTMLUtils.appendH2(sb, I18N.get("Repository"));
			HTMLUtils.appendText(sb, getRepository().getLink());
		}

		HTMLUtils.appendText(sb, noteManager.toHTML(options));
		HTMLUtils.appendText(sb, mediaManager.toHTML(options));

		if (!recordManager.getReferences().isEmpty() && options.contains(HTMLOption.REFERENCES)) {
			HTMLUtils.appendH2(sb, I18N.get("References"));
			HTMLUtils.appendText(sb, HTMLUtils.createList(recordManager.getReferences(), s -> s.toHTML(options)));
		}

		return sb.toString();
	}
	/* #endregion */
}
