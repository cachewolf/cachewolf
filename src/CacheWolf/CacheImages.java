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
package CacheWolf;

import CacheWolf.utils.FileBugfix;
import ewe.io.File;
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
	 * Adds an ImageInfo object to the list of images. The object is always appended in the
	 * last position.
	 * 
	 * @param img
	 *            ImageInfo object to add.
	 */
	public void add(ImageInfo img) {
		display = null; // New Image? Force display to get reevaluated
		getVector().add(img);
	}

	/**
	 * Gets the ImageInfo object at the specified position.
	 * 
	 * @param idx
	 *            Index of object to retrieve.
	 * @return ImageInfo object
	 */
	public ImageInfo get(int idx) {
		return (ImageInfo) getVector().get(idx);
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
	 * Returns the number of ImageInfo objects in the collection.
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
				ImageInfo currImg = this.get(i);
				// Now check against every other image
				for (int j = 0; j < this.size(); j++) {
					if (i == j)
						continue; // Except same image
					ImageInfo testImg = this.get(j);
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

	/**
	 * Checks if a image of a given URL needs to be spidered. It does <b>not</b> need to be spidered
	 * if the following conditions meet:
	 * <ul>
	 * <li>The url is from <code>http://img.geocaching.com/cache/</code> or <code>http://img.groundspeak.com/cache/</code>. (Reason: Images at these places don't change - if images change, they get a new url.)</li>
	 * <li>An image with the given URL is among the images of the caches image object.</li>
	 * <li>The intended file name is the same.</li>
	 * <li>The image is present in the file system.</li>
	 * </ul>
	 * If no spidering is needed, then the <code>ImageInfo</code> object of the equivalent image is
	 * returned, otherwise (when spidering is needed) <code>null</code> is returned.
	 * 
	 * @param pNewUrl
	 *            URL to check
	 * @return ImageInfo object
	 */
	public ImageInfo needsSpidering(String pNewUrl, String pFilename) {
		String newUrl = CacheImages.optimizeLink(pNewUrl);
		ImageInfo result = null;
		if (this.size() > 0 && (newUrl.startsWith("http://img.geocaching.com/cache/"))) {
			for (int i = 0; i < this.size(); i++) {
				ImageInfo img = this.get(i);
				if (CacheImages.optimizeLink(img.getURL()).equals(newUrl) && img.getFilename().equals(pFilename)) {
					String location = Global.getProfile().dataDir + pFilename;
					if ((new FileBugfix(location)).exists()) {
						result = img;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Checks if an image with a certain filename is present in the collection of the caches
	 * images.
	 * 
	 * @param filename
	 *            Filename to check
	 * @return <code>true</code> if there is such a file, <code>false</code> if not.
	 */
	private boolean hasFile(String filename) {
		boolean result = false;
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getFilename().toUpperCase().equals(filename.toUpperCase())) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Deletes images that are no longer needed. It takes two CacheImage objects as arguments, one
	 * as a collection of images before an update, the other after. Then every file in the old
	 * collection is checked if it is also present in the new collection. If not, the file is deleted.
	 * Note that the <i>content</i> of the files is irrelevant. It is only important to remove files
	 * - regardles of their contents - if they are no longer referenced after a cache update.
	 * 
	 * @param oldImages
	 *            Set of images before update
	 * @param newImages
	 *            Set of images after update
	 */
	public static void cleanupOldImages(CacheImages oldImages, CacheImages newImages) {
		// Loop over every image in the old collection
		for (int i = 0; i < oldImages.size(); i++) {
			// Check if image file is present in new collection
			String obsoleteFilename = oldImages.get(i).getFilename();
			if (!newImages.hasFile(obsoleteFilename)) {
				String location = Global.getProfile().dataDir + obsoleteFilename;
				File tmpFile = new FileBugfix(location);
				if (tmpFile.exists() && tmpFile.canWrite()) {
					Global.getPref().log("Image not longer needed. Deleting: " + obsoleteFilename);
					tmpFile.delete();
				}
			}
		}
	}

	/**
	 * Takes an image url and does some optimization: As img.groundspeak.com is the same as
	 * img.geocaching.com and the former is sometimes used by owners in the cache listing html,
	 * this method replaces img.groundspeak.com by img.geocaching.com, in order to be able to
	 * identify identical pictures as such.
	 * 
	 * @param pUrl
	 *            URL to modify
	 * @return Same URL, eventually modified
	 */
	public static String optimizeLink(String pUrl) {
		String url = pUrl;
		// img.groundspeak.com is same as img.geocaching.com, so replace it
		if (url.toLowerCase().startsWith("http://img.groundspeak.com/")) {
			url = "http://img.geocaching.com/" + url.substring(27);
		}
		return url;
	}

}
