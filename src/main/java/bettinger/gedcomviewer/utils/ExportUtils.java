package bettinger.gedcomviewer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.folg.gedcom.model.Generator;
import org.folg.gedcom.parser.ModelParser;
import org.folg.gedcom.visitors.GedcomWriter;
import org.javatuples.Quintet;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.xml.sax.SAXParseException;

import com.openhtmltopdf.extend.impl.FSDefaultCacheStore;
import com.openhtmltopdf.outputdevice.helper.ExternalResourceControlPriority;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.CacheStore;

import bettinger.gedcom5to7.Converter;
import bettinger.gedcom5to7.Converter.ConvertException;
import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.HTMLOption;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.IndividualFamilyCommonStructure;
import bettinger.gedcomviewer.model.Location;
import bettinger.gedcomviewer.model.Media;
import bettinger.gedcomviewer.model.MediaContainer;
import bettinger.gedcomviewer.model.Record;
import bettinger.gedcomviewer.model.Repository;
import bettinger.gedcomviewer.model.Source;
import bettinger.gedcomviewer.model.SourceCitation;
import bettinger.gedcomviewer.model.SourceCitationContainer;
import bettinger.gedcomviewer.model.Structure;

public abstract class ExportUtils {
	private static final Generator GENERATOR;

	private static final String HTML_TEMPLATE_FILE_PATH = "./src/main/resources/export/template.html";
	private static final String ASSETS_FOLDER_NAME = "assets";

	private static final String HTML_TITLE_FORMAT = String.format("<h1>%%s<a href='#top' title='%s'>\u25B2</a></h1>", I18N.get("ToTop"));

	private static final Comparator<Record> APPENDIX_COMPARATOR = (o1, o2) -> o1.toString().compareTo(o2.toString());

	@SuppressWarnings("java:S3008")
	private static String HTML_TEMPLATE = "";

	static {
		GENERATOR = new Generator();
		GENERATOR.setValue(String.format("%s %s", Constants.APP_NAME, Constants.APP_VERSION));
		GENERATOR.setName(Constants.APP_NAME);
		GENERATOR.setVersion(Constants.APP_VERSION);

		try {
			HTML_TEMPLATE = Files.readString(Path.of(HTML_TEMPLATE_FILE_PATH));
		} catch (final IOException e) {
			Logger.getLogger(ExportUtils.class.getName()).log(Level.SEVERE, String.format("Failed to read template from file '%s'", HTML_TEMPLATE_FILE_PATH), e);
		}
	}

	public static URI createGEDCOM5File(final GEDCOM gedcom, final File target) throws SAXParseException, IOException {
		final var wrappedGedcom = new ModelParser().parseGedcom(gedcom.getFile());

		final var header = wrappedGedcom.getHeader();
		header.setGenerator(GENERATOR);
		header.setDestination(null);
		header.setFile(FileUtils.getFileName(target));
		header.setDateTime(DateTimeUtils.now());

		final var writer = new GedcomWriter();
		writer.write(wrappedGedcom, target);
		return target.toURI();
	}

	public static URI createGEDCOM7File(final GEDCOM gedcom, final File target) throws ConvertException, IOException {
		try (final OutputStream output = new FileOutputStream(target)) {
			final Converter converter = Converter.parse(gedcom.getFilePath());
			converter.write(output);
			return target.toURI();
		}
	}

	public static URI createPDFFile(final GEDCOM gedcom, final Record r, final File target, final Set<HTMLOption> options) throws IOException {
		try (final OutputStream output = new FileOutputStream(target)) {
			renderPDF(output, renderHTML(gedcom, r, HTMLOption.of(options, HTMLOption.forExport())));
			return target.toURI();
		}
	}

	public static URI createPDFFile(final GEDCOM gedcom, final List<Quintet<String, Individual, Family, Individual, Integer>> records, final String title, final File target, final Set<HTMLOption> options) throws IOException {
		try (final OutputStream output = new FileOutputStream(target)) {
			renderPDF(output, renderHTML(gedcom, records, title, HTMLOption.of(options, HTMLOption.forExport())));
			return target.toURI();
		}
	}

