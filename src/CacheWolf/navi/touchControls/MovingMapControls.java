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
import CacheWolf.Preferences;
import CacheWolf.navi.MovingMap;
import ewe.fx.Dimension;
import ewe.fx.Point;
import ewe.graphics.AniImage;
import ewe.sys.Vm;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
 * @author Hälmchen
 */

public class MovingMapControls implements ICommandListener {
	private static final String ROLE_ZOOM_MANUALLY = "zoom_manually";
	private static final String ROLE_FILL_WHITE = "fill_white";
	private static final String ROLE_SHOW_CACHES = "show_caches";
	private static final String ROLE_SHOW_MAP = "show_map";
	public static final String ROLE_MENU = "menu";
	public static final String ROLE_DONE = "done";
	public static final String ROLE_WORKING = "working";
	Vector buttons = null;
	Vector visibleImages = null;
	private boolean vga;
	private MovingMap movingMap;
	private int lastTime = Vm.getTimeStamp();
	private Hashtable roles = new Hashtable();
	public MovingMapControls(MovingMap movingMap) {
		if (movingMap == null) {
			throw new IllegalArgumentException("moving map not set");
		}
		Vm.showWait(movingMap, true);
		this.vga = movingMap.isMobileVga();
		this.movingMap = movingMap;
		Dimension di = new Dimension();
		movingMap.getDisplayedSize(di);
		MovingMapControlSettings movingMapControlSettings = new MovingMapControlSettings(vga, roles);

		movingMapControlSettings.readFile();
		buttons = movingMapControlSettings.getMenuItems();
		
		checkStatesofRole(Global.getPref());
				
		visibleImages = new Vector();

		roles.put(ROLE_WORKING, new Role());

		// create all needed Buttons
		setStateOfIcons();
		Vm.showWait(movingMap, false);
	}

	/**
	 * some roles are active at start
	 * @param pref 
	 */
	private void checkStatesofRole(Preferences pref) {
		Role role = getRole(ROLE_FILL_WHITE);
		if (role!=null) {
			role.setState(pref.fillWhiteArea);
		}
		role = getRole(ROLE_SHOW_CACHES);
		if (role!=null) {
			role.setState(pref.showCachesOnMap);
		}
	}

	private void setStateOfIcons() {
		for (int i = 0; i < visibleImages.size(); i++) {
			AniImage ani = (AniImage) visibleImages.get(i);
			movingMap.getMmp().removeImage(ani);
		}
		for (int i = 0; i < buttons.size(); i++) {
			MovingMapControlItem item = (MovingMapControlItem) buttons.get(i);

			if (!item.isVisible(roles)) {
				continue;
			}
			AniImage ani = item.getImage();
			if (ani == null) {
				continue;
			}
			movingMap.getMmp().addImage(ani);
			visibleImages.add(ani);
		}
	}

	public boolean changeRoleState(String role) {
		Object object = roles.get(role);
		if (object == null) {
			return false;
		}
		Role r = (Role) object;
		if (r.getState() == true) {
			return changeRoleState(role, r, false);
		} else {
			return changeRoleState(role, r, true);
		}
	}

	public boolean getStateOfRole(String role) {
		Role r =getRole(role);
		if (r == null) {
			return false;
		}
		return r.getState();

	}
	public Role getRole(String role) {
		Object object = roles.get(role);
		if (object == null) {
			return null;
		}
		return (Role) object;

	}
	public boolean changeRoleState(String role, boolean b) {
		Object object = roles.get(role);
		if (object == null) {
			return false;
		}
		Role r = (Role) object;
		return changeRoleState(role, r, b);

	}

	private boolean changeRoleState(String roleName, Role role, boolean b) {
		role.setState(b);
		if (b == true) {
			String[] rToDis = role.getRolesToDisable();
			if (rToDis != null) {
				for (int i = 0; i < rToDis.length; i++) {
					String roleToDis = rToDis[i];
					changeRoleState(roleToDis, false);
				}
			}
		}else if (ROLE_MENU.equals(roleName)) {
			Role done = getRole(ROLE_DONE);
			if ( done != null ) { // I (pfeffer) added this because it caused an NullPointerException on PPC2003. I guess that "if (ROLE_MENU.equals(..." is not needed any more - old code? 
				String[] rToDis = done.getRolesToDisable();
				if (rToDis != null) {
					for (int i = 0; i < rToDis.length; i++) {
						String roleToDis = rToDis[i];
						changeRoleState(roleToDis, false);
					}
				}
			}
		}
			
		setStateOfIcons();

		boolean action = checkRolesForAction(roleName, b);
//		if (action) {
//
//		}
		if (getStateOfRole(ROLE_WORKING)) {
			changeRoleState(ROLE_WORKING, false);
		}
		movingMap.repaintNow();

		return action;
	}

