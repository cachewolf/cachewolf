/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
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
import CacheWolf.navi.touchControls.MovingMapControls.Role;
import ewe.fx.Image;
import ewe.graphics.AniImage;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.mString;

/**
 * class which represents a item which can be displayed on the map
 * 
 * @author Hälmchen
 */
public abstract class MovingMapControlItem {

	static public final int DISPLAY_FROM_TOP = 0x10;
	static public final int DISPLAY_FROM_BOTTOM = 0x20;
	static public final int DISPLAY_FROM_RIGHT = 0x40;
	static public final int DISPLAY_FROM_LEFT = 0x80;

	static public final int IS_PLACE_HOLDER = 0x2000;
	static public final int IS_ICON_WITH_COMMAND = 0x4000;
	static public final int IS_ICON_WITH_TEXT = 0x8000;
	static public final int IS_ICON_WITH_FRONTLINE = 0x10000;

	static public final int ICON_TEXT_LEFT = 0x20000;
	static public final int ICON_TEXT_HORIZONTAL_CENTER = 0x40000;
	static public final int ICON_TEXT_RIGHT = 0x80000;

	static public final int ICON_TEXT_TOP = 0x100000;
	static public final int ICON_TEXT_VERTICAL_CENTER = 0x200000;
	static public final int ICON_TEXT_BOTTOM = 0x400000;

	public int xProperties = 0x0;

	private String helpText = null;
	private int xPos;
	private int yPos;
	private Hashtable roles = new Hashtable();
	private String role;

	public static Image createImage(String source, String iconSrc, int alpha) {
		Image image = new Image(source);

		int imageW = image.getWidth();
		int imageH = image.getHeight();
		Image icon = null;
		if (iconSrc != null) {
			icon = new Image(iconSrc);
			int iconW = icon.getWidth();
			int iconH = icon.getHeight();
			if (iconH <= imageH && iconW <= imageW) {
				int offsetx = (imageW - iconW) / 2;
				int offsety = (imageH - iconH) / 2;

				// not so nice solution to have the icon at the left side
				if (offsetx > offsety) {
					offsetx = offsety;
				}

				int[] iconPixels = icon.getPixels(null, 0, 0, 0, iconW, iconH, 0);
				int[] imagePixels = image.getPixels(null, 0, 0, 0, imageW, imageH, 0);

				for (int y = 0; y < imageH; y++) {
					for (int x = 0; x < imageW; x++) {

						if (y >= offsety && x >= offsetx && y < offsety + iconH && x < offsetx + iconW) {

							int iconx = x - offsetx;
							int icony = y - offsety;

							int index = y * imageW + x;
							int iconIndex = icony * iconW + iconx;
							int alphaval = (iconPixels[iconIndex] >> 24) & 0xff;

							if (alphaval > 127) {
								imagePixels[index] = iconPixels[iconIndex];
							}

						}
					}
				}

				image.setPixels(imagePixels, 0, 0, 0, imageW, imageH, 0);

			}
			else
				Global.pref.log("icon " + iconSrc + " is bigger than " + source + "! Icon not loaded", null);

		}

		if (alpha >= 0 && alpha < 256) {
			alpha = alpha << 24;

			int[] imageBits = image.getPixels(null, 0, 0, 0, image.getWidth(), image.getHeight(), 0);
			for (int i = 0; i < imageBits.length; i++) {
				if (imageBits[i] != 0) {
					imageBits[i] &= 0xffffff;
					imageBits[i] |= alpha;
				}

			}
			image.setPixels(imageBits, 0, 0, 0, image.getWidth(), image.getHeight(), 0);
			image.enableAlpha();
		}

		return image;
	}

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract AniImage getImage();

	public abstract void setText(String text);

	public int getActionCommand() {
		return -1;
	}

	public String getContent() {
		return null;
	}

	public String getText() {
		return null;
	}

	public String getHelp() {

		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public void setAdditionalProperty(int prop) {

	}

	public void setPosition(int xpos, int ypos) {
		this.xPos = xpos;
		this.yPos = ypos;
	}

	public void addXtraProperties(int xProps) {
		xProperties |= xProps;

	}

	public void setVisibilityRole(String visibility) {
		String[] parts = mString.split(visibility, '+');

		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.startsWith("!")) {
				roles.put(part.substring(1), Boolean.FALSE);
			}
			else {
				roles.put(part, Boolean.TRUE);
			}
		}
	}

	public boolean isVisible(Hashtable overallRoles) {

		if (roles.size() == 0) {
			return false;
		}

		Enumeration keys = roles.keys();

		while (keys.hasMoreElements()) {
			String nextKey = (String) keys.nextElement();

			Boolean thisElement = (Boolean) roles.get(nextKey);
			if (!overallRoles.containsKey(nextKey)) {
				Global.pref.log("Lookup role " + nextKey + " not possible", null);
				return false;
			}

			Role overallElement = (Role) overallRoles.get(nextKey);
			if (thisElement.booleanValue() != overallElement.getState()) {
				return false;
			}

		}

		return true;

	}

	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public String getRoleToChange() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
