package bettinger.gedcomviewer;

import java.awt.EventQueue;

import com.formdev.flatlaf.FlatLightLaf;

import bettinger.gedcomviewer.views.MainFrame;

class Main {

	public static void main(String[] args) {
		System.setProperty("apple.awt.application.name", Constants.APP_NAME);
		System.setProperty("apple.awt.application.appearance", "system");
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		EventQueue.invokeLater(() -> {
			FlatLightLaf.setup();
			MainFrame.create();
		});
	}
}
