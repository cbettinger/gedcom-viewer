package bettinger.gedcomviewer;

import com.formdev.flatlaf.util.SystemInfo;

public abstract class Constants {

	public static final String APP_NAME = "GEDCOM Viewer";
	public static final String APP_VERSION = "1.0.0-pre";
	public static final String APP_URL = "https://github.com/cbettinger/gedcom-viewer";

	public static final int DEFAULT_FRAME_WIDTH = 1920;
	public static final int DEFAULT_FRAME_HEIGHT = 1080;

	public static final int DEFAULT_LEFT_SIDEBAR_WIDTH = DEFAULT_FRAME_WIDTH / 4;
	public static final int DEFAULT_RIGHT_SIDEBAR_WIDTH = DEFAULT_FRAME_WIDTH / 4;

	public static final int DEFAULT_MODAL_DIALOG_WIDTH = 640;
	public static final int DEFAULT_MODAL_DIALOG_HEIGHT = 480;

	public static final int BORDER_SIZE = 5;
	public static final int TEXT_PANE_MARGIN = 2 * BORDER_SIZE;

	public static final int MENU_ICON_SIZE = 14;
	public static final int TOOLBAR_ICON_SIZE = SystemInfo.isMacOS ? 16 : 22;
	public static final int TAB_ICON_SIZE = 22;

	public static final int PREVIEW_IMAGE_WIDTH = 350;
	public static final int PORTRAIT_WIDTH = 200;

	private Constants() {}
}
