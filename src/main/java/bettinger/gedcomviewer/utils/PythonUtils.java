package bettinger.gedcomviewer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface PythonUtils {

	@SuppressWarnings("java:S1192")
	public static List<String> executeScript(final String path, final String[] args) throws IOException {
		final List<String> result = new ArrayList<>();

		final var logger = Logger.getLogger(PythonUtils.class.getName());

		try {
			final var runtime = Runtime.getRuntime();

			final var installPip = runtime.exec(new String[] { "python", "-m", "ensurepip" });
			installPip.waitFor();
			logger.log(Level.INFO, "Installed pip");

			final var installPipenv = runtime.exec(new String[] { "python", "-m", "pip", "install", "--user", "pipenv" });
			installPipenv.waitFor();
			logger.log(Level.INFO, "Installed pipenv");

			final var installRequirements = runtime.exec(new String[] {"python", "-m", "pipenv", "install" });
			installRequirements.waitFor();
			logger.log(Level.INFO, "Installed requirements from Pipfile");

			final ArrayList<String> command = new ArrayList<>();
			command.addAll(Arrays.asList("python", "-m", "pipenv", "run", "python", path));
			command.addAll(Arrays.asList(args));

			final Process script = runtime.exec(command.toArray(new String[0]));
			script.waitFor();
			logger.log(Level.INFO, "Executed script '{}'", path);

			final var output = new BufferedReader(new InputStreamReader(script.getInputStream()));
			output.lines().forEach(result::add);
		} catch (final IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			final var ioe = new IOException(String.format("Unable to execute script '%s'", path), e);
			logger.log(Level.SEVERE, ioe.getMessage(), ioe);
			throw ioe;
		}

		return result;
	}
}
