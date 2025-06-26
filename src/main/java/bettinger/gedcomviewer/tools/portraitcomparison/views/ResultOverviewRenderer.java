package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Tuple;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;
import bettinger.gedcomviewer.views.visualization.AncestorsRenderer;
import bettinger.gedcomviewer.views.visualization.Node;

class ResultOverviewRenderer extends AncestorsRenderer {

    private HashMap<Color, ArrayList<String>> maxSimilarityIndividuals;
    private HashMap<Pair<String, String>, Set<Color>> maxSimilarityEdges;
    private final int LINE_OFFSET = 5;
    private final Color DEFAULT_LINE_COLOR = Color.BLACK;
    private ArrayList<String> excludedIndividuals;

    ResultOverviewRenderer(final Individual proband, final TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
        super();
        this.maxSimilarityIndividuals = new HashMap<>();
        this.maxSimilarityEdges = new HashMap<>();
        this.excludedIndividuals = new ArrayList<>();

        final var featureColors = ResultOverviewPane.getFeatureColors();
        for (final var entry : results.entrySet()) {
            var color = featureColors.get(entry.getKey());
            var res = entry.getValue();

            var maxPersonSimilarityIDs = res.getMaxPersonSimilarity().getValue0();
            this.maxSimilarityIndividuals.put(color, maxPersonSimilarityIDs);

            var personSimilarities = res.getPersonSimilarities();

            var maxSimilarityPaths = res.getMaxPathSimilarity().getValue0();
            for (final var path : maxSimilarityPaths) {
                var pathIDs = path.getAncestorIDs();
                var tuple = new Pair<String, String>(proband.getId(), pathIDs[0]);
                if (!this.maxSimilarityEdges.containsKey(tuple)) {
                    this.maxSimilarityEdges.put(tuple, new HashSet<Color>());
                }
                this.maxSimilarityEdges.get(tuple).add(color);

                ArrayList<String> exclude = new ArrayList<>();
                for (int i=0; i<pathIDs.length-1; i++) {
                    if(personSimilarities.get(pathIDs[i+1]) == null) {
                        exclude.add(pathIDs[i+1]);
                    } else {
                        exclude.clear();
                    }
                    tuple = new Pair<String,String>(pathIDs[i], pathIDs[i+1]);
                    if (!this.maxSimilarityEdges.containsKey(tuple)) {
                        this.maxSimilarityEdges.put(tuple, new HashSet<Color>());
                    }
                    this.maxSimilarityEdges.get(tuple).add(color);
                }
                this.excludedIndividuals.addAll(exclude);
            }
        } 
    }

    @Override
    public void render(final Individual proband, final int generations) {
		super.render(proband, generations);
        renderBorders();
	}

    @Override
    public void renderEdges() {
        //super.renderEdges();
        for (final var edge : edges) {
            final var rootNode = edge.getValue0();
            final var fatherNode = edge.getValue1();
            final var motherNode = edge.getValue2();

            final Point parentsPoint = renderEdge(fatherNode, motherNode);

            final boolean considerFather = fatherNode != null && fatherNode.getIndividual() != null && !excludedIndividuals.contains(fatherNode.getIndividual().getId());
            final boolean considerMother = motherNode != null && motherNode.getIndividual() != null && !excludedIndividuals.contains(motherNode.getIndividual().getId());

            boolean edgeWasDrawn = false;
            boolean leftAndRightEdgesDrawn = false;
            
            if (considerFather) {
                final Pair<String, String> tuple = new Pair<String,String>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId());
                if (maxSimilarityEdges.containsKey(tuple)) {
                    renderMaxSimilarityEdge(rootNode, fatherNode, parentsPoint, tuple, true);
                    edgeWasDrawn = true;
                }
            }
            if (considerMother) {
                final Pair<String, String> tuple = new Pair<String,String>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId());
                if (maxSimilarityEdges.containsKey(tuple)) {
                    renderMaxSimilarityEdge(rootNode, motherNode, parentsPoint, tuple, false);
                    if (edgeWasDrawn) {
                        leftAndRightEdgesDrawn = true;
                    }
                    edgeWasDrawn = true;
                }
            }
            if (parentsPoint != null) {
                if (!leftAndRightEdgesDrawn) {
                    g.setPaint(DEFAULT_LINE_COLOR);
                    g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, rootNode.getPosition().y);
                } /*else {
                    g.setPaint(Constants.DEFAULT_CONTENT_COLOR);
                    g.drawLine(parentsPoint.x-1, parentsPoint.y, parentsPoint.x+1, parentsPoint.y);
                }*/
            }
		}
    }

    void renderMaxSimilarityEdge(final Node rootNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> tuple, final boolean left) {
        final var edgeColors = maxSimilarityEdges.get(tuple);
        final Point parentNodePosition = parentNode.getPosition();

        int edgeNumber = 0;
        for (final var color : edgeColors) {
            if (parentsPoint != null) {
                final int offsetY = LINE_OFFSET * edgeNumber;
                final int offsetX = left ? -offsetY - LINE_OFFSET / 2 : offsetY + LINE_OFFSET / 2;
                final int endX = left ? parentNodePosition.x + LINE_OFFSET : parentNodePosition.x;
                g.setPaint(color);
                g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, endX, parentsPoint.y + offsetY);
                g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, parentsPoint.x + offsetX, rootNode.getPosition().y);
                edgeNumber++;
            }
        }
        g.setPaint(DEFAULT_LINE_COLOR);
    }

    void renderBorders() {

    }
}

