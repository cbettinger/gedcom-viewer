package bettinger.gedcomviewer.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;

public class Occupation extends Structure implements DerivedRecord {

	static final String TAG = "OCCU";

	private final RecordManager recordManager;

	private final String name;
	private final List<Individual> individuals;

	Occupation(final GEDCOM gedcom, final String name, final List<Individual> individuals) {
		super(gedcom, constructId(TAG, name), null);

		this.recordManager = new RecordManager(this, gedcom, individuals.stream().map(Record::getLastChange).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null));

		this.name = name;
		this.individuals = individuals;

		for (final var i : individuals) {
			i.addOccupations(this);
		}
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
	/* #endregion */

	/* #region getter & setter */
	public String getName() {
		return name;
	}

	public int getCount() {
		return individuals.size();
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

		HTMLUtils.appendH1(sb, getName());

		var publicIndividuals = individuals;
		if (options.contains(HTMLOption.NO_CONFIDENTIAL_DATA)) {
			publicIndividuals = publicIndividuals.stream().filter(Predicate.not(Structure::isConfidential)).toList();
		}

		if (!publicIndividuals.isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Individuals"));
			HTMLUtils.appendText(sb, HTMLUtils.createList(publicIndividuals, i -> i.getLink()));
		}

		return sb.toString();
	}
	/* #endregion */

	static String getLinkFromFact(final Fact occupationFact) {
		return HTMLUtils.createAnchorLink(constructId(TAG, occupationFact.getValue()), occupationFact.getValue());
	}
}
