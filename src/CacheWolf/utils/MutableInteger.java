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
package CacheWolf.utils;

/**
 * Mutable Integer class, in case we need several integer Values
 * which could be reused in order to decrease object creation.
 */
public class MutableInteger {

    private int value;

    /**
     * Creates a MutableInteger Object with value -1
     */
    public MutableInteger() {
        value = -1;
    }

    /**
     * Creates a MutableInteger Object with specified value
     *
     * @param initialValue You have to guess.
     */
    public MutableInteger(final int initialValue) {
        value = initialValue;
    }

    /**
     * Gets the value
     *
     * @return value
     */
    public int getInt() {
        return value;
    }

    /**
     * Sets the value
     *
     * @param newValue value
     */
    public void setInt(final int newValue) {
        value = newValue;
    }

}
