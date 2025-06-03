package bettinger.gedcomviewer.views.visualization;

import bettinger.gedcomviewer.model.Individual;

public class ConsanguinsRenderer extends Renderer {

	private AncestorsRenderer ancestorsRenderer;
	private DescendantsRenderer descendantsRenderer;

	@Override
	void render(final Individual proband, final int generations) {
		this.proband = proband;

		ancestorsRenderer = new AncestorsRenderer(this.doc, this.g, false);
		ancestorsRenderer.render(proband, generations);

		descendantsRenderer = new DescendantsRenderer(this.doc, this.g, false);
		descendantsRenderer.render(proband, generations, ancestorsRenderer.getProbandNode().getPosition());

		this.probandNode = descendantsRenderer.getProbandNode();
		this.rootNode = descendantsRenderer.getProbandNode();
	}

	@Override
	int getIndividualCount() {
		return (ancestorsRenderer == null ? 0 : ancestorsRenderer.getIndividualCount()) + (descendantsRenderer == null ? 0 : descendantsRenderer.getIndividualCount());
	}
}
