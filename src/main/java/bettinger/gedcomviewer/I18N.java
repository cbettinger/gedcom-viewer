package bettinger.gedcomviewer;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

public abstract class I18N {

	private static final Map<Locale, String> SUPPORTED_LOCALES = Map.of(Locale.ENGLISH, "English", Locale.GERMAN, "Deutsch", Locale.FRANCE, "Français");

	private static final String FILENAME = "locales/messages";

	private static Locale currentLocale = getCurrentLocale();
	private static ResourceBundle bundle = getBundle();

	public static Map<Locale, String> getSupportedLocales() {
		return new HashMap<>(SUPPORTED_LOCALES);
	}

	public static Locale getCurrentLocale() {
		return currentLocale == null ? Preferences.getLocale() : currentLocale;
	}

	public static void setCurrentLocale(final Locale locale) {
		if (SUPPORTED_LOCALES.containsKey(locale)) {
			currentLocale = locale;
			bundle = getBundle();

			Preferences.setLocale(locale);
		}
	}

	private static ResourceBundle getBundle() {
		return ResourceBundle.getBundle(FILENAME, getCurrentLocale());
	}

	public static String get(final String key) {
		try {
			return bundle.getString(key);
		} catch (final Exception _) {
			Logger.getLogger(I18N.class.getName()).log(Level.SEVERE, String.format("Ressource string '%s' with locale '%s' not found", key, getCurrentLocale().toString()));
			return key;
		}
	}

	public static ImageIcon getLocaleIcon(final Locale locale, final int size) {
		byte[] data;
		try {
			data = I18N.class.getClassLoader().getResourceAsStream(String.format("locales/%s.png", locale.getLanguage())).readAllBytes();
		} catch (final IOException _) {
			return null;
		}

		return new ImageIcon(new ImageIcon(data).getImage().getScaledInstance(size, size, Image.SCALE_DEFAULT));
	}

	private I18N() {}
}
