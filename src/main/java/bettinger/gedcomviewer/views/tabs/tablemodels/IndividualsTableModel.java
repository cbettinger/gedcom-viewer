package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Date;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.TableModel;

public class IndividualsTableModel extends TableModel<Individual> {

	public IndividualsTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Name"), I18N.get("DateOfBirth"), I18N.get("PlaceOfBirth"), I18N.get("DateOfDeath"), I18N.get("PlaceOfDeath"), I18N.get("LastChange") }, gedcom.getIndividuals(searchQuery));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var individual = getItemAt(row);

		final var birthDate = individual.getBirthDate() != null ? individual.getBirthDate() : individual.getBaptismDate();
		final var deathDate = individual.getDeathDate() != null ? individual.getDeathDate() : individual.getBurialDate();

		final var birthLocation = individual.getBirthLocation() != null ? individual.getBirthLocation() : individual.getBaptismLocation();
		final var birthPlace = !individual.getBirthPlace().isEmpty() ? individual.getBirthPlace() : individual.getBaptismPlace();
		final var birthPlaceStr = birthLocation != null ? birthLocation.toString() : birthPlace;

		final var deathLocation = individual.getDeathLocation() != null ? individual.getDeathLocation() : individual.getBurialLocation();
		final var deathPlace = !individual.getDeathPlace().isEmpty() ? individual.getDeathPlace() : individual.getBurialPlace();
		final var deathPlaceStr = deathLocation != null ? deathLocation.toString() : deathPlace;

		return switch (col) {
			case 0 -> individual.getNumber();
			case 1 -> individual.getName();
			case 2 -> birthDate;
			case 3 -> birthPlaceStr;
			case 4 -> deathDate;
			case 5 -> deathPlaceStr;
			case 6 -> individual.getLastChange();
			default -> "";
		};
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return switch (column) {
			case 0 -> Integer.class;
			case 2, 4 -> Date.class;
			case 6 -> LocalDateTime.class;
			default -> super.getColumnClass(column);
		};
	}
}
