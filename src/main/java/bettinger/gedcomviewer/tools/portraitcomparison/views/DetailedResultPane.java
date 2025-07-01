package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.views.WebViewPanel;

public class DetailedResultPane extends JPanel {

  static final Color PERFECT_MATCH_COLOR = Color.GREEN;
  static final Color NO_MATCH_COLOR = Color.DARK_GRAY;
  static final int LEGEND_WIDTH = 400;
  static final int COLOR_RAMP_HEIGHT = 100;

  private final WebViewPanel visualization;

  public DetailedResultPane(final Individual proband, final int numGenerations, final FacialFeatureAnalysisResult result) {
    super();
    setLayout(new BorderLayout());

    this.visualization = new WebViewPanel();

    var legend = new JPanel();
    legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));

    var colorRampPane = new JPanel();
    colorRampPane.setLayout(new BoxLayout(colorRampPane, BoxLayout.X_AXIS));
    colorRampPane.add(new ColorRamp(PERFECT_MATCH_COLOR, NO_MATCH_COLOR));
    var colorRampDescription = new JPanel();
    colorRampDescription.setLayout(new BorderLayout());
    colorRampDescription.add(new JLabel("100%"), BorderLayout.NORTH);
    colorRampDescription.add(new JLabel("0%"), BorderLayout.SOUTH);
    colorRampPane.add(colorRampDescription);
    colorRampPane.setPreferredSize(new Dimension(LEGEND_WIDTH, COLOR_RAMP_HEIGHT));

    var explanations = new JTextArea();
    explanations.setEditable(false);
    explanations.setLineWrap(true);
    explanations.setText(String.format("\n%s\n\n%s: %s\n\n%s: %s", I18N.get("PathSimilarityDetailsExplanation"), I18N.get("AvgSimilarity"), I18N.get("AvgSimilarityDetailsExplanation"), I18N.get("MaxSimilarity"), I18N.get("MaxSimilarityDetailsExplanation")));

    legend.add(colorRampPane);
    legend.add(explanations);

    add(legend, BorderLayout.EAST);
    add(visualization, BorderLayout.CENTER);

    update(proband, numGenerations, result);
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
