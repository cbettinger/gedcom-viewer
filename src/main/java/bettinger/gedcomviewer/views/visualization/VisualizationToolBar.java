package bettinger.gedcomviewer.views.visualization;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

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
import bettinger.gedcomviewer.views.ToolBar;
import bettinger.gedcomviewer.views.WebViewPanel;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

public class VisualizationToolBar extends ToolBar {

	private static final String EXTENSION_SVG = "svg";
	private static final List<FileNameExtensionFilter> SVG_FILE_FILTER = Arrays.asList(new FileNameExtensionFilter(I18N.get("SVGFile"), EXTENSION_SVG));
	private static final String UNKNOWN_EXTENSION_FORMAT = "Unknown extension '%s'";

	private WebViewPanel visualization;
	private Individual proband;

	public VisualizationToolBar() {
		this(null, null);
	}

	public VisualizationToolBar(final WebViewPanel visualization, final Individual proband) {
		setVisualization(visualization);
		setProband(proband);

		final var saveAsButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.SAVE_AS, Constants.TOOLBAR_ICON_SIZE));
		saveAsButton.setToolTipText(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("SaveAs")));
		saveAsButton.addActionListener(x -> showSaveAsFileChooser());
		add(saveAsButton);
	}

	public void setVisualization(final WebViewPanel visualization) {
		this.visualization = visualization;
	}

	public void setProband(final Individual proband) {
		this.proband = proband;
	}

	private void showSaveAsFileChooser() {
		if (visualization != null && proband != null) {
			final var renderedBody = visualization.getBody();
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
						@SuppressWarnings("java:S1301")
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
}
