package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureSimilarity;
import bettinger.gedcomviewer.views.visualization.AncestorsRenderer;
import bettinger.gedcomviewer.views.visualization.Node;

public class DetailsRenderer extends AncestorsRenderer {

    private static final int LINE_THICKNESS = 3;

    private final Individual targetPerson;
    private final HashMap<String, FacialFeatureSimilarity> personSimilarities;
    private HashMap<Pair<String, String>, Float> coloredEdges;
    private ArrayList<String> includedIndividuals;
    private HashMap<String, Float> lastIndividualsOfPath;

    DetailsRenderer(final Individual proband, final FacialFeatureAnalysisResult result) {
        this.targetPerson = proband;
        this.personSimilarities = result.getPersonSimilarities();
        this.coloredEdges = new HashMap<>();
        this.includedIndividuals = new ArrayList<>();
        this.lastIndividualsOfPath = new HashMap<>();

        for (final var entry : result.getPathSimilarities().entrySet()) {
            var pathIDs = entry.getKey().getAncestorIDs();
            var similarity = entry.getValue();

            var tuple = new Pair<String, String>(proband.getId(), pathIDs[0]);
            if (!this.coloredEdges.containsKey(tuple)) {
                this.coloredEdges.put(tuple, 0.f);
            }
            this.coloredEdges.put(tuple, Math.max(this.coloredEdges.get(tuple), similarity));
            if (personSimilarities.get(pathIDs[0]) != null) {
                this.includedIndividuals.add(pathIDs[0]);
            }

            String lastOfPath = null;
            ArrayList<String> notIncluded = new ArrayList<>();
            for (int i = 0; i < pathIDs.length - 1; i++) {
                if (personSimilarities.get(pathIDs[i + 1]) != null) {
                    this.includedIndividuals.addAll(notIncluded);
                    notIncluded.clear();
                    this.includedIndividuals.add(pathIDs[i + 1]);
                    lastOfPath = pathIDs[i + 1];
                } else {
                    notIncluded.add(pathIDs[i + 1]);
                }
                tuple = new Pair<String, String>(pathIDs[i], pathIDs[i + 1]);
                if (!this.coloredEdges.containsKey(tuple)) {
                    this.coloredEdges.put(tuple, 0.f);
                }
                this.coloredEdges.put(tuple, Math.max(this.coloredEdges.get(tuple), similarity));
            }
            this.lastIndividualsOfPath.put(lastOfPath, similarity);
        }
    }

    @Override
    protected void renderEdges() {
        for (final var edge : edges) {
            final var rootNode = edge.getValue0();
            final var fatherNode = edge.getValue1();
            final var motherNode = edge.getValue2();

            final boolean considerFather = fatherNode != null && fatherNode.getIndividual() != null && includedIndividuals.contains(fatherNode.getIndividual().getId());
            final boolean considerMother = motherNode != null && motherNode.getIndividual() != null && includedIndividuals.contains(motherNode.getIndividual().getId());

            boolean drawLeft = considerFather && coloredEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
            boolean drawRight = considerMother && coloredEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId()));

            g.setPaint(Color.WHITE);
            final Point parentsPoint = renderEdge(fatherNode, motherNode);
            if (parentsPoint != null) {
                if (!drawLeft || !drawRight) {
                    g.setPaint(Color.BLACK);
                    renderEdge(fatherNode, motherNode);
                    g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, rootNode.getPosition().y);
                }
                if (drawLeft) {
                    final Pair<String, String> tuple = new Pair<String, String>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId());
                    renderColoredEdge(rootNode, fatherNode, parentsPoint, tuple, true);
                }
                if (drawRight) {
                    final Pair<String, String> tuple = new Pair<String, String>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId());
                    renderColoredEdge(rootNode, motherNode, parentsPoint, tuple, false);
                }
            }
        }
    }

    private void renderColoredEdge(final Node rootNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> tuple, final boolean left) {
        final Point parentNodePosition = parentNode.getPosition();
        float similarity = coloredEdges.get(tuple);

        final Stroke defaultStroke = g.getStroke();

        int red = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getRed() + similarity * DetailsPane.PERFECT_MATCH_COLOR.getRed()));
        int green = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getGreen() + similarity * DetailsPane.PERFECT_MATCH_COLOR.getGreen()));
        int blue = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getBlue() + similarity * DetailsPane.PERFECT_MATCH_COLOR.getBlue()));
        var color = new Color(red, green, blue, 255);

        if (parentsPoint != null) {
            final int offsetX = left ? -LINE_THICKNESS / 2 : LINE_THICKNESS / 2;
            final int endX = left ? parentNodePosition.x + parentNode.getWidth() : parentNodePosition.x;
            g.setStroke(new BasicStroke(LINE_THICKNESS));
            g.setPaint(color);
            g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, endX, parentsPoint.y);
            g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, parentsPoint.x + offsetX, rootNode.getPosition().y);
            g.setPaint(Color.BLACK);
            g.setStroke(defaultStroke);

            if (lastIndividualsOfPath.containsKey(tuple.getValue1())) {
                final var lineStartX = left ? endX : parentsPoint.x + offsetX;
                final var centerX = lineStartX + Math.abs(parentsPoint.x + offsetX - endX) / 2;

                final var label = String.format("%.2f%%", lastIndividualsOfPath.get(tuple.getValue1()) * 100);
                final var labelWidth = g.getFontMetrics().stringWidth(label);
                var labelX = centerX - labelWidth / 2;
                final var labelY = parentsPoint.y - LINE_THICKNESS;

                g.drawString(label, labelX, labelY);
            }
        }
    }

    @Override
    protected Node createNode(final Individual individual, final boolean isClone, final Node parentNode) {
        var node = new DetailsNode(g, individual, isClone, parentNode);
        if (individual != null && individual != targetPerson) {
            node.init(targetPerson, personSimilarities.get(individual.getId()));
        }
        return node;
    }

    @Override
    protected int getEdgeLabelWidth(Node v, Node w) {
        int result = 0;
        if (v != null && w != null && v.getIndividual() != null && w.getIndividual() != null && (includedIndividuals.contains(v.getIndividual().getId()) || includedIndividuals.contains(w.getIndividual().getId()))) {
            result = g.getFontMetrics().stringWidth("100.00% 100.0%");
        }
        return result;
    }
}
