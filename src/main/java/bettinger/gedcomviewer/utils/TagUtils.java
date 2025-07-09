package bettinger.gedcomviewer.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.folg.gedcom.model.ExtensionContainer;
import org.folg.gedcom.model.GedcomTag;

public interface TagUtils {

	static final String EXTENSION_KEY = "folg.more_tags";

	public static String parseChildTagValue(final GedcomTag tag, final String tagValue) {
		final var childTag = getChildTag(tag, tagValue);
		return childTag == null ? null : childTag.getValue();
	}

	public static GedcomTag getChildTag(final GedcomTag tag, final String tagValue) {
		final var childTags = getChildTags(tag, tagValue);
		return childTags.isEmpty() ? null : childTags.get(0);
	}

	public static List<GedcomTag> getChildTags(final GedcomTag tag, final String tagValue) {
		return tag.getChildren().stream().filter(t -> t.getTag().equals(tagValue)).toList();
	}

	public static String getFirstExtensionTagValue(final ExtensionContainer extensionContainer, final String tagName) {
		final var tag = getFirstExtensionTag(extensionContainer, tagName);
		return tag == null ? "" : tag.getValue();
	}

	public static GedcomTag getFirstExtensionTag(final ExtensionContainer extensionContainer, final String tagName) {
		final var tags = getExtensionTags(extensionContainer, tagName);
		return tags.isEmpty() ? null : tags.get(0);
	}

	public static List<GedcomTag> getExtensionTags(final ExtensionContainer extensionContainer, final String tagName) {
		return getExtensionTags(extensionContainer, Arrays.asList(tagName));
	}

	public static List<GedcomTag> getExtensionTags(final ExtensionContainer extensionContainer, final List<String> tagNames) {
		return getExtensionTags(extensionContainer).stream().filter(t -> tagNames.isEmpty() || tagNames.contains(t.getTag())).toList();
	}

	public static void replaceExtensionTags(final ExtensionContainer extensionContainer, final String tagName, final List<GedcomTag> tags) {
		removeExtensionTags(extensionContainer, tagName);
		addExtensionTags(extensionContainer, tags);
	}

	public static void removeExtensionTags(final ExtensionContainer extensionContainer, final String tagName) {
		removeExtensionTags(extensionContainer, Arrays.asList(tagName));
	}

	public static void removeExtensionTags(final ExtensionContainer extensionContainer, final List<String> tagNames) {
		getExtensionTags(extensionContainer).removeIf(t -> tagNames.contains(t.getTag()));
	}

	public static void addExtensionTags(final ExtensionContainer extensionContainer, final List<GedcomTag> newTags) {
		getExtensionTags(extensionContainer).addAll(newTags);
	}

	@SuppressWarnings("unchecked")
	public static List<GedcomTag> getExtensionTags(final ExtensionContainer extensionContainer) {
		var result = extensionContainer == null ? null : ((List<GedcomTag>) extensionContainer.getExtension(EXTENSION_KEY));
		if (result == null) {
			result = new ArrayList<>();
		}
		return result;
	}
}
