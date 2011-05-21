package CacheWolf.model;

import ewe.util.Vector;

public abstract class AbstractListModel {

	public Vector allItems;

	public Vector shownItems = new Vector ();
	public int sortCriteria;

	public AbstractListModel() {
		super();
	}

	/**
	 * Creates the list of objects to be shown in the List. With this methode You can hide some objects.
	 * Standardimplementation is to show all objects anytime. 
	 *  
	 */
	public void createShowSet(){
		shownItems.clear();
		shownItems.addAll(allItems);
	}
	
	/**
	 * Adds an Object to this model to last position.
	 * @param o
	 */
	public void add (Object o){
		allItems.add(o);
	}
	
	/**
	 * Returns the number of elements in the showset of this model
	 */
	public int size(){
		return shownItems.size();
	}
	
	/**
	 * Returns the Nth item in the showset of this model
	 */
	public Object get(int n){
		return shownItems.get(n);
	}
}