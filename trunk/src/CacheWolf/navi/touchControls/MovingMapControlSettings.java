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

import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.navi.touchControls.MovingMapControlItemText.TextOptions;
import CacheWolf.navi.touchControls.MovingMapControls.Role;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.sys.Vm;
import ewe.util.Hashtable;
import ewe.util.Vector;
import ewe.util.mString;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;
import ewesoft.xml.sax.SAXException;

public class MovingMapControlSettings extends MinML  implements ICommandListener {

	public static final String CONFIG_FILE_NAME = "movingMapControls.xml";
	public static final String CONFIG_FILE_NAME_OVERWRITE = "my_movingMapControls.xml";
	public static String CONFIG_RELATIVE_PATH = "mmc/";
	public static final String SETTINGS = "settings";
	/**
	 * the size of the font on the icons
	 */
	public static final String SETTINGS_ATTR_FONTSIZE = "fontsize";
	public static final String ROLE = "role";
	/**
	 * name of the role [String]
	 */
	public static final String ROLE_ATTR_NAME = "name";
	/**
	 * the inital state of the role [true|false]
	 */
	public static final String ROLE_ATTR_ACTIVE = "active";
	/**
	 * A list of roles to disable, if this role changes the state from active to inactive. Delimiter is '|'.
	 * [String|String|...]
	 */
	public static final String ROLE_ATTR_DISABLE = "disable";
	public static final String BUTTON = "button";
	/**
	 * position of the left upper corner of the button from the left screen border [int (pixel)]
	 */
	public static final String BUTTON_ATTR_FROM_LEFT = "fromLeft";
	/**
	 * position of the left upper corner of the button from the upper screen border [int (pixel)]
	 */
	public static final String BUTTON_ATTR_FROM_TOP = "fromTop";
	/**
	 * position of the left upper corner of the button from the right screen border. If fromLeft is set, this option
	 * will not be read! [int (pixel)]
	 */
	public static final String BUTTON_ATTR_FROM_RIGHT = "fromRight";
	/**
	 * position of the left upper corner of the button from the bottom screen border. If fromTop is set, this option
	 * will not be read! [int (pixel)]
	 */
	public static final String BUTTON_ATTR_FROM_BOTTOM = "fromBottom";
	/**
	 * define when this button is visible. Contains a list of rolenames. Delimiter is '+'. If the role name starts with
	 * '!' the buttom is shown if this role is inactive [(!)String+(!)String...] Example visibleIf="menu+!zoom" button
	 * is visible if role "menu" is active and role "zoom" is inactive.
	 */
	public static final String BUTTON_ATTR_VISIBILITY = "visibleIf";
	/**
	 * the id of the text in the language file
	 */
	public static final String BUTTON_ATTR_LOCALE_ID = "localeID";
	/**
	 * the default text on the button if no localID is set or found.
	 */
	public static final String BUTTON_ATTR_LOCALE_DEFAULT = "localeDefault";
	/**
	 * the alpha value set the level of transparency of the button. [0-255]
	 */
	public static final String BUTTON_ATTR_ALPHA = "alpha";
	/**
	 * the file path and name of the button
	 */
	public static final String BUTTON_ATTR_LOCATION = "location";
	/**
	 * the file path and name of the icon which can be displayed on the button
	 */
	public static final String BUTTON_ATTR_ICON = "icon";
	public static final String BUTTON_ATTR_ICON_OFFSET_X = "iconX";
	public static final String BUTTON_ATTR_ICON_OFFSET_Y = "iconY";
	public static final String BUTTON_ATTR_TEXT_OFFSET_L = "textOffsetLeft";
	public static final String BUTTON_ATTR_TEXT_OFFSET_R = "textOffsetRight";
	public static final String BUTTON_ATTR_TEXT_OFFSET_T = "textOffsetTop";
	public static final String BUTTON_ATTR_TEXT_OFFSET_B = "textOffsetBottom";
	/**
	 * the action command. Defines what is to do if the button is clicked
	 */
	public static final String BUTTON_ATTR_ACTION = "action";
	/**
	 * if the defined action is "changeStateOfRole", this attribute defines which role state should be changed [String]
	 */
	public static final String BUTTON_ATTR_CHANGE_STAE_OF = "changeStateof";
	/**
	 * the name of content which is displayed on the button.
	 * Currently the content distance, scale, hdop, sats are known.
	 */
	public static final String BUTTON_ATTR_CONTENT = "content";
	/**
	 * the alignment of the text on the buttons. if the String starts with 'T' the text will be displayed on the top
	 * line of the button if the String starts with 'B' the text will be displayed on the bottom line of the button if
	 * the String starts ends 'L' the text will be displayed on the left side of the button if the String starts ends
	 * 'R' the text will be displayed on the right side of the button otherwise horizontal and vertical center will be
	 * used as default
	 */
	public static final String BUTTON_ATTR_ALIGNTEXT = "alignText";
	Vector menuItems = new Vector(10);
	private Hashtable roles;
	private int fontsize;
	public MovingMapControlSettings(boolean vga, Hashtable roles) {
		double fontscale = vga ? 1.5 : 1;
		this.fontsize = (int) (Global.getPref().fontSize * fontscale);
		this.roles = roles;
	}
	public void startElement(String name, AttributeList attributes) throws SAXException {
		if (name.equals(SETTINGS)) {
			String fontsizeString = attributes.getValue(SETTINGS_ATTR_FONTSIZE);
			if (fontsizeString != null) {
				try {
					fontsize = Integer.parseInt(fontsizeString);
				} catch (Exception e) {
					Global.getPref().log("fontsize not an int " + fontsizeString,e);
				}
			}
		}

		if (name.equals(ROLE)) {
			String role_name = attributes.getValue(ROLE_ATTR_NAME);
			String role_active = attributes.getValue(ROLE_ATTR_ACTIVE);
			String disable = attributes.getValue(ROLE_ATTR_DISABLE);

			if (role_name != null) {
				Role r = new Role();
				roles.put(role_name, r);

				if (role_active != null) {
					r.setState(true);
				}
				if (disable != null) {
					r.setRolesToDisable(mString.split(disable));
				}
			}
		}

		if (name.equals(BUTTON)) {
			int xProperties = 0;
			String fromLeft = attributes.getValue(BUTTON_ATTR_FROM_LEFT);
			String fromRight = attributes.getValue(BUTTON_ATTR_FROM_RIGHT);
			String fromTop = attributes.getValue(BUTTON_ATTR_FROM_TOP);
			String fromBottom = attributes.getValue(BUTTON_ATTR_FROM_BOTTOM);

			int xpos = toIntValue(fromLeft);
			if (xpos < 0) {
				xpos = toIntValue(fromRight);
				xProperties |= MovingMapControlItem.DISPLAY_FROM_RIGHT;
			} else {
				xProperties |= MovingMapControlItem.DISPLAY_FROM_LEFT;
			}

			int ypos = toIntValue(fromTop);
			if (ypos < 0) {
				ypos = toIntValue(fromBottom);
				xProperties |= MovingMapControlItem.DISPLAY_FROM_BOTTOM;
			} else {
				xProperties |= MovingMapControlItem.DISPLAY_FROM_TOP;
			}

			if (xpos < 0) {
				Global.getPref().log("the x position of the button has to be set! "
						+ "use attribute '" + BUTTON_ATTR_FROM_LEFT + "' or '"
						+ BUTTON_ATTR_FROM_RIGHT + "'",null);
				xpos = 0;
			}

			if (ypos < 0) {
				Global.getPref().log("the y position of the button has to be set! "
						+ "use attribute '" + BUTTON_ATTR_FROM_TOP + "' or '"
						+ BUTTON_ATTR_FROM_BOTTOM + "'",null);
				ypos = 0;
			}

			String changeState = attributes.getValue(BUTTON_ATTR_CHANGE_STAE_OF);

			String visibility = attributes.getValue(BUTTON_ATTR_VISIBILITY);
			int action = getCommand(attributes.getValue(BUTTON_ATTR_ACTION));
			String localeDefault = attributes.getValue(BUTTON_ATTR_LOCALE_DEFAULT);
			String imageLocation = attributes.getValue(BUTTON_ATTR_LOCATION);
			String iconLocation = attributes.getValue(BUTTON_ATTR_ICON);
			if (iconLocation != null) {
				iconLocation = CONFIG_RELATIVE_PATH + iconLocation; 
			}
			String alignText = attributes.getValue(BUTTON_ATTR_ALIGNTEXT);
			String content = attributes.getValue(BUTTON_ATTR_CONTENT);
			if (visibility == null) {
				Global.getPref().log("read MovingMap settings: " + BUTTON_ATTR_VISIBILITY
						+ " not set!",null);
				return;
			}
			if (action == -2) {
				Global.getPref().log("read MovingMap settings: " + BUTTON_ATTR_ACTION + " not set!",null);
				return;
			}
			int alphavalue = getIntFromFile(attributes, BUTTON_ATTR_ALPHA, -1);

			int localIDValue = getIntFromFile(attributes, BUTTON_ATTR_LOCALE_ID, 0);

			if (imageLocation == null) {
				// something not set
				Global.getPref().log("Image for '" + localeDefault + "' not found",null);
				return;
			}
			else {
				imageLocation = CONFIG_RELATIVE_PATH + imageLocation; 
			}
			int localfontsize=getIntFromFile(attributes, SETTINGS_ATTR_FONTSIZE, fontsize);
			// textoptions
			TextOptions tOptions = new TextOptions(localfontsize, getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_L, 0), getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_R, 0), getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_T, 0), getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_B, 0));

			MovingMapControlItem button;
			if (content != null) {
				button = new MovingMapControlItemText("", 
						imageLocation, iconLocation, alphavalue, action, content, alignText, tOptions);
			} else if (localeDefault != null) {
				button = new MovingMapControlItemText(MyLocale.getMsg(localIDValue, localeDefault),
						imageLocation, iconLocation, alphavalue, action, content, alignText, tOptions);
			} else {
				button = new MovingMapControlItemButton(imageLocation, iconLocation, action, alphavalue);
			}

			// add extra role to all icons
			visibility += "+!" + MovingMapControls.ROLE_WORKING;
			button.setVisibilityRole(visibility);
			if (changeState != null) {
				button.setRole(changeState);
			}

			button.setPosition(xpos, ypos);
			button.addXtraProperties(xProperties);

			menuItems.add(button);
		}

	}

	private int getCommand(String value) {
		if (value == null ) return -2;
		else if (value.equals("selectMap")) return SELECT_MAP;
		else if (value.equals("changeMapDir")) return CHANGE_MAP_DIR;
		else if (value.equals("moveToGps")) return MOVE_TO_GPS;
		else if (value.equals("moveToDest")) return MOVE_TO_DEST;
		else if (value.equals("moveToCenter")) return MOVE_TO_CENTER;
		else if (value.equals("allCachesRes")) return ALL_CACHES_RES;
		else if (value.equals("moreOverview")) return MORE_OVERVIEW;
		else if (value.equals("moreDetails")) return MORE_DETAILS;
		else if (value.equals("keepManResolution")) return KEEP_MAN_RESOLUTION;
		else if (value.equals("changeStateOfRole")) return changeStateOfRole;
		else if (value.equals("highestResolution")) return HIGHEST_RES;
		else if (value.equals("highestResGpsDest")) return HIGHEST_RES_GPS_DEST;
		else if (value.equals("showMap")) return SHOW_MAP;
		else if (value.equals("hideMap")) return HIDE_MAP;
		else if (value.equals("menu")) return SHOW_MENU;
		else if (value.equals("hide_menu")) return HIDE_MENU;
		else if (value.equals("show_caches")) return SHOW_CACHES;
		else if (value.equals("hide_caches")) return HIDE_CACHES;
		else if (value.equals("zoomin")) return ZOOMIN;
		else if (value.equals("zoomout")) return ZOOMOUT;
		else if (value.equals("1to1")) return ZOOM_1_TO_1;
		else if (value.equals("map_moved")) return MAP_MOVED;
		else if (value.equals("pos_updated")) return POS_UPDATED;
		else if (value.equals("goto_updated")) return GOTO_UPDATED;
		else if (value.equals("close")) return CLOSE;
		else if (value.equals("fillMap")) return FILL_MAP;
		else if (value.equals("nofillMap")) return NO_FILL_MAP;
		else if (value.equals("context_goto")) return CONTEXT_GOTO;
		else if (value.equals("context_nwp")) return CONTEXT_NEW_WAY_POINT;
		else if (value.equals("context_ocDesc")) return CONTEXT_OPEN_CACHE_DESC;
		else if (value.equals("context_ocDetail")) return CONTEXT_OPEN_CACHE_DETAIL;
		else if (value.equals("context_goto_cache")) return CONTEXT_GOTO_CACHE;
		else if (value.equals("context_tour")) return CONTEXT_TOUR;
		else return -1;
	}

	private int getIntFromFile(AttributeList attributes, String field,
			int defaultValue) {
		String entry = attributes.getValue(field);
		if (entry != null) {
			try {
				defaultValue = Integer.parseInt(entry);
			} catch (Exception e) {
				Global.getPref().log("Can not read int for filed " + field + ": " + entry,e);
			}
		}
		return defaultValue;
	}

	private int toIntValue(String pos) {
		try {
			return Integer.parseInt(pos);
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean readFile() {
		setDocumentHandler(this);
		String tmp = CONFIG_RELATIVE_PATH+"Desktop/";

		if (Vm.isMobile()) {
			tmp=CONFIG_RELATIVE_PATH+"pda/";
			
			if (MyLocale.getScreenHeight() >= 480 && MyLocale.getScreenWidth() >= 480) {
				tmp=CONFIG_RELATIVE_PATH+"pda_vga/";
			}
		}
		CONFIG_RELATIVE_PATH=tmp;
		String path = FileBase.makePath(FileBase.getProgramDirectory(),CONFIG_RELATIVE_PATH);
		path = path.replace('\\', '/');
		File file = new File(path, CONFIG_FILE_NAME_OVERWRITE);
		if (!file.exists()) {file = new File(path, ""+MyLocale.getScreenWidth()+"x"+MyLocale.getScreenHeight()+".xml");}
		if (!file.exists()) {file = new File(path, CONFIG_FILE_NAME);}
		
		
		try {
			ewe.io.Reader r = new ewe.io.InputStreamReader(new ewe.io.FileInputStream(file));
			parse(r);
			r.close();
		} catch (Exception e) {
			if (e instanceof NullPointerException)
				Global.getPref().log(
						"Error reading " + path
								+ ": NullPointerException in Element " + ""
								+ ". Wrong attribute, File not existing?", e,
						true);
			else
				Global.getPref().log("Error reading " + path + ": ", e);
			return false;
		}
		return true;
	}

	public Vector getMenuItems() {
		return menuItems;
	}
	public boolean handleCommand(int command) {
		// dummy
		return false;
	}
}
