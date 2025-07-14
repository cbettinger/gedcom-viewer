package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.Analyzer;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

public class ConfigurationDialog extends JDialog {
	public ConfigurationDialog(final Individual individual) {
		setTitle(I18N.get("FacialFeatureAnalysis"));
		setModal(true);

		setLayout(new GridBagLayout());

		final var c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;

		final var generationsSpinner = new JSpinner(new SpinnerNumberModel(Constants.MAX_FACIAL_FEATURE_ANALYSIS_DEPTH, Constants.MIN_FACIAL_FEATURE_ANALYSIS_DEPTH, Constants.MAX_FACIAL_FEATURE_ANALYSIS_DEPTH, 1));
		final var numberOfPortraitsSpinner = new JSpinner(new SpinnerNumberModel(Constants.MAX_FACIAL_FEATURE_ANALYSIS_NUM_PORTRAITS, Constants.MIN_FACIAL_FEATURE_ANALYSIS_NUM_PORTRAITS, Constants.MAX_FACIAL_FEATURE_ANALYSIS_NUM_PORTRAITS, 1));

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(2 * Constants.DIALOG_PADDING, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING);

		final var info = new JTextArea(I18N.get("FacialFeatureAnalysisInfo"));
		info.setBorder(null);
		info.setFocusable(false);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		info.setColumns(40);
		add(info, c);

		c.gridy = 4;
		c.insets = new Insets(0, Constants.DIALOG_PADDING, 2 * Constants.DIALOG_PADDING, Constants.DIALOG_PADDING);

		final var startButton = new JButton(I18N.get("StartFacialFeatureAnalysis"), IconFontSwing.buildIcon(MaterialIcons.PLAY_ARROW, Constants.DEFAULT_ICON_SIZE));
		startButton.addActionListener(_ -> {
			new Analyzer(individual, (int) generationsSpinner.getValue(), (int) numberOfPortraitsSpinner.getValue()).execute();
			dispose();
			setVisible(false);
		});
		add(startButton, c);

		c.gridwidth = 1;
		c.weightx = 0.9;

		c.gridy = 1;
		c.insets = new Insets(0, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING);
		add(new JLabel(I18N.get("Proband")), c);

		c.gridy = 2;
		add(new JLabel(I18N.get("Generations")), c);

		c.gridy = 3;
		add(new JLabel(I18N.get("PortraitsPerPerson")), c);

		c.gridx = 1;
		c.weightx = 0.1;
		c.insets = new Insets(0, 0, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING);

		c.gridy = 1;
		add(new JLabel(individual.getNameAndNumber()), c);

		c.gridy = 2;
		add(generationsSpinner, c);

		c.gridy = 3;
		add(numberOfPortraitsSpinner, c);

		pack();
		setResizable(false);
		setLocationRelativeTo(MainFrame.getInstance());

		getRootPane().setDefaultButton(startButton);
		startButton.requestFocusInWindow();

		setVisible(true);
	}
}
