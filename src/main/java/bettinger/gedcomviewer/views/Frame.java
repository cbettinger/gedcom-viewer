package bettinger.gedcomviewer.views;

import javax.swing.JFrame;

import com.formdev.flatlaf.util.SystemInfo;

public class Frame extends JFrame {
	@Override
	public void setVisible(final boolean b) {
		if (SystemInfo.isMacOS && SystemInfo.isMacFullWindowContentSupported) {
			final var rootPane = getRootPane();
			rootPane.putClientProperty("apple.awt.fullWindowContent", true);
			rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
		}

		super.setVisible(b);
	}
}
