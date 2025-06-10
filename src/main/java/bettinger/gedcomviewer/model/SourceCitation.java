package bettinger.gedcomviewer.model;

import java.util.Set;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.utils.HTMLUtils;

public class SourceCitation extends Substructure {

	private final Source source;
	private final String page;
	private final Quality quality;

	SourceCitation(final GEDCOM gedcom, final org.folg.gedcom.model.SourceCitation sourceCitation, final Structure parentStructure) {
		super(gedcom, "CITE", sourceCitation, parentStructure);

		this.source = (Source) gedcom.getRecord(sourceCitation.getRef());
		this.page = sourceCitation.getPage();
		this.quality = sourceCitation.getQuality() == null ? Quality.UNKNOWN : Quality.fromValue(Integer.parseInt(sourceCitation.getQuality()));
	}

	/* #region getter & setter */
	public Source getSource() {
		return source;
	}

	public String getPage() {
		return page;
	}

	public Quality getQuality() {
		return quality;
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		final var sb = new StringBuilder(String.format(Format.PADDED_PIPE_SEPARATED, getParentStructure().toString(), source));

		if (!(page == null || page.isEmpty())) {
			sb.append(String.format(Format.TRAILING_SPACE_COLON_WITH_SUFFIX, page));
		}

		return sb.toString();
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder(getParentStructure().getLink());

		if (!(page == null || page.isEmpty())) {
			sb.append(String.format(Format.TRAILING_SPACE_COLON_WITH_SUFFIX, page));
		}

		return sb.toString();
	}
	/* #endregion */

	@Override
	public String getLink() {
		final var sb = new StringBuilder(source.getLink());

		if (!(page == null || page.isEmpty())) {
			sb.append(String.format(Format.TRAILING_SPACE_COLON_WITH_SUFFIX, page));
		}

		return HTMLUtils.convertStringToHTML(sb.toString());
	}
}
