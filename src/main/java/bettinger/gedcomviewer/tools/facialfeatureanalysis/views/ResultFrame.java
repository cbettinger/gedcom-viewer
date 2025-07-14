package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.util.Map;

import javax.swing.JTabbedPane;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.FacialFeature;
import bettinger.gedcomviewer.views.Frame;
import bettinger.gedcomviewer.views.MainFrame;

public class ResultFrame extends Frame {
	public ResultFrame(final Individual proband, final int depth, final Map<FacialFeature, AnalysisResult> results) {
		setTitle(String.format(Format.KEY_VALUE, I18N.get("FacialFeatureAnalysis"), proband.getNameAndNumber()));

		final var tabbedPane = new JTabbedPane();

		tabbedPane.addTab(I18N.get("Overview"), new OverviewPane(proband, depth, results));

		results.entrySet().forEach(entry -> tabbedPane.addTab(I18N.get(entry.getKey().name()), new DetailsPane(proband, depth, entry.getKey(), entry.getValue())));

		add(tabbedPane);

		pack();
		setSize(Constants.DEFAULT_FRAME_WIDTH, Constants.DEFAULT_FRAME_HEIGHT);
		setLocationRelativeTo(MainFrame.getInstance());

		setVisible(true);
	}
}
