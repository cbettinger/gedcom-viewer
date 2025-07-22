package bettinger.gedcomviewer.views;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public class AutoCompletion extends PlainDocument {

	private final JComboBox<?> comboBox;
	private ComboBoxModel<?> model;
	private JTextComponent editor;

	private boolean selecting = false;
	private boolean hitBackspace = false;
	private boolean hitBackspaceOnSelection;

	private KeyListener editorKeyListener;
	private FocusListener editorFocusListener;

	private AutoCompletion(final JComboBox<?> comboBox) {
		this.comboBox = comboBox;
		this.model = comboBox.getModel();

		comboBox.addActionListener(x -> {
			if (!selecting) {
				highlightCompletedText(0);
			}
		});

		comboBox.addPropertyChangeListener(e -> {
			if ("editor".equals(e.getPropertyName())) {
				updateEditor((ComboBoxEditor) e.getNewValue());
			}
			if ("model".equals(e.getPropertyName())) {
				model = (ComboBoxModel<?>) e.getNewValue();
			}
		});

		editorKeyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (comboBox.isDisplayable()) {
					comboBox.setPopupVisible(true);
				}

				hitBackspace = false;

				final int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_BACK_SPACE) {
					hitBackspace = true;
					hitBackspaceOnSelection = editor.getSelectionStart() != editor.getSelectionEnd();
				} else if (keyCode == KeyEvent.VK_DELETE) {
					e.consume();
					comboBox.getToolkit().beep();
				}
			}
		};

		editorFocusListener = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				highlightCompletedText(0);
			}

			@Override
			public void focusLost(FocusEvent e) {
				// Workaround for Bug 5100422 - on Java 1.5: Editable JComboBox won't hide popup when tabbing out
				if (System.getProperty("java.version").startsWith("1.5")) {
					comboBox.setPopupVisible(false);
				}
			}
		};

		updateEditor(comboBox.getEditor());

		final Object selected = comboBox.getSelectedItem();
		if (selected != null) {
			setText(selected.toString());
		}

		highlightCompletedText(0);
	}

	private void updateEditor(final ComboBoxEditor newEditor) {
		if (editor != null) {
			editor.removeKeyListener(editorKeyListener);
			editor.removeFocusListener(editorFocusListener);
		}

		if (newEditor != null) {
			editor = (JTextComponent) newEditor.getEditorComponent();
			editor.addKeyListener(editorKeyListener);
			editor.addFocusListener(editorFocusListener);
			editor.setDocument(this);
		}
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		if (selecting) {
			return;
		}

		if (hitBackspace) {
			if (offs > 0) {
				if (hitBackspaceOnSelection) {
					offs--;
				}
			} else {
				UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
			}

			highlightCompletedText(offs);
		} else {
			super.remove(offs, len);
		}
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if (selecting) {
			return;
		}

		super.insertString(offs, str, a);

		Object item = lookupItem(getText(0, getLength()));
		if (item != null) {
			setSelectedItem(item);
		} else {
			item = comboBox.getSelectedItem();
			offs = offs - str.length();
			UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
		}

		setText(item.toString());
		highlightCompletedText(offs + str.length());
	}

	@SuppressWarnings("java:S112")
	private void setText(final String text) {
		try {
			super.remove(0, getLength());
			super.insertString(0, text, null);
		} catch (final BadLocationException e) {
			throw new RuntimeException(e.toString());
		}
	}

	private void highlightCompletedText(final int start) {
		editor.setCaretPosition(getLength());
		editor.moveCaretPosition(start);
	}

	private void setSelectedItem(final Object item) {
		selecting = true;
		model.setSelectedItem(item);
		selecting = false;
	}

	private Object lookupItem(final String pattern) {
		Object result = null;

		final Object selectedItem = model.getSelectedItem();

		if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
			result = selectedItem;
		} else {
			for (int i = 0, n = model.getSize(); i < n; i++) {
				final Object currentItem = model.getElementAt(i);
				if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern)) {
					result = currentItem;
					break;
				}
			}
		}

		return result;
	}

	private boolean startsWithIgnoreCase(final String str1, final String str2) {
		return str1.toLowerCase().startsWith(str2.toLowerCase());
	}

	public static void addTo(final JComboBox<?> comboBox) {
		new AutoCompletion(comboBox);
	}
}