	public static URI createHTMLFile(final GEDCOM gedcom, final Record r, final File target, final Set<HTMLOption> options) throws IOException {
		try (final var writer = new FileWriter(target)) {
			writer.write(renderHTML(gedcom, r, target, HTMLOption.of(options, HTMLOption.forExport())));
			return target.toURI();
		}
	}

	public static URI createHTMLFile(final GEDCOM gedcom, final List<Quintet<String, Individual, Family, Individual, Integer>> records, final String title, final File target, final Set<HTMLOption> options) throws IOException {
		try (final var writer = new FileWriter(target)) {
			writer.write(renderHTML(gedcom, records, title, target, HTMLOption.of(options, HTMLOption.forExport())));
			return target.toURI();
		}
	}

	private static void renderPDF(final OutputStream output, final String html) throws IOException {
		final var sanitizedHtml = HTMLUtils.removeTags(HTMLUtils.removeTags(html, "details"), "summary");

		final PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
		pdfBuilder.useFastMode();
		pdfBuilder.useCacheStore(CacheStore.PDF_FONT_METRICS, new FSDefaultCacheStore());
		pdfBuilder.useFont(new File("./src/main/resources/export/DejaVuSans-Condensed.ttf"), "DejaVuSansCondensed");
		pdfBuilder.useExternalResourceAccessControl((_, _) -> true, ExternalResourceControlPriority.RUN_AFTER_RESOLVING_URI);
		pdfBuilder.useExternalResourceAccessControl((_, _) -> true, ExternalResourceControlPriority.RUN_BEFORE_RESOLVING_URI);
		pdfBuilder.withW3cDocument(new W3CDom().fromJsoup(Jsoup.parse(sanitizedHtml)), null);
		pdfBuilder.toStream(output);
		pdfBuilder.run();
	}

	private static String renderHTML(final GEDCOM gedcom, final Record r, final Set<HTMLOption> options) throws IOException {
		return renderHTML(gedcom, r, null, options);
	}

	private static String renderHTML(final GEDCOM gedcom, final Record r, final File target, final Set<HTMLOption> options) throws IOException {
		return renderTemplate(gedcom, r, r.toString(), target, options);
	}

	private static String renderHTML(final GEDCOM gedcom, final List<Quintet<String, Individual, Family, Individual, Integer>> records, final String title, final Set<HTMLOption> options) throws IOException {
		return renderHTML(gedcom, records, title, null, options);
	}

	private static String renderHTML(final GEDCOM gedcom, final List<Quintet<String, Individual, Family, Individual, Integer>> records, final String title, final File target, final Set<HTMLOption> options) throws IOException {
		return renderTemplate(gedcom, records, title, target, options);
	}

	private static String renderTemplate(final GEDCOM gedcom, final Record r, final String title, final File target, final Set<HTMLOption> options) throws IOException {
		return renderTemplate(gedcom, r == null ? null : Arrays.asList(r), title, "", renderRecord(r, options), target, options);
	}

	private static String renderTemplate(final GEDCOM gedcom, final List<Quintet<String, Individual, Family, Individual, Integer>> records, final String title, final File target, final Set<HTMLOption> options) throws IOException {
		var publicRecords = records.stream().map(Quintet::getValue1).toList();
		if (options.contains(HTMLOption.NO_CONFIDENTIAL_DATA)) {
			publicRecords = publicRecords.stream().filter(Predicate.not(Structure::isConfidential)).toList();
		}

		createGENDEXFile(target, publicRecords);

		final var main = new StringBuilder();
		for (final var r : publicRecords) {
			main.append(renderRecord(r, options));
		}

		return renderTemplate(gedcom, publicRecords, title, createIndividualsIndex(records, options), main.toString(), target, options);
	}

