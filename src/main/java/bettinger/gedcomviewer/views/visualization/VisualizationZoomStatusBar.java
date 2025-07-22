package bettinger.gedcomviewer.views.visualization;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.views.WebViewPanel;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

public class VisualizationZoomStatusBar extends JPanel {

	public VisualizationZoomStatusBar(final WebViewPanel visualization) {
		if (visualization == null) {
			throw new NullPointerException();
		}

		setLayout(new BorderLayout());

		final var zoomBox = new JPanel();
		zoomBox.setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));

		final var zoomOutButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.REMOVE, Constants.MENU_ICON_SIZE));
		zoomOutButton.setToolTipText(I18N.get("ZoomOut"));
		zoomOutButton.putClientProperty("JButton.buttonType", "toolBarButton");
		zoomOutButton.addActionListener(x -> visualization.zoomOut());
		zoomBox.add(zoomOutButton);

		final var zoomLabel = new JLabel();
		zoomLabel.setBorder(BorderFactory.createEmptyBorder(0, Constants.BORDER_SIZE, 0, Constants.BORDER_SIZE));
		zoomLabel.setToolTipText(I18N.get("DoubleClickToReset"));
		zoomLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() == 2) {
					visualization.resetZoom();
				}
			}
		});
		zoomBox.add(zoomLabel);

		final var zoomInButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.ADD, Constants.MENU_ICON_SIZE));
		zoomInButton.putClientProperty("JButton.buttonType", "toolBarButton");
		zoomInButton.setToolTipText(I18N.get("ZoomIn"));
		zoomInButton.addActionListener(x -> visualization.zoomIn());
		zoomBox.add(zoomInButton);

		add(zoomBox, BorderLayout.EAST);

		visualization.addPropertyChangeListener(x -> {
			zoomOutButton.setEnabled(visualization.canZoomOut());
			zoomLabel.setText(String.format("%d %%", visualization.getZoom()));
			zoomInButton.setEnabled(visualization.canZoomIn());
		});
		visualization.setZoom(100);
	}
}
