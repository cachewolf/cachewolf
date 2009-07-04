/**
 * 
 */
package CacheWolf;

import ewe.util.Vector;

/**
 * This class holds the image information of a cache. 
 * @author torsti
 *
 */
public class CacheImages {
	
	private int initialSize=10;
	/** Lazy initialization of the vector: It is created only when needed. If it is not accessed,
	 * it will stay <code>null</code>.*/
	private Vector vector=null;
	/** Images that should display in the image panel */
	private CacheImages display=null;
	
	public CacheImages(){ // Public constructor
	}

	public CacheImages(int initialSize) {
		if (initialSize<0) {
			throw new IllegalArgumentException("Initial size for CacheImage must be > 0. Value: "+String.valueOf(initialSize));
		}
		this.initialSize = initialSize;
	}
	private Vector getVector(){
		if (this.vector==null) {
			vector = new Vector(this.initialSize);
		}
		return this.vector;
	}
	
	/**
	 * Adds an ImageInfo object to the list of images. The object is always appended in the 
	 * last position.
	 * @param img ImageInfo object to add.
	 */
	public void add(ImageInfo img) {
		display = null;  // New Image? Force display to get reevaluated 
        getVector().add(img);
    }
	
	/**
	 * Gets the ImageInfo object at the specified position.
	 * @param idx Index of object to retrieve.
	 * @return ImageInfo object
	 */
	public ImageInfo get(int idx) {
		return (ImageInfo) getVector().get(idx);
	}
	
	/**
	 * Removes all image information.
	 */
	public void clear() {
		if (this.vector!=null) {
			display = null; 
			this.vector.clear();
		}
	}
	
	/**
	 * Returns the number of ImageInfo objects in the collection.
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
	 */
	private void checkForDisplayImages() {
		if (this.size()>1) {
			display = new CacheImages(this.size());
			// Loop over every image
			for (int i=0; i<this.size(); i++) {
				boolean shouldDisplay = true;
				ImageInfo currImg = this.get(i);
				// Now check against every other image
				for (int j=0; j<this.size(); j++) {
					if (i==j) continue; // Except same image
					ImageInfo testImg = this.get(j);
					// Are the filenames the same?
					if (currImg.getFilename().equals(testImg.getFilename())) {
						// Check if other title is better than current one
						if (currImg.getFilename().startsWith(currImg.getTitle()) &&
							!testImg.getFilename().startsWith(testImg.getTitle())) {
							// If yes: Don't show the image
							shouldDisplay = false;
						}
					}
				}
				if (shouldDisplay) display.add(currImg);
			}
		}
	}
	
	/**
	 * Returns a CacheImage collection of the images that should be displayed in the 
	 * image panel. An image should <b>not</b> be present in this collection if its title is 
	 * equivalent to its filename and if there is another image referring to the same file but
	 * with a different title.<br>
	 * Normally, the results are cached and don't need to be evaluated again. If they should
	 * be reevaluated for whatever reason, then pass <code>true</code> as parameter.  
	 * @param forceEvaluation When <code>true</code> the images that should display are
	 * reexamined.
	 * @return The CacheImage collection of images. 
	 */
	public CacheImages getDisplayImages(boolean forceEvaluation) {
		if (display == null || forceEvaluation) this.checkForDisplayImages();
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
	 * @return The CacheImage collection of images. 
	 */
	public CacheImages getDisplayImages() {
		return this.getDisplayImages(false);
	}
	
}