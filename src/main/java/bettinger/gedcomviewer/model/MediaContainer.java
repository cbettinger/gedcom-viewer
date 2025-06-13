package bettinger.gedcomviewer.model;

import java.awt.Rectangle;
import java.util.List;

public interface MediaContainer {
	List<Media> getMedia();
	List<Media> getMedia(final boolean excludeConfidential);
	Media getPrimaryImage(final boolean onlyPhoto);
	Rectangle getImageClip(final Media image);
}
