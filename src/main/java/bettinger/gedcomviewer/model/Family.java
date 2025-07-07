package bettinger.gedcomviewer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Family extends IndividualFamilyCommonStructure {

	static final String TAG = "FAM";

	private static final String MARRIAGE_TAG = "MARR";
	private static final String DIVORCE_TAG = "DIV";

	private final org.folg.gedcom.model.Family wrappedFamily;

	Family(final GEDCOM gedcom, final org.folg.gedcom.model.Family family) {
		super(gedcom, family, family.getId());

		this.wrappedFamily = family;
	}

	/* #region getter & setter */
	@JsonProperty
	public String getName() {
		final var husband = getHusband();
		final var wife = getWife();
		return String.format(Format.PADDED_AND_SEPARATED, husband == null ? UNKNOWN_STRING : husband.getName(), wife == null ? UNKNOWN_STRING : wife.getName());
	}

	public Individual getHusband() {
		final var husbands = wrappedFamily.getHusbandRefs();
		return husbands.isEmpty() ? null : (Individual) gedcom.getRecord(husbands.get(0).getRef());
	}

	public Individual getWife() {
		final var wifes = wrappedFamily.getWifeRefs();
		return wifes.isEmpty() ? null : (Individual) gedcom.getRecord(wifes.get(0).getRef());
	}

	public Date getMarriageDate() {
		final var fact = getMarriage();
		return fact == null ? null : fact.getDate();
	}

	public String getMarriagePlace() {
		final var fact = getMarriage();
		return fact == null ? "" : fact.getPlace();
	}

	@JsonProperty
	public Location getMarriageLocation() {
		final var fact = getMarriage();
		return fact == null ? null : fact.getLocation();
	}

	private Fact getMarriage() {
		return getBestFact(MARRIAGE_TAG);
	}

	public Quality getMarriageQuality() {
		return getQuality(MARRIAGE_TAG);
	}

	public Date getDivorceDate() {
		final var fact = getDivorce();
		return fact == null ? null : fact.getDate();
	}

	public String getDivorcePlace() {
		final var fact = getDivorce();
		return fact == null ? "" : fact.getPlace();
	}

	public Location getDivorceLocation() {
		final var fact = getDivorce();
		return fact == null ? null : fact.getLocation();
	}

	private Fact getDivorce() {
		return getBestFact(DIVORCE_TAG);
	}

	public Quality getDivorceQuality() {
		return getQuality(DIVORCE_TAG);
	}

	public List<Individual> getChildren() {
		return getChildren(false);
	}

	public List<Individual> getChildren(final boolean excludeConfidential) {
		var result = wrappedFamily.getChildRefs().stream().map(child -> (Individual) gedcom.getRecord(child.getRef())).toList();

		if (excludeConfidential) {
			if (isConfidential(this)) {
				result = new ArrayList<>();
			} else {
				result = result.stream().filter(Predicate.not(Structure::isConfidential)).toList();
			}
		}

		return result;
	}
	/* #endregion */

	/* #region toString & toHTML */
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		HTMLUtils.appendH1(sb, getName());

		if (!(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && isConfidential(this))) {
			final var familyData = formatFamilyData(this, false);
			if (!familyData.isEmpty()) {
				HTMLUtils.appendText(sb, familyData);
			}
		}

		if (!options.contains(HTMLOption.EXPORT)) {
			HTMLUtils.appendLineBreak(sb);
			mediaManager.appendPrimaryPhoto(sb);
		}

		final var husband = getHusband();
		if (!(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && (isConfidential(this) || isConfidential(husband)))) {
			HTMLUtils.appendH2(sb, I18N.get("Husband"));
			HTMLUtils.appendText(sb, formatSpouse(husband));
		}

		final var wife = getWife();
		if (!(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && (isConfidential(this) || isConfidential(wife)))) {
			HTMLUtils.appendH2(sb, I18N.get("Wife"));
			HTMLUtils.appendText(sb, formatSpouse(wife));
		}

		final var children = getChildren(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
		if (!children.isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Children"));
			HTMLUtils.appendText(sb, HTMLUtils.createList(children, Family::formatChild));
		}

		HTMLUtils.appendText(sb, getCommonHTML(options));

		return sb.toString();
	}

	String getCommonHTML(final Set<HTMLOption> options) {
		return super.toHTML(options);
	}

	static String formatFamilyData(final Family family) {
		return formatFamilyData(family, true);
	}

	static String formatFamilyData(final Family family, final boolean singleLine) {
		if (family == null) {
			return "";
		}

		final var sb = new StringBuilder();

		var wasAppended = false;

		final var marriageDate = family.getMarriageDate();
		final var marriagePlace = family.getMarriagePlace();

		if (!(marriageDate == null && marriagePlace.isEmpty())) {
			if (singleLine) {
				HTMLUtils.appendSpace(sb);
			}
			HTMLUtils.appendText(sb, Individual.formatDateAndPlace(MARRIAGE_SIGN, marriageDate, marriagePlace, family.getMarriageLocation()));
			wasAppended = true;
		}

		final var divorceDate = family.getDivorceDate();
		final var divorcePlace = family.getDivorcePlace();

		if (!(divorceDate == null && divorcePlace.isEmpty())) {
			if (wasAppended) {
				if (singleLine) {
					HTMLUtils.appendSpace(sb);
				} else {
					HTMLUtils.appendLineBreak(sb);
				}
			}
			HTMLUtils.appendText(sb, Individual.formatDateAndPlace(DIVORCE_SIGN, divorceDate, divorcePlace, family.getDivorceLocation()));
		}

		return sb.toString();
	}

	static String formatSpouse(Individual spouse) {
		return String.format(Format.SPACED, spouse == null ? UNKNOWN_STRING : spouse.getLink(), Individual.formatLifeData(spouse));
	}

	static String formatChild(Individual child) {
		return Individual.formatWithSexAndLifeData(child);
	}
	/* #endregion */
}
