package bettinger.gedcomviewer.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface Record {
	GEDCOM getGEDCOM();

	boolean hasXRef();
	String getId();
	int getNumber();
	LocalDateTime getLastChange();

	void addReference(final Structure referencingStructure);
	List<Structure> getReferences();

	String toHTML(final Set<HTMLOption> options);
	String toHTML();
}
