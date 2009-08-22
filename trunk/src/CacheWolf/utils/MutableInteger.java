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
	 * @param initialValue You have to guess.
	 */
	public MutableInteger(final int initialValue) {
		value = initialValue;
	}

	/**
	 * Gets the value
	 * @return value
	 */
	public int getInt(){
		return value;
	}
	
	/**
	 * Sets the value
	 * @param newValue value
	 */
	public void setInt(final int newValue) {
		value = newValue;
	}

}
