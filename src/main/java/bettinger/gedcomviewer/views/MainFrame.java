package bettinger.gedcomviewer.views;

import java.awt.BorderLayout;
import java.awt.Taskbar;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.util.SystemInfo;
import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMException;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.views.ConfigurationDialog;
import bettinger.gedcomviewer.tools.validator.ValidationDialog;
import bettinger.gedcomviewer.utils.DesktopUtils;
import bettinger.gedcomviewer.utils.ExportUtils;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.views.dialogs.AboutDialog;
import bettinger.gedcomviewer.views.dialogs.ExportOptionsDialog;
import bettinger.gedcomviewer.views.dialogs.PropertiesDialog;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import bettinger.gedcomviewer.views.visualization.VisualizationFrame;
import jiconfont.swing.IconFontSwing;

public class MainFrame extends Frame {

	private static final String EXTENSION_PDF = "pdf";
	private static final String EXTENSION_HTML = "html";

	private static final String UNKNOWN_EXTENSION_FORMAT = "Unknown extension '%s'";

	private static final FileNameExtensionFilter GEDCOM5_FILE_FILTER = new FileNameExtensionFilter(I18N.get("GEDCOM5Files"), "ged");
	private static final FileNameExtensionFilter GEDCOM7_FILE_FILTER = new FileNameExtensionFilter(I18N.get("GEDCOM7Files"), "ged");
	private static final List<FileNameExtensionFilter> SAVE_COPY_AS_FILE_FILTER = Arrays.asList(GEDCOM5_FILE_FILTER, GEDCOM7_FILE_FILTER);

	private static final List<FileNameExtensionFilter> EXPORT_FILE_FILTER = Arrays.asList(new FileNameExtensionFilter(I18N.get("PDFFile"), EXTENSION_PDF), new FileNameExtensionFilter(I18N.get("HTMLFile"), EXTENSION_HTML));

	private final GEDCOM gedcom = new GEDCOM();

	private final URL iconURL;

	private final TabbedPane tabbedPane;
	private final MainStatusBar statusBar;

	private MainFrame() {
		iconURL = getClass().getClassLoader().getResource("icons/gedcom-viewer-icon.png");

		if (iconURL != null) {
			try {
				final var icon = ImageIO.read(iconURL);
				setIconImage(icon);
				if (SystemInfo.isMacOS) {
					Taskbar.getTaskbar().setIconImage(icon);
				}
			} catch (final IOException e) {
				// intentionally left blank
			}
		}

		IconFontSwing.register(MaterialIcons.getIconFont());

		setJMenuBar(new MainMenuBar());

		setLayout(new BorderLayout());

		final var toolBar = new MainToolBar();
		add(toolBar, BorderLayout.NORTH);

		this.tabbedPane = new TabbedPane();
		add(tabbedPane, BorderLayout.CENTER);

		this.statusBar = new MainStatusBar();
		add(statusBar, BorderLayout.SOUTH);

		pack();
		setSize(Constants.DEFAULT_FRAME_WIDTH, Constants.DEFAULT_FRAME_HEIGHT);
		setLocationRelativeTo(null);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		if (SystemInfo.isMacOS) {
			DesktopUtils.setAboutHandler(x -> showAboutDialog());
		}

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				setTitle(buildTitle(event.getGEDCOM()));
			}

			@Subscribe
			void onActionEvent(ActionEvent event) {
				final var actionCommand = event.getActionCommand();
				switch (actionCommand) {
					case "OPEN_FILE" -> showOpenFileChooser();
					case "RELOAD_FILE" -> reloadFile();
					case "SHOW_PROPERTIES" -> showPropertiesDialog();
					case "SAVE_COPY_AS" -> showSaveAsCopyFileChooser();
					case "CLOSE_FILE" -> closeFile();
					case "QUIT" -> quit();
					case "VISUALIZE_LINEAGE" -> visualizeLineage();
					case "VISUALIZE_ANCESTORS" -> visualizeAncestors();
					case "VISUALIZE_DESCENDANTS" -> visualizeDescendants();
					case "VISUALIZE_CONSANGUINS" -> visualizeConsanguins();
					case "EXPORT_RECORD" -> showExportRecordFileChooser();
					case "EXPORT_LINEAGE" -> showExportLineageFileChooser();
					case "EXPORT_ANCESTORS" -> showExportAncestorsFileChooser();
					case "EXPORT_DESCENDANTS" -> showExportDescendantsFileChooser();
					case "VALIDATION" -> validateFile();
					case "FACIAL_FEATURE_ANALYSIS" -> showFacialFeatureAnalysis();
					case "SHOW_ABOUT" -> showAboutDialog();
				}
			}

