package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    private final HashMap<String, Float> childSimilarities;
    private final ArrayList<String> idsOnBestPaths;
    private final ArrayList<String> idsWithMaxSimOnBestPath;
    private final Float maxPersonSimOnBestPath;

    public DetailsRenderer(final Individual proband, final FacialFeatureAnalysisResult result) {
        this.targetPerson = proband;
        this.personSimilarities = result.getSimilaritiesToProband();
        this.idsOnBestPaths = new ArrayList<>();
        this.idsWithMaxSimOnBestPath = result.getPersonsWithMaxSimOnBestPaths();
        this.maxPersonSimOnBestPath = result.getMaxPersonSimilarityOnBestPath();
        this.childSimilarities = result.getSimilaritiesToChild();
        
        for (final var bestPath : result.getPathsWithMaxSim()) {
            idsOnBestPaths.addAll(Arrays.asList(bestPath.getAncestorIDs()));
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

            boolean drawLeft = considerFather && childSimilarities.containsKey(fatherNode.getIndividual().getId());
            boolean drawRight = considerMother && childSimilarities.containsKey(motherNode.getIndividual().getId());

            g.setPaint(Constants.DEFAULT_CONTENT_COLOR);
            final Point parentsPoint = renderEdge(fatherNode, motherNode);
            if (parentsPoint != null) {
                if (!drawLeft || !drawRight) {
                    g.setPaint(Color.BLACK);
                    renderEdge(fatherNode, motherNode);
                    g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, rootNode.getPosition().y);
                }
                if (drawLeft) {
                    renderColoredEdge(rootNode, fatherNode, parentsPoint, true);
                }
                if (drawRight) {
                    renderColoredEdge(rootNode, motherNode, parentsPoint, false);
                }
            }
        }
    }

    private void renderColoredEdge(final Node rootNode, final Node parentNode, final Point parentsPoint, final boolean left) {
        final String parentId = parentNode.getIndividual().getId();
        final Point parentNodePosition = parentNode.getPosition();
        float similarity = childSimilarities.get(parentId);

        final Stroke defaultStroke = g.getStroke();

        Color perfectMatchColor = idsOnBestPaths.contains(parentId) ? DetailedResultPane.PERFECT_MATCH_COLOR_BEST_PATH : DetailedResultPane.PERFECT_MATCH_COLOR;

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
