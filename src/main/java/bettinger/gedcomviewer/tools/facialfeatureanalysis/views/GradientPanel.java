package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GradientPanel extends JPanel {

	public GradientPanel(final int gradientWidth, final int gradientHeight, final Color leftColor, final Color rightColor) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final var gradient = new Gradient(leftColor, rightColor);
		gradient.setPreferredSize(new Dimension(gradientWidth, gradientHeight));
		add(gradient);

		final var labels = new JPanel();
		labels.setLayout(new BoxLayout(labels, BoxLayout.X_AXIS));
		labels.add(new JLabel("0%"));
		labels.add(Box.createHorizontalGlue());
		labels.add(new JLabel("100%"));
		add(labels);
	}

	private static class Gradient extends JPanel {
		private final Color leftColor;
		private final Color rightColor;

		public Gradient(final Color leftColor, final Color rightColor) {
			this.leftColor = leftColor;
			this.rightColor = rightColor;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			final var width = getWidth();
			final var height = getHeight();

			if (g instanceof Graphics2D g2d) {
				g2d.setPaint(new GradientPaint(0, 0, leftColor, width, height, rightColor));
			}

			g.fillRect(0, 0, width, height);
		}

	}
}
