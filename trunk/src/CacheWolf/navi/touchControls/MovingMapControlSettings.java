package CacheWolf.navi.touchControls;

import CacheWolf.Global;
import CacheWolf.MyLocale;
import CacheWolf.Preferences;
import CacheWolf.navi.touchControls.MovingMapControls.Role;
import ewe.fx.Dimension;
import ewe.io.FileBase;
import ewe.sys.VMApp;
import ewe.sys.Vm;
import ewe.util.Hashtable;
import ewe.util.Vector;
import ewe.util.mString;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;
import ewesoft.xml.sax.SAXException;

public class MovingMapControlSettings extends MinML {

	public static final String SETTINGS = "settings";
	public static final String SETTINGS_ATTR_FONTSIZE = "fontsize";

	public static final String ROLE = "role";
	public static final String ROLE_ATTR_NAME = "name";
	public static final String ROLE_ATTR_ACTIVE = "active";
	public static final String ROLE_ATTR_DISABLE = "disable";

	public static final String BUTTON = "button";
	public static final String BUTTON_ATTR_FROM_LEFT = "fromLeft";
	public static final String BUTTON_ATTR_FROM_TOP = "fromTop";
	public static final String BUTTON_ATTR_FROM_RIGHT = "fromRight";
	public static final String BUTTON_ATTR_FROM_BOTTOM = "fromBottom";
	public static final String BUTTON_ATTR_VISIBILITY = "visibleIf";
	public static final String BUTTON_ATTR_CHANGE_STAE_OF = "changeStateof";

	public static final String BUTTON_ATTR_LOCALE_ID = "localeID";
	public static final String BUTTON_ATTR_LOCALE_DEFAULT = "localeDefault";
	public static final String BUTTON_ATTR_ALPHA = "alpha";
	public static final String BUTTON_ATTR_LOCATION = "location";
	public static final String BUTTON_ATTR_ICON = "icon";
	public static final String BUTTON_ATTR_ACTION = "action";
	public static final String BUTTON_ATTR_CONTEXT = "content";
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
					r.setState(Boolean.TRUE);
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

			if (visibility == null) {
				Vm.debug("no valid " + BUTTON_ATTR_VISIBILITY + " set.");
				return;
			}

			String action = attributes.getValue(BUTTON_ATTR_ACTION);
			String alpha = attributes.getValue(BUTTON_ATTR_ALPHA);
			String localeDefault = attributes
					.getValue(BUTTON_ATTR_LOCALE_DEFAULT);
			String localeID = attributes.getValue(BUTTON_ATTR_LOCALE_ID);
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
			// convert values
			int alphavalue = -1;
			if (alpha != null) {
				try {
					alphavalue = Integer.parseInt(alpha);
				} catch (Exception e) {
					// nothing
				}
			}

			int localIDValue = -1;

			if (localeID != null) {
				try {
					localIDValue = Integer.parseInt(localeID);
				} catch (Exception e) {
					Vm.debug(e.getMessage());
				}
			}

			if (imageLocation == null) {
				// something not set
				Vm.debug("Image for '" + localeDefault + "' not found");
				return;
			}

			MovingMapControlItem button;
			if (context != null) {
				button = new MovingMapControlItemText("", imageLocation,
						iconLocation, alphavalue, action, context, alignText,
						fontsize);
			} else if (localeDefault != null) {
				button = new MovingMapControlItemText(MyLocale.getMsg(
						localIDValue, localeDefault), imageLocation,
						iconLocation, alphavalue, action, context, alignText,
						fontsize);
			} else {

				button = new MovingMapControlItemButton(imageLocation,
						iconLocation, action, alphavalue);
			}

			button.setVisibilityRole(visibility);
			if (changeState != null) {
				button.setRole(changeState);
			}

			button.setPosition(xpos, ypos);
			button.addXtraProperties(xProperties);

			menuItems.add(button);
		}

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

		String file = FileBase.makePath(FileBase.getProgramDirectory(),
				"mmcDefault/movingMapControls.xml");

		if (dest.height <= 320 && dest.width <= 240) {
			file = FileBase.makePath(FileBase.getProgramDirectory(),
					"mmc240x320/movingMapControls.xml");
		} else if (dest.height <= 640 && dest.width <= 480) {
			file = FileBase.makePath(FileBase.getProgramDirectory(),
					"mmc480x640/movingMapControls.xml");
		}
		try {
			Vm.debug("read mmc file " + file);
			file = file.replace('\\', '/');
			ewe.io.Reader r = new ewe.io.InputStreamReader(
					new ewe.io.FileInputStream(file));
			parse(r);
			r.close();
		} catch (Exception e) {
			if (e instanceof NullPointerException)
				Global.getPref().log(
						"Error reading " + file
								+ ": NullPointerException in Element " + ""
								+ ". Wrong attribute, File not existing?", e,
						true);
			else
				Global.getPref().log("Error reading " + file + ": ", e);
			return false;
		}
		return true;
	}

	public Vector getMenuItems() {
		return menuItems;
	}
}
