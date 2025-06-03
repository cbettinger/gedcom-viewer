package bettinger.gedcomviewer.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public enum HTMLOption {
	COMMONS, COMMONS_HEADINGS, EXPORT, MEDIA_SOURCES, NO_CONFIDENTIAL_DATA, NO_OPEN_MEDIA_LINK, REFERENCES;

	public static Set<HTMLOption> getNone() {
		return EnumSet.noneOf(HTMLOption.class);
	}

	public static Set<HTMLOption> getDefaults() {
		return EnumSet.of(COMMONS, COMMONS_HEADINGS, REFERENCES);
	}

	public static Set<HTMLOption> forExport() {
		final Set<HTMLOption> result = getDefaults();
		result.remove(REFERENCES);
		result.add(EXPORT);
		return result;
	}

	public static Set<HTMLOption> of(final Set<HTMLOption> set1, final Set<HTMLOption> set2) {
		final Set<HTMLOption> result = EnumSet.copyOf(set1);
		result.addAll(set2);
		return result;
	}

	public static Set<HTMLOption> with(final Set<HTMLOption> original, final HTMLOption... optionsToAdd) {
		final var result = EnumSet.copyOf(original);
		result.addAll(Arrays.asList(optionsToAdd));
		return result;
	}

	public static Set<HTMLOption> without(final Set<HTMLOption> original, final HTMLOption... optionsToExclude) {
		final var result = EnumSet.copyOf(original);
		for (final var optionToExclude : optionsToExclude) {
			result.remove(optionToExclude);
		}
		return result;
	}
}
