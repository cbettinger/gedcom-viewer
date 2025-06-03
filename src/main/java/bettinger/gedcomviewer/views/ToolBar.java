package bettinger.gedcomviewer.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JToolBar;

import com.formdev.flatlaf.util.SystemInfo;

import bettinger.gedcomviewer.Events;

public class ToolBar extends JToolBar implements ActionListener {

	public ToolBar() {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createHorizontalStrut(SystemInfo.isMacOS && SystemInfo.isMacFullWindowContentSupported ? 75 : 8), 0);
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		Events.post(event);
	}
}
