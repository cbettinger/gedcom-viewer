package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Structure;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.Similarity;
import bettinger.gedcomviewer.views.visualization.Node;

public class DetailsNode extends Node {

    private static final int BORDER_THICKNESS = 3;

    private Image portraitTargetPerson;
    private int portraitTargetPersonWidth;

    private Color borderColor;

    DetailsNode(SVGGraphics2D g, Individual individual, boolean isClone, Node parentNode) {
        super(g, individual, isClone, parentNode);

        this.portraitTargetPerson = null;
        this.portraitTargetPersonWidth = 0;
        this.borderColor = null;
    }

    void init(Individual target, Similarity similarity) {
        if (similarity != null) {
            portrait = getPortrait(individual, similarity.getMaxSimilarityAncestorPortrait());
            portraitWidth = portrait == null ? 0 : portrait.getWidth(null);

            portraitTargetPerson = getPortrait(target, similarity.getMaxSimilarityProbandsPortrait());
            portraitTargetPersonWidth = portraitTargetPerson == null ? 0 : portraitTargetPerson.getWidth(null);

            text = getTextLines(similarity.getAvgSimilarity(), similarity.getMaxSimilarity());

            g.setFont(BOLD_FONT);
            final var fontMetrics = g.getFontMetrics();
            final var maximalLineWidth = fontMetrics.stringWidth(text.stream().max(Comparator.comparing(fontMetrics::stringWidth)).orElse(""));
            this.width = Math.max(MINIMAL_WIDTH, maximalLineWidth + 3 * PADDING + (this.portrait == null ? 0 : this.portraitWidth + PADDING) + (this.portraitTargetPerson == null ? 0 : this.portraitTargetPersonWidth + PADDING));
            this.height = Math.max(MINIMAL_HEIGHT, text.size() * (lineHeight + PADDING) + 2 * PADDING);

            int red = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getRed() + similarity.getAvgSimilarity() * DetailsPane.PERFECT_MATCH_COLOR.getRed()));
            int green = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getGreen() + similarity.getAvgSimilarity() * DetailsPane.PERFECT_MATCH_COLOR.getGreen()));
            int blue = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getBlue() + similarity.getAvgSimilarity() * DetailsPane.PERFECT_MATCH_COLOR.getBlue()));
            this.borderColor = new Color(red, green, blue, 255);
        }
    }

    private Image getPortrait(Individual individual, String filePath) {
        Image result = null;

        if (individual != null) {
            final var portraits = individual.getFacialPortraits();
            for (var media : portraits) {
                if (media.getFilePath().equals(filePath) && media.exists()) {
                    return individual.getClippedImage(media, -1, PORTRAIT_HEIGHT);
                }
            }
        }

        return result;
    }

    private List<String> getTextLines(Float avgSimilarity, Float maxSimilarity) {
        final List<String> result = new ArrayList<>();

        if (individual == null) {
            result.add(Structure.UNKNOWN_STRING);
        } else {
            final var name = individual.getNickname().isEmpty() ? individual.getName() : String.format(Format.STRING_WITH_QUOTED_SUFFIX, individual.getName(), individual.getNickname());
            result.add(name);

            final var avgSim = String.format("%s: %.2f%%", I18N.get("AvgSimilarity"), avgSimilarity * 100);
            result.add(avgSim);

            final var maxSim = String.format("%s: %.2f%%", I18N.get("MaxSimilarity"), maxSimilarity * 100);
            result.add(maxSim);
        }

        return result;
    }

    @Override
    protected void renderPortraits() {
        super.renderPortraits();
        if (portraitTargetPerson != null) {
            g.drawImage(portraitTargetPerson, x + portraitWidth + 2 * PADDING, y + PADDING, portraitTargetPersonWidth, PORTRAIT_HEIGHT, null);
        }
    }

    @Override
    public void render(int x, int y) {
        super.render(x, y);
        if (borderColor != null) {
            final Stroke defaultStroke = g.getStroke();
            g.setStroke(new BasicStroke(BORDER_THICKNESS));
            g.setPaint(borderColor);
            g.drawRect(this.x, this.y, this.width, this.height);
            g.setPaint(Color.BLACK);
            g.setStroke(defaultStroke);
        }
    }

    @Override
    protected int getTextPositionX() {
        return super.getTextPositionX() + (portraitTargetPerson == null ? 0 : this.portraitTargetPersonWidth + 2 * PADDING);
    }
}
