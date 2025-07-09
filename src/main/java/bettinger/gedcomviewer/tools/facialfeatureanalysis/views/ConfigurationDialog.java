package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisBackgroundWorker;
import bettinger.gedcomviewer.views.HTMLTextPane;
import bettinger.gedcomviewer.views.IntegerPicker;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

public class ConfigurationDialog extends JDialog {

	public ConfigurationDialog(final Individual individual) {
		setTitle(I18N.get("FacialFeatureAnalysis"));
		setModal(true);

		setLayout(new BorderLayout());

		final var infoPane = new HTMLTextPane();
		infoPane.setText(I18N.get("FacialFeatureAnalysisStartInfo"));

		final var maxDepthPicker = new IntegerPicker(I18N.get("MaxFacialFeatureComparisonDepth"), Constants.MIN_FACIAL_FEATURE_ANALYSIS_DEPTH, Constants.MAX_FACIAL_FEATURE_ANALYSIS_DEPTH, Constants.MAX_FACIAL_FEATURE_ANALYSIS_DEPTH / 2);
		final var maxNumPortraitsPicker = new IntegerPicker(I18N.get("MaxNumPortraitsPerPerson"), Constants.MIN_FACIAL_FEATURE_ANALYSIS_NUM_PORTRAITS, Constants.MAX_FACIAL_FEATURE_ANALYSIS_NUM_PORTRAITS, Constants.MAX_FACIAL_FEATURE_ANALYSIS_NUM_PORTRAITS);

		var vBox = new JPanel();
		vBox.setLayout(new BoxLayout(vBox, BoxLayout.PAGE_AXIS));
		vBox.setBackground(Constants.DEFAULT_CONTENT_COLOR);
		var proband = new HTMLTextPane();
		proband.setText(String.format("%s: %s", I18N.get("Proband"), individual.getName()));
		vBox.add(proband);
		vBox.add(maxDepthPicker);
		vBox.add(maxNumPortraitsPicker);
		var parameterPane = new JPanel();
		parameterPane.setBackground(Constants.DEFAULT_CONTENT_COLOR);
		parameterPane.add(vBox);

		final var startButton = new JButton(I18N.get("StartFacialFeatureAnalysis"), IconFontSwing.buildIcon(MaterialIcons.PLAY_ARROW, Constants.DEFAULT_ICON_SIZE));
		startButton.addActionListener(_ -> {
			new AnalysisBackgroundWorker(individual, maxDepthPicker.getValue(), maxNumPortraitsPicker.getValue()).execute();
			setVisible(false);
		});
		var buttonPanel = new JPanel();
		buttonPanel.add(startButton);

		add(infoPane, BorderLayout.PAGE_START);
		add(parameterPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);

		pack();

		setSize(Constants.DEFAULT_MODAL_DIALOG_WIDTH, Constants.DEFAULT_MODAL_DIALOG_HEIGHT);
		setLocationRelativeTo(MainFrame.getInstance());

		setVisible(true);
	}
}
