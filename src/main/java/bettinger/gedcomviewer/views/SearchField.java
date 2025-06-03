package bettinger.gedcomviewer.views;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.views.tabs.TableContainer;

class SearchField extends JTextField {

	SearchField() {
		super(25);

		setMaximumSize(new Dimension(100, 28));

		putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, I18N.get("Search"));
		putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
		putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Runnable) this::clear);

		addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent event) {
				select(0, getText().length());
			}

			@Override
			public void focusLost(final FocusEvent event) {
				select(0, 0);
			}
		});

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				setEnabled(event.getGEDCOM() != null && event.getGEDCOM().isLoaded());
			}

			@Subscribe
			void onTabSelectedEvent(final UI.TabSelectedEvent event) {
				setEnabled(event.getComponent() instanceof TableContainer);
			}

			@Subscribe
			void onActionEvent(final ActionEvent event) {
				final var actionCommand = event.getActionCommand();
				if ("SEARCH".equals(actionCommand)) {
					requestFocus();
					select(0, getText().length());
				}
			}
		});
	}

	void clear() {
		setText("");
		fireActionPerformed();
	}
}
