package bettinger.gedcomviewer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("java:S1192")
public interface PythonUtils {

	static final Logger logger = Logger.getLogger(PythonUtils.class.getName());
	static final Runtime runtime = Runtime.getRuntime();

	public static List<String> executeScript(final String path, final String... args) throws IOException {
		try {
			final var pipFileDirectory = FileUtils.getDirectoryPath(path);
			setupPipEnv(pipFileDirectory);
			return executeScriptInPipEnv(pipFileDirectory, path, args);
		} catch (final IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			final var ioe = new IOException(String.format("Failed to execute script '%s'", path), e);
			logger.log(Level.SEVERE, ioe.getMessage(), ioe);
			throw ioe;
		}
	}

	private static void setupPipEnv(final String pipFileDirectory) throws IOException, InterruptedException {
		executeCommand(pipFileDirectory, "ensurepip");
		executeCommand(pipFileDirectory, "pip", "install", "--user", "pipenv");
		executeCommand(pipFileDirectory, "pipenv", "install");
	}

	private static List<String> executeScriptInPipEnv(final String pipFileDirectory, final String path, final String... args) throws IOException, InterruptedException {
		final List<String> cmdList = new ArrayList<>();
		cmdList.addAll(Arrays.asList("pipenv", "run", "python"));
		cmdList.add(path);
		cmdList.addAll(Arrays.asList(args));

		return executeCommand(pipFileDirectory, cmdList.toArray(new String[0]));
	}

	@SuppressWarnings("java:S2629")
	private static List<String> executeCommand(final String workingDirectory, final String... cmdArray) throws IOException, InterruptedException {
		final List<String> result = new ArrayList<>();

		final List<String> fullCmd = new ArrayList<>();
		fullCmd.addAll(Arrays.asList("python", "-m"));
		fullCmd.addAll(Arrays.asList(cmdArray));

		final var fullCmdArray = fullCmd.toArray(new String[0]);

		final var processBuilder = new ProcessBuilder(fullCmdArray);
		if (workingDirectory != null && !workingDirectory.isEmpty()) {
			processBuilder.directory(FileUtils.getFile(workingDirectory));
		}
		final var process = processBuilder.start();
		process.waitFor();

		final var output = new BufferedReader(new InputStreamReader(process.getInputStream()));
		output.lines().forEach(result::add);

		logger.log(Level.INFO, String.format("Executed command '%s'%n%s", String.join(" ", fullCmdArray), String.join("\n", result)));

		return result;
	}
}
