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
package CacheWolf.view.pda;

import ewe.fx.Color;
import ewe.fx.Font;
import ewe.ui.CellConstants;
import ewe.ui.mButton;

public class PDAListButton extends mButton {

    public String fromText;
    public String toText;
    public boolean toLogged;
    public boolean fromLogged;

    public PDAListButton(String newText, String newAction) {
        super(newText);
        action = newAction;
        createButtonObject();

        backGround = Color.White;
        foreGround = Color.Black;

        font = new Font(getFont().getName(), Font.BOLD, 40);
        anchor = CellConstants.WEST;
        textPosition = 2;
    }

    protected void createButtonObject() {
        buttonObject = new PDAListButtonObject(this);
    }

    public void make(boolean paramBoolean) {
        if (this.buttonObject != null)
            return;
        createButtonObject();
    }

}
