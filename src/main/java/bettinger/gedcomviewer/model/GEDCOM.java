package bettinger.gedcomviewer.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.folg.gedcom.model.Gedcom;
import org.folg.gedcom.model.GedcomTag;
import org.folg.gedcom.parser.ModelParser;
import org.folg.gedcom.parser.TreeParser;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.utils.ExportUtils;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

public class GEDCOM {

	private File file;

	private Map<String, GedcomTag> tags;

	private Gedcom wrappedGedcom;

	private Map<String, Record> records;
	private Map<Class<? extends Record>, Integer> numbers;

	private Submitter author;
	private List<Submitter> submitters;

	private List<Note> notes;
	private List<Repository> repositories;
	private List<Media> media;
	private List<Source> sources;
	private List<Location> locations;

	private List<Individual> individuals;
	private List<Family> families;

	private List<Occupation> occupations;

	public GEDCOM() {
		unload();
	}

	public void unload() {
		file = null;

		tags = new HashMap<>();

		wrappedGedcom = null;

		records = new HashMap<>();
		numbers = new HashMap<>();

		author = null;
		submitters = new ArrayList<>();

		notes = new ArrayList<>();
		repositories = new ArrayList<>();
		media = new ArrayList<>();
		sources = new ArrayList<>();
		locations = new ArrayList<>();

		individuals = new ArrayList<>();
		families = new ArrayList<>();

		occupations = new ArrayList<>();

		postEvent();
	}

