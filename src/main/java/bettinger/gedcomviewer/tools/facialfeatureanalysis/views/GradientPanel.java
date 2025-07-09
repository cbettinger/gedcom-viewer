package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class GradientPanel extends JPanel {

	private final Color color1;
	private final Color color2;

	public GradientPanel(final Color color1, final Color color2) {
		this.color1 = color1;
		this.color2 = color2;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		final var width = getWidth();
		final var height = getHeight();

		((Graphics2D) g).setPaint(new GradientPaint(0, 0, color1, width, height, color2));
		g.fillRect(0, 0, width, height);
	}
}
