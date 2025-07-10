package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

public class AnalysisException extends Exception {
	public AnalysisException(final String message) {
		super(message);
	}

	public AnalysisException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
