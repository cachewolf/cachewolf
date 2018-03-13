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
package CacheWolf.model;

import ewe.util.Vector;

public class DefaultListModel {

    public Vector allItems;

    public Vector shownItems = new Vector();
    public int sortCriteria;

    public DefaultListModel() {
        super();
        allItems = new Vector();
        shownItems = new Vector();
    }

    /**
     * Creates the list of objects to be shown in the List. With this methode You can hide some objects.
     * Standardimplementation is to show all objects anytime.
     */
    public void createShowSet() {
        shownItems.clear();
        shownItems.addAll(allItems);
    }

    /**
     * Adds an Object to this model to last position.
     *
     * @param o
     */
    public void add(Object o) {
        allItems.add(o);
    }

    /**
     * Returns the number of elements in the showset of this model
     */
    public int size() {
        return shownItems.size();
    }

    /**
     * Returns the Nth item in the showset of this model
     */
    public Object get(int n) {
        return shownItems.get(n);
    }
}