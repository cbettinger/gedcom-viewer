package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.javatuples.Pair;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureSimilarity;
import bettinger.gedcomviewer.views.visualization.AncestorsRenderer;
import bettinger.gedcomviewer.views.visualization.Node;

public class DetailsRenderer extends AncestorsRenderer {

    static final int LINE_THICKNESS = 3;

    private final Individual targetPerson;
    private final HashMap<String, FacialFeatureSimilarity> personSimilarities;
    private HashMap<Pair<String, String>, Float> coloredEdges;
    private final ArrayList<String> idsOnBestPaths;
    private final ArrayList<String> idsWithMaxSimOnBestPath;
    private final Float maxPersonSimOnBestPath;

    public DetailsRenderer(final Individual proband, final FacialFeatureAnalysisResult result) {
        this.targetPerson = proband;
        this.personSimilarities = result.getSimilaritiesToProband();
        this.coloredEdges = new HashMap<>();
        this.idsOnBestPaths = new ArrayList<>();
        this.idsWithMaxSimOnBestPath = result.getPersonsWithMaxSimOnBestPaths();
        this.maxPersonSimOnBestPath = result.getMaxPersonSimilarityOnBestPath();

        var edgeSimilarities = result.getSimilaritiesToChild();

        for (final var entry : result.getPathSimilarities().entrySet()) {
            var ancestralLine = entry.getKey();
            var pathIDs = ancestralLine.getAncestorIDs();

            if (result.getPathsWithMaxSim().contains(ancestralLine)) {
                this.idsOnBestPaths.addAll(Arrays.asList(pathIDs));
            }

            var tuple = new Pair<String, String>(proband.getId(), pathIDs[0]);
            if (!this.coloredEdges.containsKey(tuple)) {
                this.coloredEdges.put(tuple, edgeSimilarities.get(pathIDs[0]));
            }

            for (int i = 0; i < pathIDs.length - 1; i++) {
                tuple = new Pair<String, String>(pathIDs[i], pathIDs[i + 1]);
                if (!this.coloredEdges.containsKey(tuple)) {
                    this.coloredEdges.put(tuple, edgeSimilarities.get(pathIDs[i+1]));
                }
            }
        }
    }

    @Override
    public void renderEdges() {
        for (final var edge : edges) {
            final var rootNode = edge.getValue0();
            final var fatherNode = edge.getValue1();
            final var motherNode = edge.getValue2();

            final boolean considerFather = fatherNode != null && fatherNode.getIndividual() != null;
            final boolean considerMother = motherNode != null && motherNode.getIndividual() != null;

            boolean drawLeft = considerFather && coloredEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
            boolean drawRight = considerMother && coloredEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId()));

            g.setPaint(Constants.DEFAULT_CONTENT_COLOR);
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

        Color perfectMatchColor = idsOnBestPaths.contains(tuple.getValue1()) ? DetailedResultPane.PERFECT_MATCH_COLOR_BEST_PATH : DetailedResultPane.PERFECT_MATCH_COLOR;

        var color = getColor(similarity, perfectMatchColor);

        if (parentsPoint != null) {
            final int offsetX = left ? -LINE_THICKNESS / 2 : LINE_THICKNESS / 2;
            final int endX = left ? parentNodePosition.x + parentNode.getWidth() : parentNodePosition.x;
            g.setStroke(new BasicStroke(LINE_THICKNESS));
            g.setPaint(color);
            g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, endX, parentsPoint.y);
            g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, parentsPoint.x + offsetX, rootNode.getPosition().y);
            g.setPaint(Color.BLACK);
            g.setStroke(defaultStroke);

            final var lineStartX = left ? endX : parentsPoint.x + offsetX;
            final var centerX = lineStartX + Math.abs(parentsPoint.x + offsetX - endX) / 2;

            final var label = String.format("%.2f%%", similarity * 100);
            final var labelWidth = g.getFontMetrics().stringWidth(label);
            var labelX = centerX - labelWidth / 2;
            final var labelY = parentsPoint.y - LINE_THICKNESS;

            g.drawString(label, labelX, labelY);
        }
    }

    private Color getColor(float similarity, Color perfectMatchColor) {
        int red = Math.min(255, (int) (DetailedResultPane.NO_MATCH_COLOR.getRed() + similarity * perfectMatchColor.getRed()));
        int green = Math.min(255, (int) (DetailedResultPane.NO_MATCH_COLOR.getGreen() + similarity * perfectMatchColor.getGreen()));
        int blue = Math.min(255, (int) (DetailedResultPane.NO_MATCH_COLOR.getBlue() + similarity * perfectMatchColor.getBlue()));
        var color = new Color(red, green, blue, 255);
        return color;
    }

    @Override
    protected void renderNodes(final Node node) {
        final Stroke defaultStroke = g.getStroke();
        if (renderRootNode || node != rootNode) {
            node.render(node.getPosition().x, node.getPosition().y);
            if (node.getIndividual() != null && idsWithMaxSimOnBestPath.contains(node.getIndividual().getId())) {
                int offset = LINE_THICKNESS;
                g.setPaint(getColor(maxPersonSimOnBestPath, DetailedResultPane.PERFECT_MATCH_COLOR_BEST_PATH));
                g.setStroke(new BasicStroke(LINE_THICKNESS));
                final Rectangle rect = node.getRectangle();
                g.drawRect(rect.x - offset, rect.y - offset, rect.width + 2 * offset, rect.height + 2 * offset);
                g.setPaint(Color.BLACK);
            }
            g.setStroke(defaultStroke);
        }

        for (final var child : node.getChildren()) {
            renderNodes(child);
        }
    }

    @Override
    protected Node getNewNode(Individual individual, boolean isClone, Node parent) {
        var node = new DetailsNode(g, individual, isClone, parent);
        if (individual != null && individual != targetPerson) {
            node.init(targetPerson, personSimilarities.get(individual.getId()));
        }
        return node;
    }

    @Override
    protected int getEdgeLabelWidth(Node v, Node w) {
        int result = 0;
        if (v != null && w != null && v.getIndividual() != null && w.getIndividual() != null) {
            result = g.getFontMetrics().stringWidth("100.00% 100.0%");
        }
        return result;
    }
}
