package bettinger.gedcomviewer.views;

import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.formdev.flatlaf.util.SystemInfo;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class WebViewPanel extends JFXPanel {

	private static final int ZOOM_MIN = 10;
	private static final int ZOOM_MAX = 300;
	private static final int ZOOM_DELTA = 10;

	private String body;

	private boolean isZoomable;
	private int zoom;

	private AnchorPane root;
	private WebView webView;

	protected JSObject js;

	public WebViewPanel() {
		this(true, null);
	}

	public WebViewPanel(final boolean isZoomable, final String resourceFolder) {
		this.body = "";

		this.isZoomable = isZoomable;
		this.zoom = 100;

		this.root = new AnchorPane();

		Platform.runLater(() -> {
			this.webView = new WebView();
			if (isZoomable) {
				webView.addEventFilter(ScrollEvent.SCROLL, event -> {
					final var deltaY = event.getDeltaY();
					if (SystemInfo.isMacOS ? event.isMetaDown() : event.isControlDown()) {
						if (deltaY > 0) {
							event.consume();
							zoomIn();
						} else if (deltaY < 0) {
							event.consume();
							zoomOut();
						}
					}
				});
			}
			root.getChildren().add(webView);

			AnchorPane.setTopAnchor(webView, 0.0);
			AnchorPane.setBottomAnchor(webView, 0.0);
			AnchorPane.setLeftAnchor(webView, 0.0);
			AnchorPane.setRightAnchor(webView, 0.0);

			final var webEngine = webView.getEngine();

			webEngine.getLoadWorker().stateProperty().addListener((o, oldState, newState) -> {
				if (newState == Worker.State.SUCCEEDED) {
					this.js = (JSObject) webEngine.executeScript("getExports()");
					onLoad();
				}
			});

			webView.getEngine().setOnAlert(event -> Logger.getLogger(WebViewPanel.class.getName()).log(Level.INFO, event.getData()));

			webEngine.load(getClass().getClassLoader().getResource(String.format("webview/%s/index.html", resourceFolder == null || resourceFolder.isEmpty() ? "default" : resourceFolder)).toString());

			this.setScene(new Scene(root));
		});
	}

	public void setBody(final String innerHTML) {
		Platform.runLater(() -> {
			if (js != null && innerHTML != null) {
				body = innerHTML;
				js.call("setBody", innerHTML);
			}
		});
	}

	public String getBody() {
		return body;
	}

	public void scrollTo(final Point target) {
		Platform.runLater(() -> {
			if (js != null && target != null) {
				js.call("setScrollTo", target.x, target.y);
			}
		});
	}

	public void call(final String fn) {
		Platform.runLater(() -> {
			if (js != null && fn != null) {
				js.call(fn);
			}
		});
	}

	public boolean canZoomOut() {
		return getZoom() - ZOOM_DELTA >= ZOOM_MIN;
	}

	public boolean canZoomIn() {
		return getZoom() + ZOOM_DELTA <= ZOOM_MAX;
	}

	public int getZoom() {
		return zoom;
	}

	public void zoomOut() {
		setZoom(getZoom() - ZOOM_DELTA);
	}

	public void zoomIn() {
		setZoom(getZoom() + ZOOM_DELTA);
	}

	public void resetZoom() {
		setZoom(100);
	}

	public void setZoom(final int zoom) {
		if (webView != null && isZoomable && zoom >= ZOOM_MIN && zoom <= ZOOM_MAX) {
			final var lastZoom = this.zoom;
			this.zoom = zoom;
			Platform.runLater(() -> webView.setZoom(zoom / 100.0));
			firePropertyChange("zoom", lastZoom, zoom);
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		this.root.setVisible(enabled);
		this.root.setDisable(!enabled);
	}

	protected void add(final Node e) {
		Platform.runLater(() -> {
			if (root != null && e != null) {
				root.getChildren().add(e);
			}
		});
	}

	protected void onLoad() {
		// intentionally left blank
	}
}
