package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Structure;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureSimilarity;
import bettinger.gedcomviewer.views.visualization.Node;
import bettinger.gedcomviewer.views.visualization.Renderer;

public class DetailsNode extends Node {

    static final int BORDER_THICKNESS = 3;

    private Image portraitTargetPerson;
    private int portraitTargetPersonWidth;

    private Color borderColor;

    public DetailsNode(SVGGraphics2D g, Individual individual, boolean isClone, Node parentNode) {
        super(g, individual, isClone, parentNode);

        this.portraitTargetPerson = null;
        this.portraitTargetPersonWidth = 0;
        this.borderColor = null;
    }

    public void init(Individual target, FacialFeatureSimilarity similarity) {
        if (similarity != null) {
            portrait = getPortrait(individual, similarity.getMaxMatchAncestorFileName());
            portraitWidth = portrait == null ? 0 : portrait.getWidth(null);

            portraitTargetPerson = getPortrait(target, similarity.getMaxMatchTargetFileName());
            portraitTargetPersonWidth = portraitTargetPerson == null ? 0 : portraitTargetPerson.getWidth(null);

            text = getTextLines(similarity.getAvgSimilarity(), similarity.getMaxSimilarity());

            g.setFont(Renderer.BOLD_FONT);
            final var fontMetrics = g.getFontMetrics();
            final var maximalLineWidth = fontMetrics.stringWidth(text.stream().max(Comparator.comparing(fontMetrics::stringWidth)).orElse(""));
            this.width = Math.max(MINIMAL_WIDTH, maximalLineWidth + 3 * PADDING + (this.portrait == null ? 0 : this.portraitWidth + PADDING) + (this.portraitTargetPerson == null ? 0 : this.portraitTargetPersonWidth + PADDING));
            this.height = Math.max(MINIMAL_HEIGHT, text.size() * (lineHeight + PADDING) + 2 * PADDING);

            int red = Math.min(255, (int) (DetailedResultPane.NO_MATCH_COLOR.getRed() + similarity.getAvgSimilarity() * DetailedResultPane.PERFECT_MATCH_COLOR.getRed()));
            int green = Math.min(255, (int) (DetailedResultPane.NO_MATCH_COLOR.getGreen() + similarity.getAvgSimilarity() * DetailedResultPane.PERFECT_MATCH_COLOR.getGreen()));
            int blue = Math.min(255, (int) (DetailedResultPane.NO_MATCH_COLOR.getBlue() + similarity.getAvgSimilarity() * DetailedResultPane.PERFECT_MATCH_COLOR.getBlue()));
            this.borderColor = new Color(red, green, blue, 255);
        }
    }

    private Image getPortrait(Individual individual, String filePath) {
        Image result = null;

        if (individual != null) {
            final var portraits = individual.getPortraits();
            for (var entry : portraits.entrySet()) {
                var media = entry.getKey();
                if (media.getFilePath().equals(filePath)) {
                    var image = (BufferedImage) media.getImage();

                    final var clip = entry.getValue();
                    if (clip != null) {
                        image = image.getSubimage(clip.x, clip.y, clip.width, clip.height);
                    }

                    result = image.getScaledInstance(-1, PORTRAIT_HEIGHT, Image.SCALE_FAST);
                    return result;
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
    protected void renderImages() {
        super.renderImages();
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
