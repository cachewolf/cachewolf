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
package CacheWolf;

import CacheWolf.utils.FileBugfix;
import ewe.fx.Image;
import ewe.io.FileBase;

/**
 * hold preloaded versions of GUI images in a single place
 * 
 * Do not instantiate this class, only use it in a static way.
 */

public final class GuiImageBroker {
	public static Image found = new Image("found.png");
	public static Image disabled = new Image("disabled.png"); // available
	public static Image archived = new Image("archived.png");
	public static Image solved = new Image("solved.png");
	public static Image bonus = new Image("bonus.png");
	public static Image owned = new Image("owned.png");
	public static Image dnf = new Image("dnf.png");

	private GuiImageBroker() {
		// Noting to do
	}

	/**
	 * Replaces the build-in symbols by images stored in /symbols:
	 * If the sub directory symbols exists in CW-directory *.png-files
	 * are read in and roughly checked for validity (names must be
	 * convertible to integers between 0 and 21).
	 * For every valid file x.png the corresponding typeImages[x] is
	 * replaced by the image in x.png.
	 * Images are NOT checked for size etc.
	 */
	public static void customizedSymbols() {
		final String sdir = "/symbols/";
		final FileBugfix dir = new FileBugfix(FileBase.getProgramDirectory() + sdir);
		if (dir.isDirectory()) {
			int id;
			boolean size = false;
			String name = "";
			String[] pngFiles;
			pngFiles = dir.list("*.png", FileBase.LIST_FILES_ONLY);
			Global.getPref().log("Nr. of own symbols (png-files) : " + pngFiles.length);
			for (int i = 0; i < pngFiles.length; i++) {
				name = pngFiles[i].substring(0, pngFiles[i].length() - 4).toLowerCase();
				if (name.endsWith("size")) {
					size = true;
					name = name.substring(0, name.length() - 4);
				} else {
					if (name.equals("disabled")) {
						disabled = new Image(FileBase.getProgramDirectory() + sdir + pngFiles[i]);
					}
					if (name.equals("archived")) {
						archived = new Image(FileBase.getProgramDirectory() + sdir + pngFiles[i]);
					}
					if (name.equals("solved")) {
						solved = new Image(FileBase.getProgramDirectory() + sdir + pngFiles[i]);
					}
					if (name.equals("bonus")) {
						bonus = new Image(FileBase.getProgramDirectory() + sdir + pngFiles[i]);
					}
					if (name.equals("owned")) {
						owned = new Image(FileBase.getProgramDirectory() + sdir + pngFiles[i]);
					}
					if (name.equals("dnf")) {
						dnf = new Image(FileBase.getProgramDirectory() + sdir + pngFiles[i]);
					}
					if (name.equals("found")) {
						found = new Image(FileBase.getProgramDirectory() + sdir + pngFiles[i]);
					}
				}
				try {
					id = Integer.parseInt(name);
				} catch (final Exception E) {
					id = -1; // filename invalid for symbols
				}
				if (0 <= id && id <= CacheType.maxCWCType) {
					final String s = FileBase.getProgramDirectory() + sdir + pngFiles[i];
					Global.getPref().log("own symbol: " + (i + 1) + " = " + pngFiles[i]);
					if (size) {
						CacheType.setMapImage((byte) id, new Image(s));
						size = false;

					} else {
						CacheType.setTypeImage((byte) id, new Image(s));
					}
				}
			}
		}
	}

}