	public void load(final File file) throws GEDCOMException {
		final var treeParser = new TreeParser();
		final var modelParser = new ModelParser();

		try {
			this.file = file;

			this.tags = treeParser.parseGedcom(file).stream().filter(t -> t.getId() != null).collect(Collectors.toMap(GedcomTag::getId, Function.identity()));

			this.wrappedGedcom = modelParser.parseGedcom(file);

			this.records = new HashMap<>();
			this.numbers = new HashMap<>();

			this.notes = wrappedGedcom.getNotes().stream().map(n -> new Note(this, n)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));
			this.repositories = wrappedGedcom.getRepositories().stream().map(r -> new Repository(this, r)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));
			this.media = wrappedGedcom.getMedia().stream().map(m -> new Media(this, m)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));
			this.sources = wrappedGedcom.getSources().stream().map(s -> new Source(this, s)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));
			this.locations = TagUtils.getExtensionTags(wrappedGedcom, Location.TAG).stream().map(tag -> new Location(this, tag)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));

			this.individuals = wrappedGedcom.getPeople().stream().map(p -> new Individual(this, p)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));
			this.families = wrappedGedcom.getFamilies().stream().map(f -> new Family(this, f)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));

			this.occupations = parseOccupations(this.individuals).stream().map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));

			this.submitters = this.wrappedGedcom.getSubmitters().stream().map(s -> new Submitter(this, s)).map(r -> { addRecord(r); return r; }).collect(Collectors.toCollection(ArrayList::new));
			this.author = this.submitters.stream().filter(s -> s.getId().equals(this.wrappedGedcom.getHeader().getSubmitterRef())).findAny().orElse(null);

			for (final var r : this.records.values()) {
				if (r instanceof SourceCitationContainer sc) {
					sc.setSources();
				}
			}

			for (final var i : this.individuals) {
				i.setAssociations();
			}

			Preferences.storeRecentFile(file);

			postEvent();
		} catch (final Exception e) {
			unload();

			throw new GEDCOMException("Failed to load file", e);
		}
	}

	public void reload() throws GEDCOMException {
		if (isLoaded()) {
			load(file);
		}
	}

	private void postEvent() {
		Events.post(new GEDCOMEvent(this));
	}

	public void saveAsCopy(final File target) throws GEDCOMException {
		if (isLoaded()) {
			try {
				ExportUtils.createGEDCOM5File(this, target);
			} catch (final Exception e) {
				throw new GEDCOMException("Failed to save file", e);
			}
		}
	}

	public void saveAsV7Copy(final File target) throws GEDCOMException {
		if (isLoaded()) {
			try {
				ExportUtils.createGEDCOM7File(this, target);
			} catch (final Exception e) {
				throw new GEDCOMException("Failed to convert file to version 7", e);
			}
		}
	}

	public boolean isLoaded() {
		return file != null;
	}

	public File getFile() {
		return file;
	}

	public String getFileName() {
		return isLoaded() ? file.getName() : "";
	}

	public String getFilePath() {
		return isLoaded() ? FileUtils.getPath(file) : "";
	}

	public String getDirectoryPath() {
		return isLoaded() ? FileUtils.getDirectoryPath(file) : "";
	}

	public GedcomTag getTag(final String id) {
		return tags.get(id);
	}

	public Gedcom getWrappedGedcom() {
		return wrappedGedcom;
	}

	void addRecord(final Record r) {
		records.put(r.getId(), r);
	}

	String getFreeId(final String prefix) {
		var number = 1;
		String id;
		boolean idIsFree;

		do {
			id = String.format(Format.TWO_STRINGS, prefix, number++);
			idIsFree = getRecord(id) == null;
		} while (!idIsFree);

		return id;
	}

	int claimNumber(Class<? extends Record> c) {
		final var result = numbers.containsKey(c) ? numbers.get(c) + 1 : 1;
		numbers.put(c, result);
		return result;
	}

	public Record getRecord(final String id) {
		return records.get(id);
	}

	public Submitter getAuthor() {
		return author;
	}

	public List<Submitter> getSubmitters() {
		return submitters;
	}

	public List<Submitter> getContributors() {
		return author == null ? submitters : submitters.stream().filter(s -> !s.getId().equals(author.getId())).toList();
	}

	public List<Note> getNotes(final String filter) {
		return filter(getNotes(), filter);
	}

	public List<Note> getNotes() {
		return notes;
	}

	public List<Repository> getRepositories(final String filter) {
		return filter(getRepositories(), filter);
	}

	public List<Repository> getRepositories() {
		return repositories;
	}

	public List<Media> getMedia(final String filter) {
		return filter(getMedia(), filter);
	}

	public List<Media> getMedia() {
		return media;
	}

	public List<Source> getSources(final String filter) {
		return filter(getSources(), filter);
	}

	public List<Source> getSources() {
		return sources;
	}

	public List<Location> getLocations(String filter) {
		return filter(getLocations(), filter);
	}

	public List<Location> getLocations() {
		return locations;
	}

	Location getPlace(final String name, final float latitude, final float longitude) {
		return getLocations().stream().filter(l -> !l.isStructure() && l.getName().equals(name) && Float.compare(l.getLatitude(), latitude) == 0 && Float.compare(l.getLongitude(), longitude) == 0).findFirst().orElse(null);
	}

	void addPlace(final Location location) {
		if (!location.isStructure()) {
			locations.add(location);
			addRecord(location);
		}
	}

	public List<Individual> getIndividuals(final String filter) {
		return filter(getIndividuals(), filter);
	}

	public List<Individual> getIndividuals() {
		return individuals;
	}

	public List<Family> getFamilies(final String filter) {
		return filter(getFamilies(), filter);
	}

	public List<Family> getFamilies() {
		return families;
	}

	public List<Occupation> getOccupations(final String filter) {
		return filter(getOccupations(), filter);
	}

	public List<Occupation> getOccupations() {
		return occupations;
	}

	private List<Occupation> parseOccupations(final List<Individual> individuals) {
		final var map = new TreeMap<String, List<Individual>>();
		for (final var individual : individuals) {
			final var occupationValues = individual.getFacts(Occupation.TAG).stream().map(Fact::getValue).filter(s -> !s.isEmpty()).distinct().toList();
			for (final var occupationValue : occupationValues) {
				map.computeIfAbsent(occupationValue, _ -> new ArrayList<>()).add(individual);
			}
		}

		final var result = new ArrayList<Occupation>();

		for (final var entry : map.entrySet()) {
			final var indis = entry.getValue();
			final var occupation = new Occupation(this, entry.getKey(), indis);
			result.add(occupation);

			for (final var i : indis) {
				i.addOccupations(occupation);
			}
		}

		return result;
	}

	private <T extends Record> List<T> filter(final List<T> list, final String filter) {
		var result = list;

		if (!filter.isEmpty()) {
			result = list.stream().filter(s -> HTMLUtils.convertHTMLToString(s.toHTML()).toLowerCase().contains(filter.toLowerCase())).toList();
		}

		return result;
	}

	public static class GEDCOMException extends Exception {
		public GEDCOMException(final String message, final Exception nestedExeption) {
			super(String.format("%s: %s", message, nestedExeption.getMessage()));
		}
	}

	public static class GEDCOMEvent {
		private final GEDCOM gedcom;

		public GEDCOMEvent(final GEDCOM gedcom) {
			this.gedcom = gedcom;
		}

		public GEDCOM getGEDCOM() {
			return gedcom;
		}
	}
}
