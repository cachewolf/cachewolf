    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    */
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
	public static final String CONTEXT_GOTO = "context_goto";
	public static final String CONTEXT_NEW_WAY_POINT = "context_nwp";
	public static final String CONTEXT_OPEN_CACHE_DESC = "context_ocDesc";
	public static final String CONTEXT_OPEN_CACHE_DETAIL =  "context_ocDetail";
	public static final String CONTEXT_GOTO_CACHE = "context_goto_cache";
	public static final String CONTEXT_TOUR = "context_tour";
	public boolean handleCommand(String command);
}
