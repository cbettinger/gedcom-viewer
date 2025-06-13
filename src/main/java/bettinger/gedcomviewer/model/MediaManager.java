package bettinger.gedcomviewer.model;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.folg.gedcom.model.GedcomTag;
import org.folg.gedcom.model.MediaRef;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

class MediaManager {

	private static final String SIGN = "▷";

	private final List<Media> media;
	private Media primaryImage;
	private final Map<Media, Rectangle> imageClips;

	MediaManager(final Structure referencingStructure, final GEDCOM gedcom) {
		this(referencingStructure, gedcom, referencingStructure.getExtensionTags(Media.TAG).stream().map(objeTag -> {
			final var mediaRef = new MediaRef();
			mediaRef.setRef(objeTag.getRef());
			return mediaRef;
		}).toList());
	}

	MediaManager(final Structure referencingStructure, final GEDCOM gedcom, final GedcomTag tag) {
		this(referencingStructure, gedcom, TagUtils.getChildTags(tag, Media.TAG).stream().map(objeTag -> {
			final var mediaRef = new MediaRef();
			mediaRef.setRef(objeTag.getRef());
			return mediaRef;
		}).toList());
	}

	MediaManager(final Structure referencingStructure, final GEDCOM gedcom, final List<MediaRef> mediaRefs) {
		this.imageClips = new HashMap<>();

		this.media = mediaRefs.stream().map(mr -> {
			final var medium = (Media) gedcom.getRecord(mr.getRef());

			if (medium.isImage()) {
				if (primaryImage == null) {
					primaryImage = medium;
				}

				final var primary = TagUtils.getFirstExtensionTagValue(mr, "_PRIM");
				if (primary.equals("Y")) {
					primaryImage = medium;
				}

				final var clip = TagUtils.getFirstExtensionTagValue(mr, "_POSITION");
				if (!clip.isEmpty()) {
					final var coordinates = clip.split(" ");
					if (coordinates.length == 4) {
						try {
							final var x1 = Integer.parseInt(coordinates[0]);
							final var y1 = Integer.parseInt(coordinates[1]);
							final var x2 = Integer.parseInt(coordinates[2]);
							final var y2 = Integer.parseInt(coordinates[3]);
							imageClips.put(medium, new Rectangle(x1, y1, x2 - x1, y2 - y1));
						} catch (final NumberFormatException _) {
							// intentionally left blank
						}
					}
				}
			}

			return medium;
		}).distinct().toList();

		for (final var m : this.media) {
			m.addReference(referencingStructure);
		}
	}

	public List<Media> getMedia() {
		return getMedia(false);
	}

	public List<Media> getMedia(final boolean excludeConfidential) {
		return excludeConfidential ? media.stream().filter(Predicate.not(Structure::isConfidential)).toList() : media;
	}

	public void appendPortrait(final StringBuilder sb) {
		appendPrimaryImage(sb, true, Constants.PORTRAIT_WIDTH);
	}

	public void appendPrimaryPhoto(final StringBuilder sb) {
		appendPrimaryImage(sb, true, Constants.PREVIEW_IMAGE_WIDTH);
	}

	public void appendPrimaryImage(final StringBuilder sb) {
		appendPrimaryImage(sb, false, Constants.PREVIEW_IMAGE_WIDTH);
	}

	public void appendPrimaryImage(final StringBuilder sb, final boolean onlyPhoto, final int width) {
		final var image = getPrimaryImage(onlyPhoto);
		if (image != null && image.exists()) {
			final var imageURL = image.getURL();
			if (!imageURL.isEmpty()) {
				HTMLUtils.appendLineBreak(sb);
				HTMLUtils.appendImage(sb, imageURL, width);
			}
		}
	}

	public Media getPrimaryImage(final boolean onlyPhoto) {
		return primaryImage != null && (!onlyPhoto || Media.Type.PHOTO.equals(primaryImage.getType())) ? primaryImage : null;
	}

	public Rectangle getImageClip(final Media image) {
		return image == null ? null : imageClips.get(image);
	}

	public String toHTML(final Set<HTMLOption> options) {
		final var sb = new StringBuilder();

		final var publicMedia = options.contains(HTMLOption.NO_CONFIDENTIAL_DATA) ? media.stream().filter(Predicate.not(Structure::isConfidential)).toList() : media;
		if (!publicMedia.isEmpty()) {
			if (options.contains(HTMLOption.COMMONS_HEADINGS)) {
				HTMLUtils.appendH2(sb, I18N.get("Media"));
			} else {
				HTMLUtils.appendLineBreaks(sb, 2);
			}

			HTMLUtils.appendText(sb, HTMLUtils.createList(publicMedia, m -> {
				var mediaSourcesLink = "";
				final var mediaSources = m.getSourceCitations();
				if (options.contains(HTMLOption.MEDIA_SOURCES) && !mediaSources.isEmpty()) {
					mediaSourcesLink = String.format(Format.BRACKETED, HTMLUtils.createSingleLineList(mediaSources, SourceCitation::getLink, ";"));
				}
				return String.format(Format.SPACED, m.getLink(), mediaSourcesLink).trim();
			}, options.contains(HTMLOption.COMMONS_HEADINGS) ? null : SIGN));
		}

		return sb.toString();
	}
}
