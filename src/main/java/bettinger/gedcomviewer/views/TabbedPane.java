package bettinger.gedcomviewer.views;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Location;
import bettinger.gedcomviewer.model.Media;
import bettinger.gedcomviewer.model.Note;
import bettinger.gedcomviewer.model.Occupation;
import bettinger.gedcomviewer.model.Record;
import bettinger.gedcomviewer.model.Repository;
import bettinger.gedcomviewer.model.Source;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.views.tabs.DefaultTableContainer;
import bettinger.gedcomviewer.views.tabs.TableContainer;
import bettinger.gedcomviewer.views.tabs.individuals.IndividualsTableContainer;
import bettinger.gedcomviewer.views.tabs.map.MapPanel;
import bettinger.gedcomviewer.views.tabs.tablemodels.FamiliesTableModel;
import bettinger.gedcomviewer.views.tabs.tablemodels.LocationsTableModel;
import bettinger.gedcomviewer.views.tabs.tablemodels.MediaTableModel;
import bettinger.gedcomviewer.views.tabs.tablemodels.NotesTableModel;
import bettinger.gedcomviewer.views.tabs.tablemodels.OccupationsTableModel;
import bettinger.gedcomviewer.views.tabs.tablemodels.RepositoriesTableModel;
import bettinger.gedcomviewer.views.tabs.tablemodels.SourcesTableModel;

class TabbedPane extends JTabbedPane {

	private GEDCOM gedcom;

	private final Map<Class<? extends Record>, TableContainer> tableContainers;

	private final SearchField searchField;

	private Record selectedRecord;

	TabbedPane() {
		tableContainers = new HashMap<>();

		searchField = new SearchField();
		searchField.addActionListener(_ -> Events.post(new UI.SearchCommand(gedcom, searchField.getText())));

		final var trailingToolBar = new JToolBar();
		trailingToolBar.add(Box.createHorizontalGlue());
		trailingToolBar.add(searchField);
		putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, trailingToolBar);

		addTab(I18N.get("Individuals"), new IndividualsTableContainer(), Individual.class);
		addTab(I18N.get("Families"), new DefaultTableContainer(FamiliesTableModel.class), Family.class);
		addTab(I18N.get("Locations"), new DefaultTableContainer(LocationsTableModel.class), Location.class);
		addTab(I18N.get("Map"), new MapPanel());
		addTab(I18N.get("Occupations"), new DefaultTableContainer(OccupationsTableModel.class), Occupation.class);
		addTab(I18N.get("Media"), new DefaultTableContainer(MediaTableModel.class), Media.class);
		addTab(I18N.get("Sources"), new DefaultTableContainer(SourcesTableModel.class), Source.class);
		addTab(I18N.get("Repositories"), new DefaultTableContainer(RepositoriesTableModel.class), Repository.class);
		addTab(I18N.get("Notes"), new DefaultTableContainer(NotesTableModel.class), Note.class);

		addChangeListener(_ -> {
			for (final var tableContainer : tableContainers.values()) {
				tableContainer.clearSelection();
			}

			postTabSelectedEvent();
		});

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				gedcom = event.getGEDCOM();

				Events.post(new UI.RecordSelectedEvent(null));
				postTabSelectedEvent();
			}

			@Subscribe
			void onHyperlinkEvent(final HyperlinkEvent event) {
				final var target = gedcom.getRecord(HTMLUtils.getAnchorFromLink(event.getDescription()));
				if (target != null) {
					searchField.clear();
					Events.post(new UI.SelectRecordCommand(target));
				}
			}

			@Subscribe
			void onSelectRecordCommand(final UI.SelectRecordCommand event) {
				final var recordToSelect = event.getRecord();
				if (recordToSelect != null) {
					final var tableContainer = tableContainers.get(recordToSelect.getClass());
					if (tableContainer != null) {
						setSelectedComponent(tableContainer);
						tableContainer.selectItem(recordToSelect);
					}
				}
			}

			@Subscribe
			void onRecordSelectedEvent(final UI.RecordSelectedEvent event) {
				selectedRecord = event.getRecord();
			}
		});
	}

	private void addTab(String title, TableContainer tableContainer, Class<? extends Record> modelClass) {
		addTab(title, tableContainer);

		if (modelClass != null) {
			tableContainers.put(modelClass, tableContainer);
		}
	}

	@Override
	public void addTab(String title, Component component) {
		if (component instanceof JComponent c) {
			c.setBorder(BorderFactory.createEmptyBorder(Constants.BORDER_SIZE, Constants.BORDER_SIZE, Constants.BORDER_SIZE, Constants.BORDER_SIZE));
		}

		super.addTab(title, component);
	}

	private void postTabSelectedEvent() {
		Events.post(new UI.TabSelectedEvent(getSelectedTab(), gedcom));
	}

	Component getSelectedTab() {
		return getComponentAt(getSelectedIndex());
	}

	Record getSelectedRecord() {
		return selectedRecord;
	}
}
