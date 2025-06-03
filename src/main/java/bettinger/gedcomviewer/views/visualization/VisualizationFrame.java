package bettinger.gedcomviewer.views.visualization;

import java.awt.BorderLayout;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.utils.SVGUtils;
import bettinger.gedcomviewer.views.Frame;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.ToolBar;
import bettinger.gedcomviewer.views.WebViewPanel;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

@SuppressWarnings("java:S1301")
public class VisualizationFrame extends Frame {

	private static final int MARGIN = 100;

	private static final String EXTENSION_SVG = "svg";
	private static final List<FileNameExtensionFilter> SVG_FILE_FILTER = Arrays.asList(new FileNameExtensionFilter(I18N.get("SVGFile"), EXTENSION_SVG));
	private static final String UNKNOWN_EXTENSION_FORMAT = "Unknown extension '%s'";

	private final Individual proband;
	private final Class<? extends Renderer> rendererClass;
	private String renderedBody;

	private final WebViewPanel visualization;
	private final VisualizationStatusBar statusBar;

	private VisualizationFrame(final String title, final Individual proband, Class<? extends Renderer> rendererClass) {
		setTitle(String.format(Format.KEY_VALUE, title, proband.getName()));
		setLayout(new BorderLayout());

		this.proband = proband;
		this.rendererClass = rendererClass;
		this.renderedBody = "";

		this.visualization = new WebViewPanel(true, null);

		this.statusBar = new VisualizationStatusBar(visualization);
		this.statusBar.addPropertyChangeListener(event -> {
			if (event.getPropertyName().equals(VisualizationStatusBar.PROPERTY_GENERATIONS)) {
				update();
			}
		});
		update();

		final var toolBar = new ToolBar();
		add(toolBar, BorderLayout.NORTH);

		final var saveAsButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.SAVE_AS, Constants.TOOLBAR_ICON_SIZE));
		saveAsButton.setToolTipText(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("SaveAs")));
		saveAsButton.addActionListener(_ -> showSaveAsFileChooser());
		toolBar.add(saveAsButton);

		add(visualization, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);

		pack();
		setSize(Constants.DEFAULT_FRAME_WIDTH - MARGIN, Constants.DEFAULT_FRAME_HEIGHT - MARGIN);
		setLocationRelativeTo(MainFrame.getInstance());
	}

	private void update() {
		Renderer renderer = null;

		try {
			renderer = rendererClass.getDeclaredConstructor().newInstance();
		} catch (final Exception e) {
			Logger.getLogger(VisualizationFrame.class.getName()).log(Level.SEVERE, "Failed to create renderer", e);
		}

		if (renderer != null) {
			renderer.render(proband, statusBar.getGenerations());
			renderedBody = renderer.toString();

			visualization.setBody(renderedBody);
			visualization.scrollTo(renderer.getProbandNode().getPosition());

			statusBar.setNumberOfIndividuals(renderer.getIndividualCount());
		}
	}

	private void showSaveAsFileChooser() {
		if (renderedBody != null && !renderedBody.isEmpty()) {
			final var fileChooser = FileUtils.buildFileChooser(SVG_FILE_FILTER, proband.toString());
			final var result = fileChooser.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				var selectedFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();
				if (selectedFilter == null) {
					selectedFilter = (FileNameExtensionFilter) fileChooser.getChoosableFileFilters()[0];
				}

				final var selectedExtension = selectedFilter.getExtensions()[0];
				final var fileName = FileUtils.ensureFileExtension(fileChooser.getSelectedFile(), selectedExtension);

				new ExportWorker() {
					@Override
					protected URI doInBackground() throws Exception {
						var uri = super.doInBackground();

						try {
							switch (selectedExtension) {
								case EXTENSION_SVG:
									uri = SVGUtils.saveAs(renderedBody, fileName);
									break;
								default:
									throw new UnsupportedOperationException(String.format(UNKNOWN_EXTENSION_FORMAT, selectedExtension));
							}
						} catch (final Exception e) {
							onExportError(e);
						}

						return uri;
					}
				}.execute();
			}
		}
	}

	private class ExportWorker extends SwingWorker<URI, Void> {
		@Override
		protected URI doInBackground() throws Exception {
			SwingUtilities.invokeLater(() -> setEnabled(false));
			return null;
		}

		@Override
		protected void done() {
			onAfterExport();
		}

		void onExportError(final Exception e) {
			onAfterExport();

			JOptionPane.showMessageDialog(null, e.getMessage(), I18N.get("Export"), JOptionPane.OK_OPTION);
		}

		void onAfterExport() {
			SwingUtilities.invokeLater(() -> setEnabled(true));
		}
	}

	public static void renderLineage(final Individual proband) {
		new VisualizationFrame(I18N.get("Lineage"), proband, LineageRenderer.class).setVisible(true);
	}

	public static void renderAncestors(final Individual proband) {
		new VisualizationFrame(I18N.get("AncestorsList"), proband, AncestorsRenderer.class).setVisible(true);
	}

	public static void renderDescendants(final Individual proband) {
		new VisualizationFrame(I18N.get("DescendantsList"), proband, DescendantsRenderer.class).setVisible(true);
	}

	public static void renderConsanguins(final Individual proband) {
		new VisualizationFrame(I18N.get("ConsanguinsList"), proband, ConsanguinsRenderer.class).setVisible(true);
	}
}
