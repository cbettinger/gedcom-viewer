package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.views.WebViewPanel;

public class DetailedResultPane extends JPanel {
  static final Color NO_MATCH_COLOR = Color.DARK_GRAY;
  static final Color PERFECT_MATCH_COLOR_BEST_PATH = Color.GREEN;
  static final int COLOR_RAMP_WIDTH = 200;
  static final int COLOR_RAMP_HEIGHT = 20;

  private final WebViewPanel visualization;

  public DetailedResultPane(final Individual proband, final int numGenerations, final FacialFeatureAnalysisResult result) {
    super();
    setLayout(new BorderLayout());

    this.visualization = new WebViewPanel();

    var legend = new JPanel();
    legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));

    var colorRampBest = getColorRampPane(I18N.get("BestPathColor"), PERFECT_MATCH_COLOR_BEST_PATH);

    var explanations = new JTextArea();
    explanations.setEditable(false);
    explanations.setLineWrap(true);
    explanations.setWrapStyleWord(true);
    explanations.setText(String.format("\n%s\n\n%s: %s", I18N.get("PathSimilarityDetailsExplanation"), I18N.get("AvgSimilarity"), I18N.get("AvgSimilarityDetailsExplanation")));

    legend.add(colorRampBest);
    legend.add(explanations);
    legend.setBorder(new EmptyBorder(5, 5, 5, 5));

    add(legend, BorderLayout.EAST);
    add(visualization, BorderLayout.CENTER);

    update(proband, numGenerations, result);
  }

  private JPanel getColorRampPane(final String title, final Color perfectMatchColor) {
    var colorRampPane = new JPanel();
    colorRampPane.setLayout(new BoxLayout(colorRampPane, BoxLayout.Y_AXIS));
    var titlePane = new JPanel();
    titlePane.setLayout(new BoxLayout(titlePane, BoxLayout.X_AXIS));
    var label = new JLabel(title + ":");
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    titlePane.add(label);
    titlePane.add(Box.createHorizontalGlue());
    var ramp = new ColorRamp(NO_MATCH_COLOR, perfectMatchColor);
    ramp.setMinimumSize(new Dimension(COLOR_RAMP_WIDTH, COLOR_RAMP_HEIGHT));
    colorRampPane.add(titlePane);
    colorRampPane.add(ramp);
    var colorRampDescription = new JPanel();
    colorRampDescription.setLayout(new BoxLayout(colorRampDescription, BoxLayout.X_AXIS));
    colorRampDescription.add(new JLabel("0%"));
    colorRampDescription.add(Box.createHorizontalGlue());
    colorRampDescription.add(new JLabel("100%"));
    colorRampPane.add(colorRampDescription);
    return colorRampPane;
  }

  private void update(final Individual proband, final int numGenerations, FacialFeatureAnalysisResult result) {
    DetailsRenderer renderer = null;

    try {
      renderer = new DetailsRenderer(proband, result);
    } catch (final Exception e) {
      Logger.getLogger(DetailedResultPane.class.getName()).log(Level.SEVERE, "Failed to create renderer", e);
    }

    if (renderer != null) {
      renderer.render(proband, numGenerations + 1);

      visualization.setBody(renderer.toString());
      visualization.scrollTo(renderer.getProbandNode().getPosition());
    }
  }
}
