/**
 * 
 */
package CacheWolf;

import ewe.util.Vector;

public class CacheImages {
	
	public CacheImages(){ // Public Constructor
	}
	
	private Vector vector=null;
	private Vector getVector(){
		if (this.vector==null) {
			vector = new Vector(10);
		}
		return this.vector;
	}
	
	public void add(ImageInfo img) {
        getVector().add(img);
    }
	
	public ImageInfo get(int idx) {
		return (ImageInfo) getVector().get(idx);
	}
	
	public void clear() {
		if (this.vector!=null) {
			this.vector.clear();
		}
	}
	public int size() {
		if (this.vector == null) {
			return 0;
		} else {
			return this.vector.size();
		}
	}
}