	private static void createGENDEXFile(final File target, final List<Individual> individuals) throws IOException {
		if (target != null && individuals != null) {
			final var fileName = FileUtils.getFileName(target);

			final var sb = new StringBuilder();
			for (final var i : individuals) {
				final var reference = String.format("%s#%s", fileName, i.getId());

				final var birthDateStr = i.getBirthDate() != null ? i.getBirthDate().getRaw() : (i.getBaptismDate() != null ? i.getBaptismDate().getRaw() : "");
				final var deathDateStr = i.getDeathDate() != null ? i.getDeathDate().getRaw() : (i.getBurialDate() != null ? i.getBurialDate().getRaw() : "");

				final var birthLocation = i.getBirthLocation() != null ? i.getBirthLocation() : i.getBaptismLocation();
				final var birthPlace = !i.getBirthPlace().isEmpty() ? i.getBirthPlace() : i.getBaptismPlace();
				final var birthPlaceStr = birthLocation != null ? birthLocation.toString() : birthPlace;

				final var deathLocation = i.getDeathLocation() != null ? i.getDeathLocation() : i.getBurialLocation();
				final var deathPlace = !i.getDeathPlace().isEmpty() ? i.getDeathPlace() : i.getBurialPlace();
				final var deathPlaceStr = deathLocation != null ? deathLocation.toString() : deathPlace;

				sb.append(String.format("%s|%s|%s|%s|%s|%s|%s|%n", reference, i.getSurname(), i.getRawName(), birthDateStr, birthPlaceStr, deathDateStr, deathPlaceStr));
			}

			try (final var writer = new FileWriter(new File(FileUtils.getDirectoryPath(target), "gendex.txt"))) {
				writer.write(sb.toString());
			}
		}
	}

	private static String renderTemplate(final GEDCOM gedcom, final List<? extends Record> records, final String title, final String recordIndex, final String main, final File target, final Set<HTMLOption> options) throws IOException {
		if (records == null || records.isEmpty()) {
			return "";
		}

		final var appendices = getAppendices(records, options);
		return HTMLUtils.printPretty(copyAssets(target, String.format(HTML_TEMPLATE, I18N.getCurrentLocale().getLanguage(), title, title, createAppendixIndex(appendices), recordIndex, main, createLocationsSection(appendices, options), createMediaSection(appendices, options), createSourcesSection(appendices, options), createRepositoriesSection(appendices, options), createFooter(gedcom, options))));
	}

	private static String renderRecord(final Record r, final Set<HTMLOption> options) {
		return r == null ? "" : String.format("<article id='%s' class='%s'>%s</article>", r.getId(), r.getClass().getSimpleName().toLowerCase(), HTMLUtils.addLinks(r.toHTML(options)));
	}

	private static String copyAssets(final File target, final String html) throws IOException {
		if (target == null) {
			return html;
		} else {
			final var fileDirectory = FileUtils.getDirectoryPath(target);

			var result = html;

			final var assets = HTMLUtils.getDownloadLinks(html).stream().map(File::new).toList();
			if (!assets.isEmpty()) {
				final var assetDirectory = new File(fileDirectory, ASSETS_FOLDER_NAME);
				if (!assetDirectory.exists()) {
					assetDirectory.mkdirs();
				}

				for (final var asset : assets) {
					final var assetFileName = FileUtils.getFileName(asset);
					Files.copy(asset.toPath(), Paths.get(FileUtils.getPath(assetDirectory), assetFileName), StandardCopyOption.REPLACE_EXISTING);

					result = result.replaceAll(asset.toURI().toString(), String.format(Format.SLASH_SEPARATED, ASSETS_FOLDER_NAME, assetFileName));
				}
			}

			return result;
		}
	}

	private static String createIndividualsIndex(final List<Quintet<String, Individual, Family, Individual, Integer>> records, final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		if (records != null && records.size() > 1) {
			sb.append("<table>");
			for (final var r : records) {
				final var individual = r.getValue1();
				sb.append(String.format("<tr><td>%s</td><td>%s</td></tr>", r.getValue0(), options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && Structure.isConfidential(individual) ? Structure.UNKNOWN_STRING : individual.getLink()));
			}
			sb.append("</table>");
		}

		return sb.toString();
	}

