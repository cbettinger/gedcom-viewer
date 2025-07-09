package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.net.URI;
import java.util.Map;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalyser;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalysisException;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatures;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.views.ResultFrame;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.MainFrame.BackgroundWorker;

public class AnalysisBackgroundWorker extends BackgroundWorker {

	private final Individual proband;
	private final int maxDepth;
	private final int maxNumPortraits;

	private Map<FacialFeatures, FacialFeatureAnalysisResult> results;

	public AnalysisBackgroundWorker(final Individual proband, final int maxDepth, final int maxNumPortraits) {
		MainFrame.getInstance().super(I18N.get("FacialFeatureAnalysis"));

		this.proband = proband;
		this.maxDepth = maxDepth;
		this.maxNumPortraits = maxNumPortraits;
	}

	@Override
	protected URI doInBackground() throws Exception {
		var uri = super.doInBackground();

		try {
			results = FacialFeatureAnalyser.analyse(proband, maxDepth, maxNumPortraits);
		} catch (final FacialFeatureAnalysisException e) {
			onError(e);
		}

		return uri;
	}

	@Override
	protected void onSuccess(final URI uri) {
		super.onSuccess(uri);

		if (results != null) {
			new ResultFrame(proband, maxDepth, results);
		}
	}
}
