package CacheWolf.navi.touchControls;

public interface ICommandListener {

	public static final String SELECT_MAP = "selectMap";

	public static final String CHANGE_MAP_DIR = "changeMapDir";

	public static final String MOVE_TO_GPS = "moveToGps";

	public static final String MOVE_TO_DEST = "moveToDest";

	public static final String MOVE_TO_CENTER = "moveToCenter";

	public static final String ALL_CACHES_RES = "allCachesRes";

	public static final String MORE_OVERVIEW = "moreOverview";

	public static final String MORE_DETAILS = "moreDetails";

	public static final String KEEP_MAN_RESOLUTION = "keepManResolution";

	public static final String HIGHEST_RES = "highestResolution";

	public static final String HIGHEST_RES_GPS_DEST = "highestResGpsDest";

	public static final String SHOW_MAP = "showMap";

	public static final String HIDE_MAP = "hideMap";

	public static final String SHOW_MENU = "menu";

	public static final String HIDE_MENU = "hide_menu";
	
	public static final String SHOW_CACHES = "show_caches";

	public static final String HIDE_CACHES = "hide_caches";

	public static final String ZOOMIN = "zoomin";

	public static final String ZOOMOUT = "zoomout";
	
	public static final String ZOOM_1_TO_1 = "1to1";

	public static final String MAP_MOVED = "map_moved";
	
	public static final String POS_UPDATED = "pos_updated";
	
	public static final String GOTO_UPDATED = "goto_updated";

	public static final String CLOSE = "close";

	public static final String FILL_MAP = "fillMap";
	
	public static final String NO_FILL_MAP = "nofillMap";

	public boolean handleCommand(String command);

}
