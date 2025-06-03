package bettinger.gedcomviewer.model;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.folg.gedcom.model.GedcomTag;
import org.folg.gedcom.model.NoteRef;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

class NoteManager {

	private static final String SIGN = "☰";

	private final List<Note> notes;

	NoteManager(final Structure referencingStructure, final GEDCOM gedcom) {
		this(referencingStructure, gedcom, referencingStructure.getExtensionTags(Note.TAG).stream().map(noteTag -> {
			final var noteRef = new NoteRef();
			noteRef.setRef(noteTag.getRef());
			return noteRef;
		}).toList());
	}

	NoteManager(final Structure referencingStructure, final GEDCOM gedcom, final GedcomTag tag) {
		this(referencingStructure, gedcom, TagUtils.getChildTags(tag, Note.TAG).stream().map(noteTag -> {
			final var noteRef = new NoteRef();
			noteRef.setRef(noteTag.getRef());
			return noteRef;
		}).toList());
	}

	NoteManager(final Structure referencingStructure, final GEDCOM gedcom, final List<NoteRef> noteRefs) {
		this.notes = noteRefs.stream().map(nr -> (Note) gedcom.getRecord(nr.getRef())).distinct().toList();

		for (final var n : this.notes) {
			n.addReference(referencingStructure);
		}
	}

	public List<Note> getNotes() {
		return getNotes(false);
	}

	public List<Note> getNotes(final boolean excludeConfidential) {
		return excludeConfidential ? notes.stream().filter(Predicate.not(Structure::isConfidential)).toList() : notes;
	}

	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var publicNotes = options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) ? notes.stream().filter(Predicate.not(Structure::isConfidential)).toList() : notes;
		if (!publicNotes.isEmpty()) {
			if (options.contains(HTMLOption.COMMONS_HEADINGS)) {
				HTMLUtils.appendH2(sb, I18N.get("Notes"));
			} else {
				HTMLUtils.appendLineBreaks(sb, 2);
			}

			HTMLUtils.appendText(sb, HTMLUtils.createList(publicNotes, n -> n.toHTML(HTMLOption.without(options, HTMLOption.COMMONS)), options.contains(HTMLOption.COMMONS_HEADINGS) ? null : SIGN, options.contains(HTMLOption.COMMONS_HEADINGS)));
		}

		return sb.toString();
	}
}
