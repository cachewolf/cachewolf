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

import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.graphics.AniImage;

public class MovingMapControlItemButton extends MovingMapControlItem {

	private int command;
	private AniImageGrayScaled aniImage;

	public MovingMapControlItemButton(String source, String iconSrc, int actionCommand, int alpha) {
		Image image = MovingMapControlItem.createImage(source, iconSrc, alpha);
		aniImage = new AniImageGrayScaled(image);
		aniImage.freeSource();
		command = actionCommand;
		aniImage.properties |= mImage.AlwaysOnTop;
		xProperties |= IS_ICON_WITH_COMMAND;
	}

	public int getActionCommand() {
		return command;
	}

	public int getWidth() {
		return aniImage.getWidth();
	}

	public int getHeight() {
		return aniImage.getHeight();
	}

	public AniImage getImage() {
		return aniImage;
	}

	public void setText(String text) {

	}

}
