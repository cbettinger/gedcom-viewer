package bettinger.gedcomviewer.views.tabs.individuals.navigation.tabs.descendants;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Quintet;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Quality;
import bettinger.gedcomviewer.utils.Numbering;
import bettinger.gedcomviewer.views.TableModel;

class DescendantsTableModel extends TableModel<Quintet<String, Individual, Family, Individual, Integer>> {

	DescendantsTableModel() {
		this(new ArrayList<>());
	}

	DescendantsTableModel(final List<Quintet<String, Individual, Family, Individual, Integer>> descendants) {
		super(new String[] { I18N.get("GenerationAbbreviation"), I18N.get("dAboville"), I18N.get("Name"), I18N.get("BirthAbbreviation"), I18N.get("MarriageAbbreviation"), I18N.get("DeathAbbreviation") }, descendants);
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return switch (column) {
			case 3, 4, 5 -> Quality.class;
			default -> super.getColumnClass(column);
		};
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var entry = getItemAt(row);
		final var number = entry.getValue0();
		final var individual = entry.getValue1();
		final var family = entry.getValue2();
		final var generation = entry.getValue4();

		return switch (col) {
			case 0 -> Numbering.getRoman(generation);
			case 1 -> number;
			case 2 -> individual.getName();
			case 3 -> individual.getBirthQuality();
			case 4 -> family != null ? family.getMarriageQuality() : Quality.UNKNOWN;
			case 5 -> individual.getDeathQuality();
			default -> "";
		};
	}
}
