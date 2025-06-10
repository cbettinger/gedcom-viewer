package bettinger.gedcomviewer.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import bettinger.gedcomviewer.utils.HTMLUtils;

public class Note extends Structure implements Record, SourceCitationContainer {

	static final String TAG = "NOTE";

	private final RecordManager recordManager;
	private final SourceCitationManager sourceCitationManager;

	final org.folg.gedcom.model.Note wrappedNote;

	public Note(final GEDCOM gedcom, final org.folg.gedcom.model.Note note) {
		super(gedcom, note.getId(), note);

		this.recordManager = new RecordManager(this, gedcom, note.getChange());
		this.sourceCitationManager = new SourceCitationManager(this, gedcom);

		this.wrappedNote = note;
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
	public void setSources() {
		sourceCitationManager.setSources(wrappedNote.getSourceCitations());
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
	public String getText() {
		return wrappedNote.getValue() == null ? "" : wrappedNote.getValue().trim();
	}

	public void setText(final String text) {
		if (text != null) {
			wrappedNote.setValue(text);
		}
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		return getId();
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		HTMLUtils.appendText(sb, HTMLUtils.convertStringToHTML(getText()));

		if (options.contains(HTMLOption.COMMONS)) {
			HTMLUtils.appendText(sb, sourceCitationManager.toHTML(options));
			HTMLUtils.appendText(sb, recordManager.toHTML(options));
		}

		return sb.toString();
	}
	/* #endregion */
}
