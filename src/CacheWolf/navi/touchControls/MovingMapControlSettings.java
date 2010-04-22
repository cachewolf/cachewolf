package CacheWolf.navi.touchControls;

import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.navi.touchControls.MovingMapControlItemText.TextOptions;
import CacheWolf.navi.touchControls.MovingMapControls.Role;
import ewe.fx.Dimension;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.sys.Vm;
import ewe.util.Hashtable;
import ewe.util.Vector;
import ewe.util.mString;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;
import ewesoft.xml.sax.SAXException;

public class MovingMapControlSettings extends MinML {

	public static final String CONFIG_FILE_NAME = "movingMapControls.xml";
	
	public static final String CONFIG_FILE_NAME_OVERWRITE = "my_movingMapControls.xml";
	
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
	 * the name of content which is displayed on the button. Currently the content distance and scale are known.
	 */
	public static final String BUTTON_ATTR_CONTEXT = "content";
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

	public void startElement(String name, AttributeList attributes)
			throws SAXException {

		if (name.equals(SETTINGS)) {
			String fontsizeString = attributes.getValue(SETTINGS_ATTR_FONTSIZE);
			if (fontsizeString != null) {
				try {
					fontsize = Integer.parseInt(fontsizeString);
				} catch (Exception e) {
					Vm.debug("fontsize not an int " + fontsizeString);
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
				Vm.debug("the x position of the button has to be set! "
						+ "use attribute '" + BUTTON_ATTR_FROM_LEFT + "' or '"
						+ BUTTON_ATTR_FROM_RIGHT + "'");
				xpos = 0;
			}

			if (ypos < 0) {
				Vm.debug("the y position of the button has to be set! "
						+ "use attribute '" + BUTTON_ATTR_FROM_TOP + "' or '"
						+ BUTTON_ATTR_FROM_BOTTOM + "'");
				ypos = 0;
			}

			String changeState = attributes
					.getValue(BUTTON_ATTR_CHANGE_STAE_OF);

			String visibility = attributes.getValue(BUTTON_ATTR_VISIBILITY);
			String action = attributes.getValue(BUTTON_ATTR_ACTION);
			String localeDefault = attributes
					.getValue(BUTTON_ATTR_LOCALE_DEFAULT);
			String imageLocation = attributes.getValue(BUTTON_ATTR_LOCATION);
			String iconLocation = attributes.getValue(BUTTON_ATTR_ICON);
			String alignText = attributes.getValue(BUTTON_ATTR_ALIGNTEXT);
			String context = attributes.getValue(BUTTON_ATTR_CONTEXT);
			if (visibility == null) {
				Vm.debug("read MovingMap settings: " + BUTTON_ATTR_VISIBILITY
						+ " not set!");
				return;
			}
			if (action == null) {
				Vm.debug("read MovingMap settings: " + BUTTON_ATTR_ACTION
						+ " not set!");
				return;
			}
			int alphavalue = getIntFromFile(attributes, BUTTON_ATTR_ALPHA, -1);

			int localIDValue = getIntFromFile(attributes,
					BUTTON_ATTR_LOCALE_ID, 0);

			if (imageLocation == null) {
				// something not set
				Vm.debug("Image for '" + localeDefault + "' not found");
				return;
			}
			int localfontsize=getIntFromFile(attributes, SETTINGS_ATTR_FONTSIZE, fontsize);
			// textoptions
			TextOptions tOptions = new TextOptions(localfontsize, getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_L, 0), getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_R, 0), getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_T, 0), getIntFromFile(
					attributes, BUTTON_ATTR_TEXT_OFFSET_B, 0));

			MovingMapControlItem button;
			if (context != null) {
				button = new MovingMapControlItemText("", imageLocation,
						iconLocation, alphavalue, action, context, alignText,
						tOptions);
			} else if (localeDefault != null) {
				button = new MovingMapControlItemText(MyLocale.getMsg(
						localIDValue, localeDefault), imageLocation,
						iconLocation, alphavalue, action, context, alignText,
						tOptions);
			} else {
				button = new MovingMapControlItemButton(imageLocation,
						iconLocation, action, alphavalue);
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

	private int getIntFromFile(AttributeList attributes, String field,
			int defaultValue) {
		String entry = attributes.getValue(field);
		if (entry != null) {
			try {
				defaultValue = Integer.parseInt(entry);
			} catch (Exception e) {
				Vm.debug("Can not read int for filed " + field + ": " + entry);
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

	public boolean readFile(Dimension dest) {
		setDocumentHandler(this);
		String path = FileBase.makePath(FileBase.getProgramDirectory(),
				"mmcDesktop/");

		if (Vm.isMobile()) {
			path = FileBase.makePath(FileBase.getProgramDirectory(),
			"mmc240x320/");
			
			if (dest.height >= 640 && dest.width >= 480) {
				path = FileBase.makePath(FileBase.getProgramDirectory(),
						"mmc480x640/");
			}
		}
		path = path.replace('\\', '/');
		File file = new File(path, CONFIG_FILE_NAME_OVERWRITE);
		if (!file.exists()) {
			file = new File(path, CONFIG_FILE_NAME);
		}
		
		
		try {
			ewe.io.Reader r = new ewe.io.InputStreamReader(
					new ewe.io.FileInputStream(file));
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
}