	private boolean checkRolesForAction(String role, boolean state) {
		if (role == null) {
			return false;
		}
		if (ROLE_SHOW_MAP.equals(role)) {
			changeRoleState(ROLE_WORKING, true);
			if (state) {
				return movingMap.handleCommand(SHOW_MAP);
			} else
				return movingMap.handleCommand(HIDE_MAP);
		}
		if (ROLE_SHOW_CACHES.equals(role)) {
			changeRoleState(ROLE_WORKING, true);
			if (state) {
				return movingMap.handleCommand(SHOW_CACHES);
			} else
				return movingMap.handleCommand(HIDE_CACHES);
		}

		if (ROLE_FILL_WHITE.equals(role)) {
			changeRoleState(ROLE_WORKING, true);
			if (state) {
				return movingMap.handleCommand(FILL_MAP);
			} else
				return movingMap.handleCommand(NO_FILL_MAP);
		}

		if (ROLE_ZOOM_MANUALLY.equals(role)) {
			changeRoleState(ROLE_WORKING, true);
			if (state) {
				movingMap.setZoomingMode(true);
			} else
				movingMap.setZoomingMode(false);
			return true;
		}

		if (ROLE_MENU.equals(role)) {
			if (state) {
				movingMap.setPaintPosDestLine(false);
			} else
				movingMap.setPaintPosDestLine(true);
			return false;
		}
		return false;
	}

	// private void checkStateOfIcon(MovingMapControlItem item, AniImage ani) {
	// if (MOVE_TO_DEST.equals(item.getActionCommand())) {
	// if (movingMap.getDestination() != null) {
	// ani.properties &= ~mImage.IsNotHot;
	// } else {
	// ani.properties |= mImage.IsNotHot;
	// }
	// }
	// if (MOVE_TO_CENTER.equals(item.getActionCommand())) {
	// if (Global.getPref().getCurCentrePt().isValid())
	// ani.properties &= ~mImage.IsNotHot;
	// else {
	// ani.properties |= mImage.IsNotHot;
	//
	// }
	// }}

	public void updateContent(String contentName, String text) {
		if (contentName == null) {
			return;
		}
		updateContent(contentName, text, -1);

	}

	public void updateContent(String contentName, String text, int property) {
		for (int i = 0; i < buttons.size(); i++) {
			MovingMapControlItem item = (MovingMapControlItem) buttons.get(i);

			if ((item.xProperties & MovingMapControlItem.IS_ICON_WITH_TEXT) != 0) {
				if (contentName.equals(item.getContent())) {
					item.setText(item.getText()+text);
				}
			}
			if ((item.xProperties & MovingMapControlItem.IS_ICON_WITH_FRONTLINE) != 0) {
				if (contentName.equals(item.getContent())) {
					item.setAdditionalProperty(property);
				}
			}
		}

	}

	public void updateFormSize(int w, int h) {

		// adding bottom and top
		for (int i = 0; i < buttons.size(); i++) {
			MovingMapControlItem item = (MovingMapControlItem) buttons.get(i);

			if ((item.xProperties & MovingMapControlItem.IS_ICON_WITH_COMMAND) == 0
					&& (item.xProperties & MovingMapControlItem.IS_ICON_WITH_TEXT) == 0) {
			}

			AniImage ani = item.getImage();

			int xpos = 0;
			int ypos = 0;

			if ((item.xProperties & MovingMapControlItem.DISPLAY_FROM_TOP) != 0) {
				ypos = item.getyPos();
			} else
				ypos = h - item.getyPos();

			if ((item.xProperties & MovingMapControlItem.DISPLAY_FROM_LEFT) != 0) {

				xpos = item.getxPos();
			} else
				xpos = w - item.getxPos();

			ani.setLocation(xpos, ypos);

		}

	}

	public boolean imageClicked(AniImage which) {
		int timenow = Vm.getTimeStamp();

		// avoid double clicks
		if (timenow < 40 + lastTime) {
			return false;
		}
		
		boolean result = handleImageClicked(which);
		
		lastTime = Vm.getTimeStamp();
		return result;
	}

	private boolean handleImageClicked(AniImage which) {
	
		for (int i = 0; i < buttons.size(); i++) {
			MovingMapControlItem item = (MovingMapControlItem) buttons.get(i);
			AniImage ani = item.getImage();
			if (which == ani) {

				int command = item.getActionCommand();
				if (changeStateOfRole == command) {
					boolean val = changeRoleState(item.getRoleToChange());
					if (val) {
						changeRoleState(ROLE_MENU, false);
					}
					setStateOfIcons();
					movingMap.repaintNow();
					return val;
				}
				changeRoleState(ROLE_WORKING, true);
				boolean handleCommand = movingMap.handleCommand(command);
				if (handleCommand) {
					changeRoleState(ROLE_MENU, false);
				}
				changeRoleState(ROLE_WORKING, false);
				return true;
			}
		}
		changeRoleState(ROLE_MENU, false);
		return true;
	}

	public boolean handleCommand(int actionCommand) {
		if (CLOSE == actionCommand) {
			return changeRoleState(ROLE_MENU, false);
		}
		return false;
	}

	public boolean imageBeginDragged(AniImage which, Point pos) {
		boolean contains = containsImage(which);

		if (contains) {
			imageClicked(which);
			// if there was the right image, return true to say the event is handled
			return true;
		}
		return false;
	}

	private boolean containsImage(AniImage which) {
		if (which == null) {
			return false;
		}
		for (int i = 0; i < buttons.size(); i++) {
			MovingMapControlItem item = (MovingMapControlItem) buttons.get(i);
			AniImage ani = item.getImage();
			if (which == ani) {
				return true;
			}
		}
		return false;
	}

	public static class Role {
		boolean state = false;
		String rolesToDisable[] = null;

		public void setRolesToDisable(String[] rolesToDisable) {
			this.rolesToDisable = rolesToDisable;
		}

		public void setState(boolean state) {
			this.state = state;
		}

		public boolean getState() {
			return state;
		}

		public String[] getRolesToDisable() {
			return rolesToDisable;
		}
	}
}
