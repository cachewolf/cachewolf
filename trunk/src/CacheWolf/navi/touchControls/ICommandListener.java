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
	public static final int SELECT_MAP = 0;//"selectMap";
	public static final int CHANGE_MAP_DIR = 1;//"changeMapDir";
	public static final int MOVE_TO_GPS = 2;//"moveToGps";
	public static final int MOVE_TO_DEST = 3;//"moveToDest";
	public static final int MOVE_TO_CENTER = 4;//"moveToCenter";
	public static final int ALL_CACHES_RES = 5;//"allCachesRes";
	public static final int MORE_OVERVIEW = 6;//"moreOverview";
	public static final int MORE_DETAILS = 7;//"moreDetails";
	public static final int KEEP_MAN_RESOLUTION = 8;//"keepManResolution";
	public static final int changeStateOfRole = 9;//"changeStateOfRole";
	public static final int HIGHEST_RES = 10;//"highestResolution";
	public static final int HIGHEST_RES_GPS_DEST = 11;//"highestResGpsDest";
	public static final int SHOW_MAP = 12;//"showMap";
	public static final int HIDE_MAP = 13;//"hideMap";
	public static final int SHOW_MENU = 14;//"menu";
	public static final int HIDE_MENU = 15;//"hide_menu";
	public static final int SHOW_CACHES = 16;//"show_caches";
	public static final int HIDE_CACHES = 17;//"hide_caches";
	public static final int ZOOMIN = 18;//"zoomin";
	public static final int ZOOMOUT = 19;//"zoomout";
	public static final int ZOOM_1_TO_1 = 20;//"1to1";
	public static final int MAP_MOVED = 21;//"map_moved";
	public static final int POS_UPDATED = 22;//"pos_updated";
	public static final int GOTO_UPDATED = 23;//"goto_updated";
	public static final int CLOSE = 24;//"close";
	public static final int FILL_MAP = 25;//"fillMap";
	public static final int NO_FILL_MAP = 26;//"nofillMap";
	public static final int CONTEXT_GOTO = 27;//"context_goto";
	public static final int CONTEXT_NEW_WAY_POINT = 28;//"context_nwp";
	public static final int CONTEXT_OPEN_CACHE_DESC = 29;//"context_ocDesc";
	public static final int CONTEXT_OPEN_CACHE_DETAIL =  30;//"context_ocDetail";
	public static final int CONTEXT_GOTO_CACHE = 31;//"context_goto_cache";
	public static final int CONTEXT_TOUR = 32;//"context_tour";
	public boolean handleCommand(int command);
}
