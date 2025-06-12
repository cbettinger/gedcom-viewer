package bettinger.gedcomviewer.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.folg.gedcom.model.Name;
import org.javatuples.Quintet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.Numbering;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Individual extends IndividualFamilyCommonStructure {

	static final String TAG = "INDI";

	private static final String BIRTH_TAG = "BIRT";
	private static final String BAPTISM_TAG = "BAPM";
	private static final String DEATH_TAG = "DEAT";
	private static final String BURIAL_TAG = "BURI";

	private static final Comparator<Individual> BIRTH_DATE_COMPARATOR = (i1, i2) -> {
		final var i1BirthDate = i1.getBirthDate() != null ? i1.getBirthDate() : i1.getBaptismDate();
		final var i2BirthDate = i2.getBirthDate() != null ? i2.getBirthDate() : i2.getBaptismDate();

		if (i1BirthDate == null && i2BirthDate == null) {
			return 0;
		} else if (i1BirthDate == null) {
			return 1;
		} else if (i2BirthDate == null) {
			return -1;
		} else {
			return i1BirthDate.compareTo(i2BirthDate);
		}
	};

	private final org.folg.gedcom.model.Person wrappedPerson;

	private List<Association> associations;
	private List<Occupation> occupations;

	Individual(final GEDCOM gedcom, final org.folg.gedcom.model.Person person) {
		super(gedcom, person, person.getId());

		this.wrappedPerson = person;

		this.associations = new ArrayList<>();
		this.occupations = new ArrayList<>();
	}

	/* #region getter & setter */
	@JsonProperty
	public String getName() {
		final var names = wrappedPerson.getNames();
		if (names == null || names.isEmpty()) {
			return UNKNOWN_STRING;
		} else {
			return getFullName(names.get(0));
		}
	}

	public String getAlternativeNames() {
		final var names = wrappedPerson.getNames();
		if (names == null || names.size() < 2) {
			return "";
		} else {
			return String.join(Format.TRAILING_SPACE_COMMA, names.stream().skip(1).map(this::getFullName).toList());
		}
	}

	private String getFullName(final Name name) {
		if (name.getSurname() == null && name.getGiven() == null) {
			return UNKNOWN_STRING;
		}

		return String.format(Format.SPACED, name.getSurname() == null ? UNKNOWN_STRING : name.getSurname().toUpperCase(), name.getGiven() == null ? UNKNOWN_STRING : name.getGiven());
	}

	public String getRawName() {
		final var names = wrappedPerson.getNames();
		if (names == null || names.isEmpty()) {
			return UNKNOWN_STRING;
		} else {
			return names.get(0).getValue();
		}
	}

	public String getSurname() {
		final var names = wrappedPerson.getNames();
		if (names == null || names.isEmpty()) {
			return UNKNOWN_STRING;
		} else {
			final var firstName = names.get(0);
			return firstName.getSurname() == null ? UNKNOWN_STRING : firstName.getSurname();
		}
	}

	public String getGivenName() {
		final var names = wrappedPerson.getNames();
		if (names == null || names.isEmpty()) {
			return UNKNOWN_STRING;
		} else {
			final var firstName = names.get(0);
			return firstName.getGiven() == null ? UNKNOWN_STRING : firstName.getGiven();
		}
	}

	public String getNickname() {
		final var names = wrappedPerson.getNames();
		if (names == null || names.isEmpty()) {
			return "";
		} else {
			final var firstName = names.get(0);
			return firstName.getNickname() == null ? "" : firstName.getNickname();
		}
	}

	public boolean isMale() {
		return Structure.MALE_SIGN.equals(getSex());
	}

	public boolean isFemale() {
		return Structure.FEMALE_SIGN.equals(getSex());
	}

	public String getSex() {
		final var sexFacts = getFacts("SEX");
		final var firstSexFact = sexFacts.isEmpty() ? null : sexFacts.get(0);
		final var sex = firstSexFact == null ? UNKNOWN_STRING : firstSexFact.getValue();
		return getSexSign(sex);
	}

	@JsonProperty
	private String getBirthYear() {
		final var birthDate = getBirthDate() != null ? getBirthDate() : getBaptismDate();
		return birthDate == null ? "" : birthDate.getYear();
	}

	public Date getBirthDate() {
		final var primaryFact = getPrimaryBirth();
		return primaryFact == null ? null : primaryFact.getDate();
	}

	public String getBirthPlace() {
		return getPlace(getPrimaryBirth());
	}

	@JsonProperty
	public Location getBirthLocation() {
		final var primaryFact = getPrimaryBirth();
		return primaryFact == null ? null : primaryFact.getLocation();
	}

	private Fact getPrimaryBirth() {
		return getBestFact(BIRTH_TAG);
	}

	public Quality getBirthQuality() {
		return getQuality(BIRTH_TAG);
	}

	public Date getBaptismDate() {
		final var primaryFact = getPrimaryBaptism();
		return primaryFact == null ? null : primaryFact.getDate();
	}

	public String getBaptismPlace() {
		return getPlace(getPrimaryBaptism());
	}

	@JsonProperty
	public Location getBaptismLocation() {
		final var primaryFact = getPrimaryBaptism();
		return primaryFact == null ? null : primaryFact.getLocation();
	}

	private Fact getPrimaryBaptism() {
		return getBestFact(BAPTISM_TAG);
	}

	public Quality getBaptismQuality() {
		return getQuality(BAPTISM_TAG);
	}

	public Date getDeathDate() {
		final var primaryFact = getPrimaryDeath();
		return primaryFact == null ? null : primaryFact.getDate();
	}

	public String getDeathPlace() {
		return getPlace(getPrimaryDeath());
	}

	public Location getDeathLocation() {
		final var primaryFact = getPrimaryDeath();
		return primaryFact == null ? null : primaryFact.getLocation();
	}

	private Fact getPrimaryDeath() {
		return getBestFact(DEATH_TAG);
	}

	public Quality getDeathQuality() {
		return getQuality(DEATH_TAG);
	}

	public Date getBurialDate() {
		final var primaryFact = getPrimaryBurial();
		return primaryFact == null ? null : primaryFact.getDate();
	}

	public String getBurialPlace() {
		return getPlace(getPrimaryBurial());
	}

	public Location getBurialLocation() {
		final var primaryFact = getPrimaryBurial();
		return primaryFact == null ? null : primaryFact.getLocation();
	}

	private Fact getPrimaryBurial() {
		return getBestFact(BURIAL_TAG);
	}

	public Quality getBurialQuality() {
		return getQuality(BURIAL_TAG);
	}

	private String getPlace(final Fact fact) {
		return fact == null ? "" : fact.getPlace();
	}

	public Individual getFather() {
		final var parents = getParents();
		return parents == null ? null : parents.getHusband();
	}

	public Individual getMother() {
		final var parents = getParents();
		return parents == null ? null : parents.getWife();
	}

	@JsonProperty
	public Family getParents() {
		final var parentFamilies = wrappedPerson.getParentFamilyRefs();
		return parentFamilies == null || parentFamilies.isEmpty() ? null : (Family) gedcom.getRecord(parentFamilies.get(0).getRef());
	}

	public List<Individual> getSiblings() {
		return getSiblings(false);
	}

	public List<Individual> getSiblings(final boolean excludeConfidential) {
		final var parents = getParents();
		var result = parents == null ? new ArrayList<Individual>() : parents.getChildren().stream().filter(child -> !equals(child)).toList();

		if (excludeConfidential) {
			if (isConfidential(this)) {
				result = new ArrayList<>();
			} else {
				result = result.stream().filter(Predicate.not(Structure::isConfidential)).toList();
			}
		}

		return result;
	}

	@JsonProperty
	public List<Family> getFamilies() {
		return getFamilies(false);
	}

	public List<Family> getFamilies(final boolean excludeConfidential) {
		final var spouseFamilies = wrappedPerson.getSpouseFamilyRefs();
		var result = spouseFamilies == null || spouseFamilies.isEmpty() ? new ArrayList<Family>() : spouseFamilies.stream().map(family -> (Family) gedcom.getRecord(family.getRef())).toList();

		if (excludeConfidential) {
			if (isConfidential(this)) {
				result = new ArrayList<>();
			} else {
				result = result.stream().filter(Predicate.not(Structure::isConfidential)).toList();
			}
		}

		return result;
	}

	public Family getFamilyWithSpouse(final Individual spouse) {
		return spouse == null ? null : getFamilies().stream().filter(f -> (isMale() ? f.getWife() : f.getHusband()) == spouse).findFirst().orElse(null);
	}

	public Family getFamilyWithChild(final Individual child) {
		return child == null ? null : getFamilies().stream().filter(f -> f.getChildren().contains(child)).findFirst().orElse(null);
	}

	public Individual getSpouse(final Family family) {
		Individual spouse = null;

		if (family != null) {
			final var husband = family.getHusband();
			final var wife = family.getWife();

			if (husband != null && husband.getId().equals(getId())) {
				spouse = wife;
			} else if (wife != null && wife.getId().equals(getId())) {
				spouse = husband;
			}
		}

		return spouse;
	}

	public boolean isSpouse(final Individual individual) {
		if (individual == null || individual == this) {
			return false;
		}

		final var families = getFamilies();
		for (final var family : families) {
			if (family.getWife() == individual && family.getHusband() == this) {
				return true;
			}

			if (family.getHusband() == individual && family.getWife() == this) {
				return true;
			}
		}

		return false;
	}

	public List<Individual> getChildren() {
		final var result = new ArrayList<Individual>();

		final var families = getFamilies();
		for (final var family : families) {
			final var children = family.getChildren();
			for (final var child : children) {
				result.add(child);
			}
		}

		result.sort(BIRTH_DATE_COMPARATOR);

		return result;
	}

	void setAssociations() {
		associations = wrappedPerson.getAssociations().stream().map(association -> new Association(gedcom, association, this)).toList();
	}

	public List<Association> getAssociations() {
		return getAssociations(false);
	}

	public List<Association> getAssociations(final boolean excludeConfidential) {
		var result = associations;

		if (excludeConfidential) {
			if (isConfidential(this)) {
				result = new ArrayList<>();
			} else {
				result = result.stream().filter(a -> !isConfidential(a.getTarget())).toList();
			}
		}

		return result;
	}

	public List<Quintet<String, Individual, Family, Individual, Integer>> getLineage(final LineageMode mode) {
		return getLineage(mode, 0);
	}

	public List<Quintet<String, Individual, Family, Individual, Integer>> getLineage(final LineageMode mode, final int generations) {
		List<Quintet<String, Individual, Family, Individual, Integer>> result = switch (mode) {
		case LineageMode.NAME_LINE -> getNameLine();
		case LineageMode.MALE_LINE -> getMaleLine();
		default -> new ArrayList<>();
		};

		if (generations > 0) {
			result = result.stream().filter(q -> q.getValue4() <= generations).toList();
		}

		return result;
	}

	private List<Quintet<String, Individual, Family, Individual, Integer>> getMaleLine() {
		final List<Quintet<String, Individual, Family, Individual, Integer>> result = new ArrayList<>();

		var kekule = 1;
		var individual = this;
		result.add(new Quintet<>(Integer.toString(kekule), individual, individual.getFamilies().stream().min(Comparator.comparing(Family::getMarriageQuality)).orElse(null), null, Numbering.getGeneration(kekule)));

		var father = individual.getFather();

		while (father != null) {
			kekule *= 2;
			result.add(new Quintet<>(Integer.toString(kekule), father, individual.getParents(), individual, Numbering.getGeneration(kekule)));

			individual = father;
			father = individual.getFather();
		}

		return result;
	}

	private List<Quintet<String, Individual, Family, Individual, Integer>> getNameLine() {
		final List<Quintet<String, Individual, Family, Individual, Integer>> result = new ArrayList<>();

		var kekule = 1;
		var individual = this;
		result.add(new Quintet<>(Integer.toString(kekule), individual, individual.getFamilies().stream().min(Comparator.comparing(Family::getMarriageQuality)).orElse(null), null, Numbering.getGeneration(kekule)));

		var parent = getNameLineParent(individual);

		while (parent != null) {
			if (parent == individual.getFather()) {
				kekule *= 2;
				result.add(new Quintet<>(Integer.toString(kekule), parent, individual.getParents(), individual, Numbering.getGeneration(kekule)));
			} else if (parent == individual.getMother()) {
				kekule = (2 * kekule) + 1;
				result.add(new Quintet<>(Integer.toString(kekule), parent, individual.getParents(), individual, Numbering.getGeneration(kekule)));
			}
			individual = parent;
			parent = getNameLineParent(individual);
		}

		return result;
	}

	public static Individual getNameLineParent(final Individual individual) {
		final var father = individual.getFather();
		var distanceFather = Integer.MAX_VALUE;
		if (father != null) {
			distanceFather = LevenshteinDistance.getDefaultInstance().apply(individual.getSurname().toLowerCase(), father.getSurname().toLowerCase());
		}

		final var mother = individual.getMother();
		var distanceMother = Integer.MAX_VALUE;
		if (mother != null) {
			distanceMother = LevenshteinDistance.getDefaultInstance().apply(individual.getSurname().toLowerCase(), mother.getSurname().toLowerCase());
		}

		if (father != null && distanceFather < distanceMother) {
			return father;
		}

		if (mother != null && distanceMother < distanceFather) {
			return mother;
		}

		return null;
	}

	public List<Quintet<String, Individual, Family, Individual, Integer>> getAncestorsList() {
		return getAncestorsList(0);
	}

	public List<Quintet<String, Individual, Family, Individual, Integer>> getAncestorsList(final int generations) {
		List<Quintet<String, Individual, Family, Individual, Integer>> result = new ArrayList<>();

		final var kekule = 1;
		var individual = this;
		result.add(new Quintet<>(Integer.toString(kekule), individual, individual.getFamilies().stream().min(Comparator.comparing(Family::getMarriageQuality)).orElse(null), null, Numbering.getGeneration(kekule)));

		final var queue = new ArrayDeque<Individual>();
		queue.add(individual);

		final var visited = new HashMap<Individual, Integer>();
		visited.put(individual, kekule);

		while (!queue.isEmpty()) {
			individual = queue.poll();

			final var father = individual.getFather();
			if (father != null && !visited.containsKey(father)) {
				final var fatherKekule = visited.get(individual) * 2;
				result.add(new Quintet<>(Integer.toString(fatherKekule), father, individual.getParents(), individual, Numbering.getGeneration(fatherKekule)));
				queue.add(father);
				visited.put(father, fatherKekule);
			}

			final var mother = individual.getMother();
			if (mother != null && !visited.containsKey(mother)) {
				final var motherKekule = (visited.get(individual) * 2) + 1;
				result.add(new Quintet<>(Integer.toString(motherKekule), mother, individual.getParents(), individual, Numbering.getGeneration(motherKekule)));
				queue.add(mother);
				visited.put(mother, motherKekule);
			}
		}

		if (generations > 0) {
			result = result.stream().filter(q -> q.getValue4() <= generations).toList();
		}

		return result;
	}

	public List<Quintet<String, Individual, Family, Individual, Integer>> getDescendantsList() {
		return getDescendantsList(0);
	}

	@SuppressWarnings("java:S3824")
	public List<Quintet<String, Individual, Family, Individual, Integer>> getDescendantsList(final int generations) {
		List<Quintet<String, Individual, Family, Individual, Integer>> result = new ArrayList<>();

		final Map<Individual, Integer> generation = new HashMap<>();
		final Map<Individual, String> number = new HashMap<>();

		var individual = this;
		generation.put(individual, 1);
		number.put(individual, "1");
		result.add(new Quintet<>(number.get(individual), individual, individual.getFamilies().stream().min(Comparator.comparing(Family::getMarriageQuality)).orElse(null), null, generation.get(individual)));

		final var queue = new ArrayDeque<Individual>();
		queue.add(individual);

		final var visited = new HashSet<Individual>();
		visited.add(individual);

		while (!queue.isEmpty()) {
			individual = queue.poll();
			final var individualNumber = number.get(individual);

			final var children = individual.getChildren();
			for (final var child : children) {
				if (!visited.contains(child)) {
					generation.put(child, generation.get(individual) + 1);
					number.put(child, String.format(Format.DOT_SEPARATED, individualNumber, children.indexOf(child) + 1));
					result.add(new Quintet<>(number.get(child), child, child.getFamilies().stream().min(Comparator.comparing(Family::getMarriageQuality)).orElse(null), individual, generation.get(child)));
					queue.add(child);
					visited.add(child);
				}
			}
		}

		if (generations > 0) {
			result = result.stream().filter(q -> q.getValue4() <= generations).toList();
		}

		return result;
	}

	void addOccupations(final Occupation occupation) {
		occupations.add(occupation);
	}

	public List<Occupation> getOccupations() {
		return occupations;
	}
	/* #endregion */

	/* #region toString() & to HTML() */
	@Override
	public String toString() {
		return getName();
	}

	@SuppressWarnings("java:S6541")
	@Override
	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var name = getName();
		final var nickname = getNickname();
		HTMLUtils.appendH1(sb, nickname.isEmpty() ? name : String.format(Format.STRING_WITH_QUOTED_SUFFIX, name, nickname));

		if (!(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && isConfidential(this))) {
			var wasAppended = false;

			final var alternativeNames = getAlternativeNames();
			if (!alternativeNames.isEmpty()) {
				HTMLUtils.appendText(sb, alternativeNames);
				wasAppended = true;
			}

			final var lifeData = formatLifeData(this, false);
			if (!lifeData.isEmpty()) {
				HTMLUtils.appendTextAfterLineBreaks(sb, lifeData, wasAppended ? 2 : 0);
			}

			if (!occupations.isEmpty()) {
				final var occupationList = String.join(Format.TRAILING_SPACE_COMMA, occupations.stream().map(Occupation::getLink).distinct().toList());
				if (!occupationList.isEmpty()) {
					HTMLUtils.appendTextAfterLineBreaks(sb, occupationList, 2);
				}
			}
		}

		if (!options.contains(HTMLOption.EXPORT)) {
			HTMLUtils.appendLineBreak(sb);
			mediaManager.appendPortrait(sb);
		}

		final var father = getFather();
		if (!(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && (isConfidential(this) || isConfidential(father)))) {
			HTMLUtils.appendH2(sb, I18N.get("Father"));
			HTMLUtils.appendText(sb, formatWithLifeData(father));
		}

		final var mother = getMother();
		if (!(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && (isConfidential(this) || isConfidential(mother)))) {
			HTMLUtils.appendH2(sb, I18N.get("Mother"));
			HTMLUtils.appendText(sb, formatWithLifeData(mother));
		}

		final var siblings = getSiblings(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
		if (!siblings.isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Siblings"));
			HTMLUtils.appendText(sb, HTMLUtils.createList(siblings, Individual::formatWithLifeData));
		}

		final var families = getFamilies(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
		for (final var family : families) {
			HTMLUtils.appendH2(sb, I18N.get("Spouse"));

			final var husband = family.getHusband();
			final var wife = family.getWife();

			if (husband != null && husband.getId().equals(getId()) && (wife == null || !(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && isConfidential(wife)))) {
				HTMLUtils.appendText(sb, formatSpouse(wife, family));
			} else if (wife != null && wife.getId().equals(getId()) && (husband == null || !(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && isConfidential(husband)))) {
				HTMLUtils.appendText(sb, formatSpouse(husband, family));
			}

			final var children = family.getChildren(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
			if (!children.isEmpty()) {
				HTMLUtils.appendTextAfterLineBreak(sb, HTMLUtils.createList(children, Individual::formatChild));
			}

			final var familyFacts = family.getCommonHTML(HTMLOption.without(options, HTMLOption.COMMONS_HEADINGS));
			if (!familyFacts.isEmpty()) {
				HTMLUtils.appendText(sb, familyFacts);
			}
		}

		final var publicAssociations = getAssociations(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
		if (!publicAssociations.isEmpty()) {
			HTMLUtils.appendH2(sb, I18N.get("Associations"));
			HTMLUtils.appendText(sb, HTMLUtils.createList(publicAssociations, a -> a.toHTML(options)));
		}

		HTMLUtils.appendText(sb, super.toHTML(options));

		return sb.toString();
	}

	static String formatSpouse(final Individual spouse, final Family family) {
		return String.format(Format.SPACED, spouse == null ? UNKNOWN_STRING : spouse.getLink(), formatLifeData(spouse, family));
	}

	static String formatChild(final Individual child) {
		return String.format("↳ %s", formatWithSexAndLifeData(child));
	}

	static String formatWithSexAndLifeData(final Individual individual) {
		return String.format(Format.SPACED, formatWithSex(individual), formatLifeData(individual));
	}

	static String formatWithSex(final Individual individual) {
		return String.format(Format.SPACED, individual.getSex(), individual.getLink());
	}

	static String formatWithLifeData(final Individual individual) {
		return individual == null ? UNKNOWN_STRING : String.format(Format.SPACED, individual.getLink(), formatLifeData(individual));
	}

	static String formatLifeData(final Individual individual) {
		return formatLifeData(individual, null);
	}

	static String formatLifeData(final Individual individual, final Family family) {
		return formatLifeData(individual, family, true);
	}

	static String formatLifeData(final Individual individual, final boolean singleLine) {
		return formatLifeData(individual, null, singleLine);
	}

	static String formatLifeData(final Individual individual, final Family family, final boolean singleLine) {
		if (individual == null) {
			return "";
		}

		final var sb = new StringBuilder();

		var wasAppended = false;

		final var birthDate = individual.getBirthDate();
		final var birthPlace = individual.getBirthPlace();

		final var baptismDate = individual.getBaptismDate();
		final var baptismPlace = individual.getBaptismPlace();

		if (!(birthDate == null && birthPlace.isEmpty())) {
			if (singleLine) {
				HTMLUtils.appendSpace(sb);
			}
			HTMLUtils.appendText(sb, formatDateAndPlace(BIRTH_SIGN, birthDate, birthPlace, individual.getBirthLocation()));
			wasAppended = true;
		} else if (!(baptismDate == null && baptismPlace.isEmpty())) {
			if (singleLine) {
				HTMLUtils.appendSpace(sb);
			}
			HTMLUtils.appendText(sb, formatDateAndPlace(BAPTISM_SIGN, baptismDate, baptismPlace, individual.getBaptismLocation()));
			wasAppended = true;
		}

		if (family != null) {
			HTMLUtils.appendText(sb, Family.formatFamilyData(family));
		}

		final var deathDate = individual.getDeathDate();
		final var deathPlace = individual.getDeathPlace();

		final var burialDate = individual.getBurialDate();
		final var burialPlace = individual.getBurialPlace();

		if (!(deathDate == null && deathPlace.isEmpty())) {
			if (wasAppended) {
				if (singleLine) {
					HTMLUtils.appendSpace(sb);
				} else {
					HTMLUtils.appendLineBreak(sb);
				}
			}
			HTMLUtils.appendText(sb, formatDateAndPlace(DEATH_SIGN, deathDate, deathPlace, individual.getDeathLocation()));
		} else if (!(burialDate == null && burialPlace.isEmpty())) {
			if (wasAppended) {
				if (singleLine) {
					HTMLUtils.appendSpace(sb);
				} else {
					HTMLUtils.appendLineBreak(sb);
				}
			}
			HTMLUtils.appendText(sb, formatDateAndPlace(BURIAL_SIGN, burialDate, burialPlace, individual.getBurialLocation()));
		}

		return sb.toString();
	}

	static String formatDateAndPlace(final String sign, final Date date, final String place, final Location location) {
		final var sb = new StringBuilder();

		final var dateStr = date != null ? date.toString() : "";
		final var placeStr = location != null ? location.getLink() : (place != null ? place : "");

		if (!dateStr.isEmpty() || !placeStr.isEmpty()) {
			HTMLUtils.appendText(sb, String.format(Format.TRAILING_SPACE, sign));
		}

		var wasAppended = false;

		if (!dateStr.isEmpty()) {
			HTMLUtils.appendText(sb, dateStr);
			wasAppended = true;
		}

		if (!placeStr.isEmpty()) {
			if (wasAppended) {
				HTMLUtils.appendSeparator(sb);
			}
			HTMLUtils.appendText(sb, placeStr);
		}

		return sb.toString();
	}
	/* #endregion */
}
