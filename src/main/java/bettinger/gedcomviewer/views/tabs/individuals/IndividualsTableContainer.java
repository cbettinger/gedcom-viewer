package bettinger.gedcomviewer.views.tabs.individuals;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.views.tabs.TableContainer;
import bettinger.gedcomviewer.views.tabs.individuals.navigation.NavigationSideBar;
import bettinger.gedcomviewer.views.tabs.tablemodels.IndividualsTableModel;

public class IndividualsTableContainer extends TableContainer {

	public IndividualsTableContainer() {
		super(IndividualsTableModel.class);

		final var splitPaneRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), new JScrollPane(detailsSideBar));
		splitPaneRight.setResizeWeight(1.0);
		splitPaneRight.setDividerLocation(Constants.DEFAULT_FRAME_WIDTH - Constants.DEFAULT_RIGHT_SIDEBAR_WIDTH - Constants.DEFAULT_LEFT_SIDEBAR_WIDTH);

		final var splitPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new NavigationSideBar(), splitPaneRight);
		splitPaneLeft.setResizeWeight(0.0);
		splitPaneLeft.setDividerLocation(Constants.DEFAULT_LEFT_SIDEBAR_WIDTH);
		splitPaneLeft.setDividerSize(20);
		splitPaneLeft.setOneTouchExpandable(true);
		add(splitPaneLeft, BorderLayout.CENTER);
	}
}
