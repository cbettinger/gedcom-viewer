package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.net.URI;
import java.util.Map;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.Analyser;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.AnalysisException;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.AnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeature;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.views.ResultFrame;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.MainFrame.BackgroundWorker;

public class AnalysisBackgroundWorker extends BackgroundWorker {

	private final Individual proband;
	private final int depth;
	private final int numberOfPortraits;

	private Map<FacialFeature, AnalysisResult> results;

	public AnalysisBackgroundWorker(final Individual proband, final int depth, final int numberOfPortraits) {
		MainFrame.getInstance().super(I18N.get("FacialFeatureAnalysis"));

		this.proband = proband;
		this.depth = depth;
		this.numberOfPortraits = numberOfPortraits;
	}

	@Override
	protected URI doInBackground() throws Exception {
		var uri = super.doInBackground();

		try {
			results = Analyser.analyse(proband, depth, numberOfPortraits);
		} catch (final AnalysisException e) {
			onError(e);
		}

		return uri;
	}

	@Override
	protected void onSuccess(final URI uri) {
		super.onSuccess(uri);

		if (results != null) {
			new ResultFrame(proband, depth, results);
		}
	}
}
