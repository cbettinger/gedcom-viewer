package bettinger.gedcomviewer.views.visualization;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.views.GenerationsComboBox;
import bettinger.gedcomviewer.views.WebViewPanel;

class VisualizationStatusBar extends VisualizationZoomStatusBar {

	static final String PROPERTY_GENERATIONS = "generations";

	private final JLabel numberOfIndividualsLabel;
	private int generations;

	VisualizationStatusBar(final WebViewPanel visualization) {
		super(visualization);

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
	}

	void setNumberOfIndividuals(final int number) {
		numberOfIndividualsLabel.setText(String.format(Format.KEY_VALUE, I18N.get("Individuals"), Math.max(0, number)));
	}

	int getGenerations() {
		return generations;
	}
}
