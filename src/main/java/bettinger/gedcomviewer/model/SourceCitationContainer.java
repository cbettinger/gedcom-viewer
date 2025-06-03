package bettinger.gedcomviewer.model;

import java.util.List;

public interface SourceCitationContainer {
	void setSources();
	List<SourceCitation> getSourceCitations();
	List<SourceCitation> getSourceCitations(final boolean excludeConfidential);
}
