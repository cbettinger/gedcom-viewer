package bettinger.gedcomviewer.views.tabs;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import bettinger.gedcomviewer.Constants;

public class DefaultTableContainer extends TableContainer {

	public DefaultTableContainer(final Class<?> tableModelClass) {
		super(tableModelClass);

		final var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), new JScrollPane(detailsSideBar));
		splitPane.setResizeWeight(1.0);
		splitPane.setDividerLocation(Constants.DEFAULT_FRAME_WIDTH - Constants.DEFAULT_RIGHT_SIDEBAR_WIDTH);
		add(splitPane, BorderLayout.CENTER);
	}
}
