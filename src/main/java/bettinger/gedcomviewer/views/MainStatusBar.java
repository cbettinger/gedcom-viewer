package bettinger.gedcomviewer.views;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.views.tabs.IRecordCollectionView;

class MainStatusBar extends JPanel {

	private final JProgressBar exportProgressBar;

	private String selectedRecordId;

	MainStatusBar() {
		setLayout(new BorderLayout());

		final var numberOfRecordsLabel = new JLabel();
		numberOfRecordsLabel.setBorder(BorderFactory.createEmptyBorder(Constants.BORDER_SIZE, 2 * Constants.BORDER_SIZE, Constants.BORDER_SIZE, Constants.BORDER_SIZE));
		add(numberOfRecordsLabel, BorderLayout.WEST);

		final var idLabel = new JLabel();
		final var centerPanel = UI.wrap(idLabel);
		centerPanel.setToolTipText(I18N.get("DoubleClickToCopyToClipboard"));
		centerPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() == 2 && selectedRecordId != null) {
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selectedRecordId), null);
				}
			}
		});
		add(centerPanel, BorderLayout.CENTER);

		this.exportProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		this.exportProgressBar.setIndeterminate(true);
		this.exportProgressBar.setStringPainted(true);
		this.exportProgressBar.setVisible(false);
		this.exportProgressBar.setBorder(BorderFactory.createEmptyBorder(0, Constants.BORDER_SIZE, Constants.BORDER_SIZE, Constants.BORDER_SIZE));
		add(this.exportProgressBar, BorderLayout.EAST);

		this.selectedRecordId = null;

		Events.register(new Object() {

			@Subscribe
			void onTabSelectedEvent(final UI.TabSelectedEvent event) {
				numberOfRecordsLabel.setText(String.format(Format.KEY_VALUE, I18N.get("Records"), event.getComponent() instanceof IRecordCollectionView rcv ? rcv.getRecordCount() : "-"));
			}

			@Subscribe
			void onRecordSelectedEvent(final UI.RecordSelectedEvent event) {
				selectedRecordId = (event.getRecord() != null && event.getRecord().hasXRef()) ? event.getRecord().getId() : null;
				idLabel.setText(selectedRecordId == null ? "" : String.format(Format.KEY_VALUE, "ID", selectedRecordId));
			}
		});
	}

	void showProgressBar(final String label) {
		exportProgressBar.setString(label);
		exportProgressBar.setVisible(true);
	}

	void hideProgressBar() {
		exportProgressBar.setVisible(false);
	}
}
