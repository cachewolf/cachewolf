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
package CacheWolf.navi;

import CacheWolf.database.CWPoint;

public class MapSymbol extends MapImage {
    String name;
    String filename;
    CWPoint where;
    Object mapObject;

    public MapSymbol(String name, String filename, CWPoint where) {
        this.name = name;
        this.filename = filename;
        this.where = where;
        loadImage();
    }

    public MapSymbol(String name, Object mapObject, String filename, CWPoint where) {
        this.name = name;
        this.filename = filename;
        this.where = where;
        this.mapObject = mapObject;
        loadImage();
    }

    public MapSymbol(String name, Object mapObject, ewe.fx.Image fromIm, CWPoint where) {
        this.name = name;
        this.where = where;
        this.mapObject = mapObject;
        setImage(fromIm);
    }

    private void loadImage() {
        if (filename != null) {
            setImage(new ewe.fx.Image(filename), 0);
            freeSource();
        }
    }
}
