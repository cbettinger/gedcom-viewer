package bettinger.gedcomviewer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("java:S1192")
public interface PythonUtils {

	static final Runtime runtime = Runtime.getRuntime();

	public static String executeScript(final String path, final String... args) throws IOException {
		final var pipFileDirectory = FileUtils.getDirectoryPath(path);

		setupPipEnv(pipFileDirectory);

		return executeScriptInPipEnv(pipFileDirectory, path, args);
	}

	private static void setupPipEnv(final String pipFileDirectory) throws IOException {
		executeCommand(pipFileDirectory, "ensurepip");
		executeCommand(pipFileDirectory, "pip", "install", "--user", "pipenv");
		executeCommand(pipFileDirectory, "pipenv", "install");
	}

	private static String executeScriptInPipEnv(final String pipFileDirectory, final String path, final String... args) throws IOException {
		final List<String> cmdList = new ArrayList<>();
		cmdList.addAll(Arrays.asList("pipenv", "run", "python"));
		cmdList.add(path);
		cmdList.addAll(Arrays.asList(args));

		return executeCommand(pipFileDirectory, cmdList.toArray(new String[0]));
	}

	private static String executeCommand(final String workingDirectory, final String... cmdArray) throws IOException {
		final List<String> fullCmd = new ArrayList<>();
		fullCmd.addAll(Arrays.asList("python", "-m"));
		fullCmd.addAll(Arrays.asList(cmdArray));

		final var fullCmdArray = fullCmd.toArray(new String[0]);

		final var processBuilder = new ProcessBuilder(fullCmdArray);
		if (workingDirectory != null && !workingDirectory.isEmpty()) {
			processBuilder.directory(FileUtils.getFile(workingDirectory));
		}
		final var process = processBuilder.start();
		try {
			process.waitFor();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException(e.getMessage(), e);
		}

		final List<String> output = new ArrayList<>();
		final var stream = new BufferedReader(new InputStreamReader(process.getInputStream()));
		stream.lines().forEach(output::add);

		final var result = String.join(System.lineSeparator(), output);

		if (process.exitValue() != 0) {
			throw new IOException(String.format("Executed command '%s'%n%n%s", String.join(" ", fullCmdArray), result));
		}

		return result;
	}
}
