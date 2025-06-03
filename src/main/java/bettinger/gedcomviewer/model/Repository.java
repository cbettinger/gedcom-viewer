package bettinger.gedcomviewer.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import bettinger.gedcomviewer.utils.HTMLUtils;

public class Repository extends Structure implements RegularRecord, NoteContainer {

	static final String TAG = "REPO";

	private final RecordManager recordManager;
	private final NoteManager noteManager;

	final org.folg.gedcom.model.Repository wrappedRepository;

	private final Address address;

	public Repository(final GEDCOM gedcom, final org.folg.gedcom.model.Repository repository) {
		super(gedcom, repository.getId(), repository);

		this.recordManager = new RecordManager(this, gedcom, repository.getChange());
		this.noteManager = new NoteManager(this, gedcom, repository.getNoteRefs());

		this.wrappedRepository = repository;

		this.address = new Address(gedcom, repository, this);
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
	/* #endregion */

	/* #region getter & setter */
	public String getName() {
		final var name = wrappedRepository.getName();
		if (name != null && !name.isEmpty()) {
			return name;
		}
		return getId();
	}

	public void setName(final String value) {
		wrappedRepository.setName(value == null ? "" : value);
	}

	public Address getAddress() {
		return address;
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

		if (!getName().isEmpty()) {
			HTMLUtils.appendH1(sb, getName());
		}

		if (getAddress() != null) {
			final var addressElement = getAddress().toHTML(options);
			if (!addressElement.isEmpty()) {
				HTMLUtils.appendText(sb, addressElement);
			}
		}

		HTMLUtils.appendText(sb, noteManager.toHTML(options));
		HTMLUtils.appendText(sb, recordManager.toHTML(options));

		return sb.toString();
	}
	/* #endregion */
}
