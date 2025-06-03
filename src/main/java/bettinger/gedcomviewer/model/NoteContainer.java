package bettinger.gedcomviewer.model;

import java.util.List;

public interface NoteContainer {
	List<Note> getNotes();
	List<Note> getNotes(final boolean excludeConfidential);
}
