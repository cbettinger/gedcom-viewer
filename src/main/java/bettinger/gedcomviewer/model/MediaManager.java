package bettinger.gedcomviewer.model;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.folg.gedcom.model.GedcomTag;
import org.folg.gedcom.model.MediaRef;
import org.javatuples.Triplet;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.utils.TagUtils;

class MediaManager {

	private static final String SIGN = "▷";

	private final List<Media> media;
	private Media primaryImage;
	private final List<Media> facialPortraits;
	private final Map<Media, Rectangle> imageClips;
	private final Map<Triplet<Media, Integer, Integer>, Image> cachedClippedImages;

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
		this.facialPortraits = new ArrayList<>();
		this.imageClips = new HashMap<>();
		this.cachedClippedImages = new HashMap<>();

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

				final var facial = TagUtils.getFirstExtensionTagValue(mr, "_FACIAL");
				if (facial.equals("Y")) {
					facialPortraits.add(medium);
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
						} catch (final NumberFormatException e) {
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

	public Image getClippedImage(final Media image, final int width, final int height) {
		final var key = new Triplet<>(image, width, height);
		if (cachedClippedImages.containsKey(key)) {
			return cachedClippedImages.get(key);
		}

		Image result = image.getImage();

		final var clip = imageClips.get(image);
		if (result != null && clip != null) {
			result = ((BufferedImage) result).getSubimage(clip.x, clip.y, clip.width, clip.height);

			if (width != -1 || height != -1) {
				result = result.getScaledInstance(width, height, Image.SCALE_FAST);
			}

			cachedClippedImages.put(key, result);
		}

		return result;
	}

	public List<Media> getFacialPortraits() {
		return facialPortraits;
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
