package bettinger.gedcomviewer.utils;

import java.awt.Desktop;
import java.awt.desktop.AboutHandler;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface DesktopUtils {

	public static void openFileURI(final URI uri) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(new File(uri));
			}
		} catch (final Exception e) {
			Logger.getLogger(DesktopUtils.class.getName()).log(Level.SEVERE, String.format("Failed to open URI '%s'", uri), e);
		}
	}

	public static void openURL(final URL url) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(url.toURI());
			}
		} catch (final Exception e) {
			Logger.getLogger(DesktopUtils.class.getName()).log(Level.SEVERE, String.format("Failed to open URL '%s'", url), e);
		}
	}

	public static void setAboutHandler(final AboutHandler aboutHandler) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().setAboutHandler(aboutHandler);
			}
		} catch (final Exception e) {
			Logger.getLogger(DesktopUtils.class.getName()).log(Level.SEVERE, "Failed to set about handler", e);
		}
	}
}