			@Subscribe
			void onLoadFileCommand(final UI.LoadFileCommand event) {
				loadFile(event.getFile());
			}

			@Subscribe
			void onLocaleChangedEvent(final Preferences.LocaleChangedEvent event) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), I18N.get("RestartToApplyChanges"), I18N.get("Language"), JOptionPane.INFORMATION_MESSAGE);
			}
		});

		gedcom.unload();
	}

	private String buildTitle(final GEDCOM gedcom) {
		if (gedcom != null) {
			return String.format("%s%s%s", Constants.APP_NAME, gedcom.isLoaded() ? Format.TRAILING_SPACE_COLON : "", gedcom.isLoaded() ? gedcom.getFileName() : "");
		}
		return Constants.APP_NAME;
	}

	private void showOpenFileChooser() {
		final var fileChooser = FileUtils.buildFileChooser(GEDCOM5_FILE_FILTER);
		final var result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			loadFile(fileChooser.getSelectedFile());
		}
	}

	private void loadFile(final File file) {
		if (file != null) {
			try {
				gedcom.load(file);
			} catch (final GEDCOMException e) {
				onLoadError(e);
			}
		}
	}

	private void reloadFile() {
		try {
			gedcom.reload();
		} catch (final GEDCOMException e) {
			onLoadError(e);
		}
	}

	private void onLoadError(final GEDCOMException e) {
		Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.getMessage(), e);

		JOptionPane.showMessageDialog(MainFrame.getInstance(), e.getMessage(), I18N.get("Open"), JOptionPane.ERROR_MESSAGE);
	}

	private void showPropertiesDialog() {
		new PropertiesDialog(gedcom);
	}

	private void showSaveAsCopyFileChooser() {
		final JFileChooser fileChooser = FileUtils.buildFileChooser(SAVE_COPY_AS_FILE_FILTER);
		final var result = fileChooser.showSaveDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			var selectedFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();
			if (selectedFilter == null) {
				selectedFilter = (FileNameExtensionFilter) fileChooser.getChoosableFileFilters()[0];
			}

			final var convertToV7 = selectedFilter == GEDCOM7_FILE_FILTER;

			final var selectedExtension = selectedFilter.getExtensions()[0];
			final var fileName = FileUtils.ensureFileExtension(fileChooser.getSelectedFile(), selectedExtension);

			new BackgroundWorker(I18N.get("Save")) {
				@Override
				protected URI doInBackground() throws Exception {
					final var uri = super.doInBackground();

					try {
						if (convertToV7) {
							gedcom.saveAsV7Copy(fileName);
						} else {
							gedcom.saveAsCopy(fileName);
						}
					} catch (final Exception e) {
						onError(e);
					}

					return uri;
				}
			}.execute();
		}
	}

	private void closeFile() {
		gedcom.unload();
	}

	private void quit() {
		System.exit(0);
	}

	private void visualizeLineage() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual proband) {
			VisualizationFrame.renderLineage(proband);
		}
	}

	private void visualizeAncestors() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual proband) {
			VisualizationFrame.renderAncestors(proband);
		}
	}

	private void visualizeDescendants() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual proband) {
			VisualizationFrame.renderDescendants(proband);
		}
	}

	private void visualizeConsanguins() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual proband) {
			VisualizationFrame.renderConsanguins(proband);
		}
	}

	private void showExportRecordFileChooser() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord != null) {
			final var fileChooser = FileUtils.buildFileChooser(EXPORT_FILE_FILTER, selectedRecord.toString());
			final var result = fileChooser.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				var selectedFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();
				if (selectedFilter == null) {
					selectedFilter = (FileNameExtensionFilter) fileChooser.getChoosableFileFilters()[0];
				}

				final var selectedExtension = selectedFilter.getExtensions()[0];
				final var fileName = FileUtils.ensureFileExtension(fileChooser.getSelectedFile(), selectedExtension);

				final var options = Objects.equals(selectedExtension, EXTENSION_PDF) ? ExportOptionsDialog.getPDFOptions() : ExportOptionsDialog.getHTMLOptions();
				if (options == null) {
					return;
				}

				new BackgroundWorker(I18N.get("Save")) {
					@Override
					protected URI doInBackground() throws Exception {
						var uri = super.doInBackground();

						try {
							switch (selectedExtension) {
								case EXTENSION_PDF:
									uri = ExportUtils.createPDFFile(gedcom, selectedRecord, fileName, options);
									break;
								case EXTENSION_HTML:
									uri = ExportUtils.createHTMLFile(gedcom, selectedRecord, fileName, options);
									break;
								default:
									throw new UnsupportedOperationException(String.format(UNKNOWN_EXTENSION_FORMAT, selectedExtension));
							}
						} catch (final Exception e) {
							onError(e);
						}

						return uri;
					}

					@Override
					protected void onSuccess(final URI uri) {
						super.onSuccess(uri);

						if (uri != null) {
							DesktopUtils.openFileURI(uri);
						}
					}
				}.execute();
			}
		}
	}

	private void showExportLineageFileChooser() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual proband) {
			final var lineage = proband.getLineage(Preferences.getLineageMode());
			final var fileChooser = FileUtils.buildFileChooser(EXPORT_FILE_FILTER, selectedRecord.toString());
			final var result = fileChooser.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				final var title = String.format(Format.KEY_VALUE, I18N.get("Lineage"), selectedRecord.toString());

				var selectedFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();
				if (selectedFilter == null) {
					selectedFilter = (FileNameExtensionFilter) fileChooser.getChoosableFileFilters()[0];
				}

				final var selectedExtension = selectedFilter.getExtensions()[0];
				final var fileName = FileUtils.ensureFileExtension(fileChooser.getSelectedFile(), selectedExtension);

				final var options = Objects.equals(selectedExtension, EXTENSION_PDF) ? ExportOptionsDialog.getPDFOptions() : ExportOptionsDialog.getHTMLOptions();
				if (options == null) {
					return;
				}

				new BackgroundWorker(I18N.get("Save")) {
					@Override
					protected URI doInBackground() throws Exception {
						var uri = super.doInBackground();

						try {
							switch (selectedExtension) {
								case EXTENSION_PDF:
									uri = ExportUtils.createPDFFile(gedcom, lineage, title, fileName, options);
									break;
								case EXTENSION_HTML:
									uri = ExportUtils.createHTMLFile(gedcom, lineage, title, fileName, options);
									break;
								default:
									throw new UnsupportedOperationException(String.format(UNKNOWN_EXTENSION_FORMAT, selectedExtension));
							}
						} catch (final Exception e) {
							onError(e);
						}

						return uri;
					}

					@Override
					protected void onSuccess(final URI uri) {
						super.onSuccess(uri);

						if (uri != null) {
							DesktopUtils.openFileURI(uri);
						}
					}
				}.execute();
			}
		}
	}

	private void showExportAncestorsFileChooser() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual proband) {
			final var ancestorsList = proband.getAncestorsList();
			final var fileChooser = FileUtils.buildFileChooser(EXPORT_FILE_FILTER, selectedRecord.toString());
			final var result = fileChooser.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				final var title = String.format(Format.KEY_VALUE, I18N.get("AncestorsList"), selectedRecord.toString());

				var selectedFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();
				if (selectedFilter == null) {
					selectedFilter = (FileNameExtensionFilter) fileChooser.getChoosableFileFilters()[0];
				}

				final var selectedExtension = selectedFilter.getExtensions()[0];
				final var fileName = FileUtils.ensureFileExtension(fileChooser.getSelectedFile(), selectedExtension);

				final var options = Objects.equals(selectedExtension, EXTENSION_PDF) ? ExportOptionsDialog.getPDFOptions() : ExportOptionsDialog.getHTMLOptions();
				if (options == null) {
					return;
				}

				new BackgroundWorker(I18N.get("Save")) {
					@Override
					protected URI doInBackground() throws Exception {
						var uri = super.doInBackground();

						try {
							switch (selectedExtension) {
								case EXTENSION_PDF:
									uri = ExportUtils.createPDFFile(gedcom, ancestorsList, title, fileName, options);
									break;
								case EXTENSION_HTML:
									uri = ExportUtils.createHTMLFile(gedcom, ancestorsList, title, fileName, options);
									break;
								default:
									throw new UnsupportedOperationException(String.format(UNKNOWN_EXTENSION_FORMAT, selectedExtension));
							}
						} catch (final Exception e) {
							onError(e);
						}

						return uri;
					}

					@Override
					protected void onSuccess(final URI uri) {
						super.onSuccess(uri);

						if (uri != null) {
							DesktopUtils.openFileURI(uri);
						}
					}
				}.execute();
			}
		}
	}

	private void showExportDescendantsFileChooser() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual proband) {
			final var descendantsList = proband.getDescendantsList();
			final var fileChooser = FileUtils.buildFileChooser(EXPORT_FILE_FILTER, selectedRecord.toString());
			final var result = fileChooser.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				final var title = String.format(Format.KEY_VALUE, I18N.get("DescendantsList"), selectedRecord.toString());

				var selectedFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();
				if (selectedFilter == null) {
					selectedFilter = (FileNameExtensionFilter) fileChooser.getChoosableFileFilters()[0];
				}

				final var selectedExtension = selectedFilter.getExtensions()[0];
				final var fileName = FileUtils.ensureFileExtension(fileChooser.getSelectedFile(), selectedExtension);

				final var options = Objects.equals(selectedExtension, EXTENSION_PDF) ? ExportOptionsDialog.getPDFOptions() : ExportOptionsDialog.getHTMLOptions();
				if (options == null) {
					return;
				}

				new BackgroundWorker(I18N.get("Save")) {
					@Override
					protected URI doInBackground() throws Exception {
						var uri = super.doInBackground();

						try {
							switch (selectedExtension) {
								case EXTENSION_PDF:
									uri = ExportUtils.createPDFFile(gedcom, descendantsList, title, fileName, options);
									break;
								case EXTENSION_HTML:
									uri = ExportUtils.createHTMLFile(gedcom, descendantsList, title, fileName, options);
									break;
								default:
									throw new UnsupportedOperationException(String.format(UNKNOWN_EXTENSION_FORMAT, selectedExtension));
							}
						} catch (final Exception e) {
							onError(e);
						}

						return uri;
					}

					@Override
					protected void onSuccess(final URI uri) {
						super.onSuccess(uri);

						if (uri != null) {
							DesktopUtils.openFileURI(uri);
						}
					}
				}.execute();
			}
		}
	}

	private void validateFile() {
		new BackgroundWorker(I18N.get("Validation")) {
			private ValidationDialog dialog;

			@Override
			protected URI doInBackground() throws Exception {
				final var uri = super.doInBackground();

				try {
					dialog = new ValidationDialog(gedcom);
				} catch (final FileNotFoundException e) {
					onError(e);
				}

				return uri;
			}

			@Override
			protected void onSuccess(final URI uri) {
				super.onSuccess(uri);

				if (dialog != null) {
					dialog.open();
				}
			}
		}.execute();
	}

	private void showFacialFeatureAnalysis() {
		final var selectedRecord = tabbedPane.getSelectedRecord();
		if (selectedRecord instanceof Individual individual) {
			new ConfigurationDialog(individual);
		}
	}

	private void showAboutDialog() {
		new AboutDialog();
	}

	private static MainFrame instance = null;

	public static MainFrame getInstance() {
		return instance;
	}

	public static URL getIconURL() {
		return instance != null ? instance.iconURL : null;
	}

	public static void create() {
		if (instance != null) {
			instance.setVisible(false);
			instance.dispose();
			instance = null;
		}

		instance = new MainFrame();
		instance.setVisible(true);
	}

	public class BackgroundWorker extends SwingWorker<URI, Void> {

		protected final String label;

		protected BackgroundWorker(final String label) {
			this.label = label;
		}

		@Override
		protected URI doInBackground() throws Exception {
			before();

			return null;
		}

		@Override
		protected void done() {
			after();

			try {
				onSuccess(get());
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final ExecutionException e) {
				// intentionally left blank
			}
		}

		protected void onSuccess(@SuppressWarnings("java:S1172") final URI uri) {
			// intentionally left blank
		}

		protected void onError(final Exception e) {
			after();

			JOptionPane.showMessageDialog(MainFrame.getInstance(), e.getMessage(), label, JOptionPane.ERROR_MESSAGE);
		}

		private void before() {
			SwingUtilities.invokeLater(() -> {
				statusBar.showProgressBar(String.format(Format.TRAILING_TRIPLE_DOT, label));
				setEnabled(false);
			});
		}

		private void after() {
			SwingUtilities.invokeLater(() -> {
				statusBar.hideProgressBar();
				setEnabled(true);
			});
		}
	}
}
