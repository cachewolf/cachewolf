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

import CacheWolf.database.CacheType;
import CacheWolf.utils.FileBugfix;
import ewe.fx.Graphics;
import ewe.fx.IImage;
import ewe.fx.IconAndText;
import ewe.fx.Image;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.ui.ControlConstants;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.PullDownMenu;
import ewe.ui.mButton;

/**
 * hold preloaded versions of GUI images in a single place
 * 
 * Do not instantiate this class, only use it in a static way.
 */

public final class GuiImageBroker {
    static final String basedir = FileBase.getProgramDirectory() + "/symbols/";
    static String extension;

    public static Image found = new Image("found.png");
    public static Image disabled = new Image("disabled.png");
    public static Image archived = new Image("archived.png");
    public static Image solved = new Image("solved.png");
    public static Image bonus = new Image("bonus.png");
    public static Image owned = new Image("owned.png");
    public static Image dnf = new Image("dnf.png");

    private GuiImageBroker() {
    }

    /**
     * Replaces the build-in symbols by images stored in /symbols: If the sub directory symbols exists in CW-directory *.png-files are read in and roughly checked for validity (some names must be convertible to integers between 0 and 21). For every
     * valid file x.png the corresponding typeImages[x] is replaced by the image in x.png. Images are NOT checked for size etc.
     */
    public static void customizedSymbols() {
	setExtension();
	final FileBugfix dir = new FileBugfix(basedir);
	if (dir.isDirectory()) {
	    int id;
	    boolean size = false;
	    String name = "";
	    String[] pngFiles;
	    pngFiles = dir.list("*.png", FileBase.LIST_FILES_ONLY);
	    Global.pref.log("Nr. of own symbols (png-files) : " + pngFiles.length);
	    for (int i = 0; i < pngFiles.length; i++) {
		name = pngFiles[i].substring(0, pngFiles[i].length() - 4).toLowerCase();
		if (name.endsWith("size")) {
		    size = true;
		    name = name.substring(0, name.length() - 4);
		} else {
		    if (name.equals("disabled")) {
			disabled = new Image(basedir + pngFiles[i]);
		    }
		    if (name.equals("archived")) {
			archived = new Image(basedir + pngFiles[i]);
		    }
		    if (name.equals("solved")) {
			solved = new Image(basedir + pngFiles[i]);
		    }
		    if (name.equals("bonus")) {
			bonus = new Image(basedir + pngFiles[i]);
		    }
		    if (name.equals("owned")) {
			owned = new Image(basedir + pngFiles[i]);
		    }
		    if (name.equals("dnf")) {
			dnf = new Image(basedir + pngFiles[i]);
		    }
		    if (name.equals("found")) {
			found = new Image(basedir + pngFiles[i]);
		    }
		}
		try {
		    id = Integer.parseInt(name);
		} catch (final Exception E) {
		    id = -1; // filename invalid for symbols
		}
		if (0 <= id && id <= CacheType.maxCWCType) {
		    final String s = basedir + pngFiles[i];
		    Global.pref.log("own symbol: " + (i + 1) + " = " + pngFiles[i]);
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

    /**
     * should be only called once, before first access of extension
     */
    private static void setExtension() {
	if (extension == null) {
	    if (Global.pref.useBigIcons)
		extension = "_vga.png";
	    else
		extension = ".png";
	}
    }

    private static String getImageName(String icon) {
	String in;
	File f = new File(basedir + icon + extension);
	if (f.exists()) {
	    in = f.getAbsolutePath();
	    Global.pref.log("using image " + in);
	} else {
	    in = icon + extension;
	}
	return in;
    }

    private static String getText(String text) {
	if (!Global.pref.useText) {
	    text = "";
	}
	return text;
    }

    public static Image getImage(String icon) {
	if (Global.pref.useIcons)
	    return new Image(getImageName(icon));
	else
	    // simply using a small transparent image
	    //return new Image(getImageName("leer"));
	    return null;
    }

    public static IconAndText getIconAndText(String text, String icon) {
	return new IconAndText(getImage(icon), getText(text), null); //Gui.makeHot(text)
    }

    public static IImage makeImageForButton(mButton btn, String text, String icon) {
	if (btn.image != null) {
	    if (btn.image instanceof IconAndText) {
		return getIconAndText(text, icon);
	    } else {
		return getImage(icon);
	    }
	}
	return null;
    }

    public static void setButtonText(mButton btn, String text) {
	if (btn.image == null || (btn.image != null && !(btn.image instanceof IconAndText))) {
	    btn.setText(text);
	} else {
	    IconAndText iat = (IconAndText) btn.image;
	    // hack for force update text of IconAndText
	    // to change, there must be a change (of mTA)
	    int mTA = iat.multiLineTextAlignment;
	    if (mTA == 0)
		mTA = Graphics.CENTER;
	    else
		mTA = 0;
	    iat.text = text;
	    iat.changeTextPosition(iat.textPosition, mTA);
	    // won't change back to original at the moment
	    btn.repaint(); // still have to do a repaint after the change
	}
    }

    public static void setButtonIconAndText(mButton btn, String text, IImage iat) {
	if (btn.image == null || (btn.image != null && !(btn.image instanceof IconAndText)) || iat == null) {
	    // text and image separate
	    btn.setText(text);
	    if (iat != null) {
		btn.image = iat;
	    }
	} else {
	    // iat is IconAndText (and text already changed) 
	    btn.image = iat;
	}
	btn.repaint(); // ?still have to do a repaint after the change
    }

    public static mButton getButton(String text, String icon) {
	mButton btn;
	if (Global.pref.useIcons) {
	    if (Global.pref.leftIcons) {
		btn = new mButton(getText(text));
		// Graphics.Up, Graphics.Down, Graphics.Right, Graphics.Left // über, unter, rechts, links vom Icon
		btn.textPosition = Graphics.Right;
		btn.image = getImage(icon);
	    } else {
		// Icons in the middle of the Button (as IconAndText)
		btn = new mButton(getText(text), getImageName(icon), null);
	    }
	} else {
	    if (text.length() == 0) {
		btn = new mButton("", getImageName(icon), null);
	    } else {
		btn = new mButton(getText(text)); //, "leer", null
	    }
	}
	// btn.backGround = Color.LightGreen;
	return btn;
    }

    public static MenuItem getMenuItem(String text, String icon) {
	MenuItem mi = new MenuItem().iconize(getText(text), getImage(icon), true);
	return mi;
    }

    public static PullDownMenu getPullDownMenu(String text, String icon, MenuItem[] menuItems) {
	PullDownMenu pdm;
	if (Global.pref.leftIcons) {
	    pdm = new PullDownMenu(getText(text), new Menu(menuItems, null));
	    pdm.image = getImage(icon);
	    pdm.textPosition = Graphics.Right; // rechts vom Icon
	} else {
	    pdm = new PullDownMenu("", new Menu(menuItems, null));
	    pdm.image = GuiImageBroker.getIconAndText(text, icon);
	}
	pdm.modify(0, ControlConstants.DrawFlat | ControlConstants.MakeMenuAtLeastAsWide | ControlConstants.NoFocus);
	pdm.setBorder(ewe.ui.UIConstants.BDR_OUTLINE, 1);
	//pdm.backGround = Color.LightGreen;
	return pdm;
    }
}
