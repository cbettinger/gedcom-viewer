package bettinger.gedcomviewer.views;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import bettinger.gedcomviewer.utils.Numbering;

public class GenerationsComboBox extends JComboBox<Integer> {

	private static final int MAXIMAL_GENERATIONS = 20;

	public GenerationsComboBox() {
		super();

		setRenderer(new ListCellRenderer<Integer>() {
			private static final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index, boolean isSelected, boolean cellHasFocus) {
				final var label = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (label != null && value != null) {
					label.setText(value == 0 ? "∞" : Numbering.getRoman(value));
				}
				return label;
			}
		});

		for (var i = 0; i <= MAXIMAL_GENERATIONS; i++) {
			addItem(i);
		}
	}
}
