package bettinger.gedcomviewer.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.folg.gedcom.model.ExtensionContainer;
import org.folg.gedcom.model.GedcomTag;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public abstract class Structure {
	public static final String UNKNOWN_STRING = "?";

	public static final String MALE_SIGN = "♂";
	public static final String FEMALE_SIGN = "♀";
	public static final String BIRTH_SIGN = "*";
	public static final String BAPTISM_SIGN = "~";
	public static final String DEATH_SIGN = "✝";
	public static final String BURIAL_SIGN = "\u25AF";
	public static final String MARRIAGE_SIGN = "⚭";
	public static final String DIVORCE_SIGN = "⚮";

	protected final GEDCOM gedcom;

	private final String id;
	private final ExtensionContainer wrappedStructure;

	Structure(final GEDCOM gedcom, final String id, final ExtensionContainer wrappedStructure) {
		this.gedcom = gedcom;
		this.id = id;
		this.wrappedStructure = wrappedStructure;
	}

	/* #region getter & setter */
	public String getLink() {
		return HTMLUtils.createAnchorLink(getId(), HTMLUtils.eliminateLineBreaks(toString()));
	}

	@JsonProperty
	public String getId() {
		return this.id;
	}

	private List<Restriction> getRestrictions() {
		return getExtensionTags("RESN", "_RESN").stream().map(t -> Restriction.fromValue(t.getValue())).distinct().toList();
	}
	/* #endregion */

	List<GedcomTag> getExtensionTags(final String... tagNames) {
		return TagUtils.getExtensionTags(wrappedStructure, Arrays.asList(tagNames));
	}

	String getFirstExtensionTagValue(final String tagName) {
		return TagUtils.getFirstExtensionTagValue(wrappedStructure, tagName);
	}

	GedcomTag getFirstExtensionTag(final String tagName) {
		return TagUtils.getFirstExtensionTag(wrappedStructure, tagName);
	}

	void replaceExtensionTags(final String tagName, final List<GedcomTag> tags) {
		TagUtils.replaceExtensionTags(wrappedStructure, tagName, tags);
	}

	/* #region toString & toHTML */
	public String toHTML() {
		return toHTML(HTMLOption.getDefaults());
	}

	public abstract String toHTML(final Set<HTMLOption> options);
	/* #endregion */

	public static String getSexSign(final String sex) {
		return switch (sex) {
		case "M" -> MALE_SIGN;
		case "F" -> FEMALE_SIGN;
		default -> UNKNOWN_STRING;
		};
	}

	public static boolean isConfidential(final Structure structure) {
		return structure == null || structure.getRestrictions().contains(Restriction.CONFIDENTIAL);
	}

	enum Restriction {

		CONFIDENTIAL, LOCKED, PRIVACY;

		private static final Map<String, Restriction> VALUES = new HashMap<>();

		static {
			for (final var v : values()) {
				VALUES.put(v.toString().toLowerCase(), v);
			}
		}

		public static Restriction fromValue(final String value) {
			return value == null ? null : VALUES.get(value.toLowerCase());
		}
	}
}
