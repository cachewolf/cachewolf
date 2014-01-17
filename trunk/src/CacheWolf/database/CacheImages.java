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
package CacheWolf.database;

import ewe.util.Vector;

/**
 * This class holds the image information of a cache.
 * 
 * @author torsti
 * 
 */
public class CacheImages {

    private int initialSize = 10;
    /**
     * Lazy initialization of the vector: It is created only when needed. If it is not accessed,
     * it will stay <code>null</code>.
     */
    private Vector vector = null;
    /** Images that should display in the image panel */
    private CacheImages display = null;

    public CacheImages() { // Public constructor
    }

    public CacheImages(int initialSize) {
	if (initialSize < 0) {
	    throw new IllegalArgumentException("Initial size for CacheImage must be > 0. Value: " + String.valueOf(initialSize));
	}
	this.initialSize = initialSize;
    }

    private Vector getVector() {
	if (this.vector == null) {
	    vector = new Vector(this.initialSize);
	}
	return this.vector;
    }

    /**
     * Adds an CacheImage object to the list of images. The object is always appended in the
     * last position.
     * 
     * @param img
     *            CacheImage object to add.
     */
    public void add(CacheImage img) {
	display = null; // New Image? Force display to get reevaluated
	getVector().add(img);
    }

    /**
     * Gets the CacheImage object at the specified position.
     * 
     * @param idx
     *            Index of object to retrieve.
     * @return CacheImage object
     */
    public CacheImage get(int idx) {
	return (CacheImage) getVector().get(idx);
    }

    /**
     * Removes all image information.
     */
    public void clear() {
	if (this.vector != null) {
	    display = null;
	    this.vector.clear();
	}
    }

    /**
     * Returns the number of CacheImage objects in the collection.
     * 
     * @return Number
     */
    public int size() {
	if (this.vector == null) {
	    return 0;
	} else {
	    return this.vector.size();
	}
    }

    /**
     * Fills the <code>display</code> collection with the images that really should display
     * in the image panel.
     * 
     * @param prefix
     *            The prefix which is used to name default image titles
     */
    private void checkForDisplayImages(String prefix) {
	if (this.size() > 1) {
	    display = new CacheImages(this.size());
	    // Loop over every image
	    for (int i = 0; i < this.size(); i++) {
		boolean shouldDisplay = true;
		CacheImage currImg = this.get(i);
		// Now check against every other image
		for (int j = 0; j < this.size(); j++) {
		    if (i == j)
			continue; // Except same image
		    CacheImage testImg = this.get(j);
		    // Are the filenames the same?
		    if (currImg.getFilename().toLowerCase().equals(testImg.getFilename().toLowerCase())) {
			// Check if other title is better than current one
			if (currImg.getTitle().startsWith(prefix) && !testImg.getTitle().startsWith(prefix)) {
			    // If yes: Don't show the image
			    shouldDisplay = false;
			}
		    }
		}
		if (shouldDisplay)
		    display.add(currImg);
	    }
	}
    }

    /**
     * Returns a CacheImage collection of the images that should be displayed in the
     * image panel. An image should <b>not</b> be present in this collection if its title is
     * starting with the indicated prefix and if there is another image referring to the same file but
     * with a different title.<br>
     * Normally, the results are cached and don't need to be evaluated again. If they should
     * be reevaluated for whatever reason, then pass <code>true</code> as parameter.
     * 
     * @param prefix
     *            The prefix which is used to name default image titles
     * @param forceEvaluation
     *            When <code>true</code> the images that should display are
     *            reexamined.
     * @return The CacheImage collection of images.
     */
    public CacheImages getDisplayImages(String prefix, boolean forceEvaluation) {
	if (display == null || forceEvaluation)
	    this.checkForDisplayImages(prefix);
	if (display == null) {
	    return this;
	} else {
	    return display;
	}
    }

    /**
     * Returns a CacheImage collection of the images that should be displayed in the
     * image panel. An image should <b>not</b> be present in this collection if its title is
     * equivalent to its filename and if there is another image referring to the same file but
     * with a different title.
     * 
     * @param prefix
     *            The prefix which is used to name default image titles
     * @return The CacheImage collection of images.
     */
    public CacheImages getDisplayImages(String prefix) {
	return this.getDisplayImages(prefix, false);
    }

}
