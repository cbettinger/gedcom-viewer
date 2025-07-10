package bettinger.gedcomviewer.tools.facialfeatureanalysis;

class AnalysisException extends Exception {
	AnalysisException(final String message) {
		super(message);
	}

	AnalysisException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
