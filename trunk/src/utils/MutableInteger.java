package utils;

import ewe.util.Intable;

/**
 * Mutable Integer class, in case we need several integer Values
 * which could be reused in order to decrease object creation. 
 */
public class MutableInteger implements Intable {

	private int value;
	
	public MutableInteger() {
		value = -1;
	}
	
	public MutableInteger(final int initialValue) {
		value = initialValue;
	}

	public int getInt(){
		return value;
	}
	
	public void setInt(final int newValue) {
		value = newValue;
	}

}
