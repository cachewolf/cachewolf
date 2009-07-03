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
	
	public CacheImages(){ // Public Constructor
	}
	
	/** Lazy initialization of the vector: It is created only when needed. If it is not accessed,
	 * it will stay <code>null</code>.*/
	private Vector vector=null;
	private Vector getVector(){
		if (this.vector==null) {
			vector = new Vector(10);
		}
		return this.vector;
	}
	
	/**
	 * Adds an ImageInfo object to the list of images. The object is always appended in the 
	 * last position.
	 * @param img ImageInfo object to add.
	 */
	public void add(ImageInfo img) {
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
}