	private static Set<Record> getAppendices(final List<? extends Record> records, final Set<HTMLOption> options) {
		final var result = new ArrayList<Record>();

		final var visited = new HashSet<Record>();

		final var queue = new ArrayDeque<Record>();
		queue.addAll(records);

		while (!queue.isEmpty()) {
			final var appendix = queue.poll();
			if (appendix != null && !visited.contains(appendix)) {
				visited.add(appendix);

				if (appendix instanceof IndividualFamilyCommonStructure ifcs) {
					final var appendixLocation = ifcs.getLocations(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
					result.addAll(appendixLocation);
					queue.addAll(appendixLocation);
				}

				if (appendix instanceof MediaContainer mc) {
					final var appendixMedia = mc.getMedia(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
					result.addAll(appendixMedia);
					queue.addAll(appendixMedia);
				}

				if (appendix instanceof SourceCitationContainer sc) {
					final var appendixSources = sc.getSourceCitations(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA)).stream().map(SourceCitation::getSource).toList();
					result.addAll(appendixSources);
					queue.addAll(appendixSources);


					final var appendixRepositories = appendixSources.stream().map(Source::getRepository).filter(r -> r != null).toList();
					result.addAll(appendixRepositories);
					queue.addAll(appendixRepositories);
				}

				if (appendix instanceof Individual individual) {
					final var families = individual.getFamilies(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
					for (final var family : families) {
						if (!visited.contains(family)) {
							queue.add(family);
						}
					}
				}

				if (appendix instanceof Family family) {
					final var husband = family.getHusband();
					if (husband != null && !(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && (Structure.isConfidential(family) || Structure.isConfidential(husband)))) {
						result.addAll(getLifeLocations(husband));
					}

					final Individual wife = family.getWife();
					if (wife != null && !(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && (Structure.isConfidential(family) || Structure.isConfidential(wife)))) {
						result.addAll(getLifeLocations(wife));
					}

					final var children = family.getChildren(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA));
					if (!children.isEmpty()) {
						for (final var child : children) {
							result.addAll(getLifeLocations(child));
						}
					}
				}
			}
		}

		return new HashSet<>(result);
	}

	private static List<Location> getLifeLocations(final Individual individual) {
		final var result = new ArrayList<Location>();

		if (individual != null) {
			final var birthPlace = individual.getBirthLocation();
			if (birthPlace != null) {
				result.add(birthPlace);
			}

			final var baptismLocation = individual.getBaptismLocation();
			if (baptismLocation != null) {
				result.add(baptismLocation);
			}

			final var deathLocation = individual.getDeathLocation();
			if (deathLocation != null) {
				result.add(deathLocation);
			}

			final var burialLocation = individual.getBurialLocation();
			if (burialLocation != null) {
				result.add(burialLocation);
			}
		}

		return result;
	}

	private static String createAppendixIndex(final Set<Record> appendices) {
		final var sb = new StringBuilder();

		if (appendices != null && !appendices.isEmpty()) {
			final var locations = getLocations(appendices);
			final var media = getMedia(appendices);
			final var sources = getSources(appendices);
			final var repositories = getRepositories(appendices);

			if (!locations.isEmpty()) {
				sb.append(String.format("<span><a href='#locations'>%s</a></span>", I18N.get("Locations")));
			}

			if (!media.isEmpty()) {
				sb.append(String.format("<span><a href='#media'>%s</a></span>", I18N.get("Media")));
			}

			if (!sources.isEmpty()) {
				sb.append(String.format("<span><a href='#sources'>%s</a></span>", I18N.get("Sources")));
			}

			if (!repositories.isEmpty()) {
				sb.append(String.format("<span><a href='#repositories'>%s</a></span>", I18N.get("Repositories")));
			}
		}

		return sb.toString();
	}

	private static String createLocationsSection(final Set<Record> appendices, final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var locations = getLocations(appendices);
		if (!locations.isEmpty()) {
			sb.append(String.format(HTML_TITLE_FORMAT, I18N.get("Locations")));

			for (final var l : locations) {
				sb.append(String.format("<article id='%s' class='location'>%s</article>", l.getId(), HTMLUtils.addLinks(l.toHTML(options))));
			}
		}

		return sb.toString();
	}

	private static String createMediaSection(final Set<Record> appendices, final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var media = getMedia(appendices);
		if (!media.isEmpty()) {
			sb.append(String.format(HTML_TITLE_FORMAT, I18N.get("Media")));

			for (final var m : media) {
				sb.append(String.format("<article id='%s' class='media'>%s</article>", m.getId(), HTMLUtils.addLinks(m.toHTML(options))));
			}
		}

		return sb.toString();
	}

	private static String createSourcesSection(final Set<Record> appendices, final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var sources = getSources(appendices);
		if (!sources.isEmpty()) {
			sb.append(String.format(HTML_TITLE_FORMAT, I18N.get("Sources")));

			for (final var s : sources) {
				sb.append(String.format("<article id='%s' class='source'>%s</article>", s.getId(), HTMLUtils.addLinks(s.toHTML(options))));
			}
		}

		return sb.toString();
	}

	private static String createRepositoriesSection(final Set<Record> appendices, final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var repositories = getRepositories(appendices);
		if (!repositories.isEmpty()) {
			sb.append(String.format(HTML_TITLE_FORMAT, I18N.get("Repositories")));

			for (final var r : repositories) {
				sb.append(String.format("<article id='%s' class='repository'>%s</article>", "id", HTMLUtils.addLinks(r.toHTML(options))));
			}
		}

		return sb.toString();
	}

	private static List<Record> getLocations(final Set<Record> appendices) {
		return appendices.stream().filter(Location.class::isInstance).sorted(APPENDIX_COMPARATOR).toList();
	}

	private static List<Record> getMedia(final Set<Record> appendices) {
		return appendices.stream().filter(Media.class::isInstance).sorted(APPENDIX_COMPARATOR).toList();
	}

	private static List<Record> getSources(final Set<Record> appendices) {
		return appendices.stream().filter(Source.class::isInstance).sorted(APPENDIX_COMPARATOR).toList();
	}

	private static List<Record> getRepositories(final Set<Record> appendices) {
		return appendices.stream().filter(Repository.class::isInstance).sorted(APPENDIX_COMPARATOR).toList();
	}

	private static String createFooter(final GEDCOM gedcom, final Set<HTMLOption> options) {
		final var submitters = new StringBuilder();

		var wasAppended = false;

		final var author = gedcom.getAuthor();
		if (author != null && !(options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) && Structure.isConfidential(author))) {
			final var authorText = author.toString();
			if (!authorText.isEmpty()) {
				HTMLUtils.appendText(submitters, String.format(Format.SPACED, HTMLUtils.createStrong(I18N.get("Author")), authorText));
				wasAppended = true;
			}
		}

		var contributors = gedcom.getContributors();
		if (options.contains(HTMLOption.NO_CONFIDENTIAL_DATA)) {
			contributors = contributors.stream().filter(Predicate.not(Structure::isConfidential)).toList();
		}

		if (!contributors.isEmpty()) {
			if (wasAppended) {
				HTMLUtils.appendLineBreaks(submitters, 2);
			}
			HTMLUtils.appendLine(submitters, HTMLUtils.createStrong(I18N.get("Contributors")));
			HTMLUtils.appendText(submitters, HTMLUtils.createList(contributors, Object::toString));
		}

		final var sb = new StringBuilder();

		final var submittersText = submitters.toString();
		if (!submittersText.isEmpty()) {
			HTMLUtils.appendSection(sb, submittersText);
		}

		HTMLUtils.appendSection(sb, String.format("<span><a href='%s' target='_blank'>%s</a></span><span class='right'>%s</span>", Constants.APP_URL, Constants.APP_NAME, DateTimeUtils.format(LocalDateTime.now())));

		return sb.toString();
	}

	private ExportUtils() {}
}
