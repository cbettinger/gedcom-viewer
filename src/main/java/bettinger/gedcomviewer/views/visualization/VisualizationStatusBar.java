package bettinger.gedcomviewer.views.visualization;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.views.GenerationsComboBox;
import bettinger.gedcomviewer.views.WebViewPanel;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

class VisualizationStatusBar extends JPanel {

	static final String PROPERTY_GENERATIONS = "generations";

	private final JLabel numberOfIndividualsLabel;
	private int generations;

	void setNumberOfIndividuals(final int number) {
		numberOfIndividualsLabel.setText(String.format(Format.KEY_VALUE, I18N.get("Individuals"), Math.max(0, number)));
	}

	int getGenerations() {
		return generations;
	}

	VisualizationStatusBar(final WebViewPanel visualization) {
		if (visualization == null) {
			throw new NullPointerException();
		}

		setLayout(new BorderLayout());

		this.numberOfIndividualsLabel = new JLabel();
		numberOfIndividualsLabel.setBorder(BorderFactory.createEmptyBorder(0, 2 * Constants.BORDER_SIZE, 0, 0));
		add(numberOfIndividualsLabel, BorderLayout.WEST);

		this.generations = 0;

		final var generationsBox = new JPanel();
		generationsBox.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

		final var generationsLabel = new JLabel(String.format(Format.TRAILING_COLON, I18N.get("Generations")));
		generationsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Constants.BORDER_SIZE));
		generationsBox.add(generationsLabel);

		final var generationsComboBox = new GenerationsComboBox();
		generationsComboBox.setSelectedIndex(0);
		generationsComboBox.addActionListener(_ -> {
			final var oldValue = generations;
			generations = (int) generationsComboBox.getSelectedItem();
			firePropertyChange(PROPERTY_GENERATIONS, oldValue, generations);
		});
		generationsBox.add(generationsComboBox);

		add(generationsBox, BorderLayout.CENTER);

		final var zoomBox = new JPanel();
		zoomBox.setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));

		final var zoomOutButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.REMOVE, Constants.MENU_ICON_SIZE));
		zoomOutButton.setToolTipText(I18N.get("ZoomOut"));
		zoomOutButton.putClientProperty("JButton.buttonType", "toolBarButton");
		zoomOutButton.addActionListener(_ -> visualization.zoomOut());
		zoomBox.add(zoomOutButton);

		final var zoomLabel = new JLabel();
		zoomLabel.setBorder(BorderFactory.createEmptyBorder(0, Constants.BORDER_SIZE, 0, Constants.BORDER_SIZE));
		zoomLabel.setToolTipText(I18N.get("DoubleClickToReset"));
		zoomLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() == 2) {
					visualization.resetZoom();
				}
			}
		});
		zoomBox.add(zoomLabel);

		final var zoomInButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.ADD, Constants.MENU_ICON_SIZE));
		zoomInButton.putClientProperty("JButton.buttonType", "toolBarButton");
		zoomInButton.setToolTipText(I18N.get("ZoomIn"));
		zoomInButton.addActionListener(_ -> visualization.zoomIn());
		zoomBox.add(zoomInButton);

		add(zoomBox, BorderLayout.EAST);

		visualization.addPropertyChangeListener(_ -> {
			zoomOutButton.setEnabled(visualization.canZoomOut());
			zoomLabel.setText(String.format("%d %%", visualization.getZoom()));
			zoomInButton.setEnabled(visualization.canZoomIn());
		});
		visualization.setZoom(100);
	}
}
