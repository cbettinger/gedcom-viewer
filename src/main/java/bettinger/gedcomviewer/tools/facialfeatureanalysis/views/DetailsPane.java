package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

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
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.views.WebViewPanel;

public class DetailsPane extends JPanel {
  static final Color PERFECT_MATCH_COLOR = Color.GREEN;
  static final Color NO_MATCH_COLOR = Color.DARK_GRAY;
  static final int LEGEND_WIDTH = 400;
  static final int COLOR_RAMP_HEIGHT = 100;

  private final WebViewPanel visualization;

  public DetailsPane(final Individual proband, final int generations, final AnalysisResult result) {
    setLayout(new BorderLayout());

    this.visualization = new WebViewPanel();

    var legend = new JPanel();
    legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));

    var colorGradientPane = new JPanel();
    colorGradientPane.setLayout(new BoxLayout(colorGradientPane, BoxLayout.X_AXIS));
    colorGradientPane.add(new GradientPanel(PERFECT_MATCH_COLOR, NO_MATCH_COLOR));

	var colorGradientDescription = new JPanel();
    colorGradientDescription.setLayout(new BorderLayout());
    colorGradientDescription.add(new JLabel("100%"), BorderLayout.NORTH);
    colorGradientDescription.add(new JLabel("0%"), BorderLayout.SOUTH);
    colorGradientPane.add(colorGradientDescription);
    colorGradientPane.setPreferredSize(new Dimension(LEGEND_WIDTH, COLOR_RAMP_HEIGHT));

    var explanations = new JTextArea();
    explanations.setEditable(false);
    explanations.setLineWrap(true);
    explanations.setText(String.format("\n%s\n\n%s: %s\n\n%s: %s", I18N.get("PathSimilarityDetailsExplanation"), I18N.get("AvgSimilarity"), I18N.get("AvgSimilarityDetailsExplanation"), I18N.get("MaxSimilarity"), I18N.get("MaxSimilarityDetailsExplanation")));

    legend.add(colorGradientPane);
    legend.add(explanations);

    add(legend, BorderLayout.EAST);
    add(visualization, BorderLayout.CENTER);

    update(proband, generations, result);
  }

  private void update(final Individual proband, final int numGenerations, AnalysisResult result) {
    DetailsRenderer renderer = null;

    try {
      renderer = new DetailsRenderer(proband, result);
    } catch (final Exception e) {
      Logger.getLogger(DetailsPane.class.getName()).log(Level.SEVERE, "Failed to create renderer", e);
    }

    if (renderer != null) {
      renderer.render(proband, numGenerations + 1);

      visualization.setBody(renderer.toString());
      visualization.scrollTo(renderer.getProbandNode().getPosition());
    }
  }
}
