package bettinger.gedcomviewer.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.svg.SVGDocument;

public interface SVGUtils {

	static final BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
	static final GVTBuilder builder = new GVTBuilder();

	static final int PADDING = 20;

	public static SVGDocument createDocument() {
		return (SVGDocument) SVGDOMImplementation.getDOMImplementation().createDocument("http://www.w3.org/2000/svg", "svg", null);
	}

	public static SVGGraphics2D createGraphics(final SVGDocument doc) {
		final var context = SVGGeneratorContext.createDefault(doc);
		context.setComment(null);
		return new SVGGraphics2D(context, false);
	}

	public static void setSize(final SVGDocument doc, final SVGGraphics2D g) {
		g.getRoot(doc.getDocumentElement());

		final var bounds = builder.build(ctx, doc).getSensitiveBounds();
		doc.getRootElement().setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(bounds.getMaxX() + PADDING));
		doc.getRootElement().setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(bounds.getMaxY() + PADDING));
	}

	public static String toString(final SVGDocument doc) {
		var result = "";

		try (final var writer = new StringWriter()) {
			final var transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(doc.getDocumentElement()), new StreamResult(writer));
			result = writer.toString();
		} catch (final Exception e) {
			// intentionally left blank
		}

		return result;
	}

	public static URI saveAs(final String content, final File target) throws IOException {
		try (final var writer = new FileWriter(target)) {
			writer.write(content);
			return target.toURI();
		}
	}
}
