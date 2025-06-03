package bettinger.gedcomviewer.utils;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import bettinger.gedcomviewer.Format;

public interface FileUtils {
	public static JFileChooser buildFileChooser(final FileNameExtensionFilter filter) {
		return buildFileChooser(Arrays.asList(filter), null);
	}

	public static JFileChooser buildFileChooser(final List<FileNameExtensionFilter> filter) {
		return buildFileChooser(filter, null);
	}

	public static JFileChooser buildFileChooser(final FileNameExtensionFilter filter, final String fileName) {
		return buildFileChooser(Arrays.asList(filter), fileName);
	}

	public static JFileChooser buildFileChooser(final List<FileNameExtensionFilter> filter, final String fileName) {
		final var fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(getFile(System.getProperty("user.home")));
		fileChooser.setAcceptAllFileFilterUsed(false);

		for (final var f : filter) {
			fileChooser.addChoosableFileFilter(f);
		}

		if (!(fileName == null || fileName.isEmpty())) {
			fileChooser.setSelectedFile(getFile(sanitizeFileName(fileName)));
		}
		return fileChooser;
	}

	public static File getFile(final String filePath) {
		return new File(filePath);
	}

	public static String getDirectoryPath(final File file) {
		return getPath(file.getParentFile());
	}

	public static String getPath(final String firstSegment, final String... moreSegments) {
		return getPath(Paths.get(firstSegment, moreSegments).toFile());
	}

	public static String getPath(final File file) {
		return Paths.get(file.getAbsoluteFile().toString()).toString();
	}

	public static String getFileName(final String filePath) {
		return getFileName(getFile(filePath));
	}

	public static String getFileName(final File file) {
		return file.getName();
	}

	public static String getBaseName(final String fileName) {
		return removeFileExtension(fileName, true);
	}

	public static String removeFileExtension(final String fileName, final boolean removeAllExtensions) {
		return fileName == null || fileName.isEmpty() ? fileName : fileName.replaceAll("(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$"), "");
	}

	public static File ensureFileExtension(final File file, final String fileExtension) {
		final var oldPath = file.getAbsolutePath();
		return !oldPath.endsWith(String.format(".%s", fileExtension)) ? getFile(String.format(Format.DOT_SEPARATED, oldPath, fileExtension)) : file;
	}

	public static String sanitizeFileName(final String fileName) {
		return fileName.replaceAll("[\\/:*?\"<>|#%&$!@=+`\']", "").trim().replaceAll(" +", " ");
	}
}
