package CacheWolf.navi;

import ewe.fx.Dimension;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Map;
import ewe.util.Vector;
import ewe.util.Map.MapEntry;

public class MovingMapCache {

	/**
	 * Implements singleton pattern
	 */
		private static MovingMapCache singleton;
		
		/**
		 * Map<Key,Value> holding map-parts
		 */
		private Map cache;
		
		/**
		 * Map<String,Dimension> holding dimensions of the images
		 */
		private Map dimensions;
		
		/**
		 * private Xtor for singleton pattern
		 *
		 */
		private MovingMapCache (){
			cache = new Hashtable ();
			dimensions = new Hashtable ();
		}
		
		public static MovingMapCache getCache (){
			if(singleton==null){
				singleton = new MovingMapCache();
			}
			
			return singleton;
		}
		
		public MapImage get(String filename, int row, int column) {
			// TODO Auto-generated method stub
			Key key = new Key ();
			key.filename = filename;
			key.row = row;
			key.column = column;
			
			Value v = (Value) cache.get(key);
			if ( v != null){
				v.used = true;
				return v.image;
			}
			else{
				return null;
			}
		}
		
		public  void put(String filename, int row, int column, MapImage im) {
			// TODO Auto-generated method stub
			Key key = new Key ();
			key.filename = filename;
			key.row = row;
			key.column = column;
			
			cache.put(key, new Value (im));
		}

		public void clearUsedFlags() {
			for (Iterator i = cache.entries(); i.hasNext();) {
				MapEntry element =  (MapEntry) i.next();
				Value v = (Value) element.getValue();
				v.used = false;
			}
			
		}

		public void cleanCache() {
			//EWE does not suport the remove-operation on Hashtables.
			Vector keysToRemove = new Vector (); 
			for (Iterator i = cache.entries(); i.hasNext();) {
				MapEntry element =  (MapEntry) i.next();
				Value v = (Value) element.getValue();
				if (!v.used){
					keysToRemove.add(element.getKey());
				}
			}
			for (Iterator i = keysToRemove.iterator();i.hasNext();){
				Object element = i.next();
				cache.remove(element);
			}
		}


		private class Key{
			private String filename;
			private int row;
			private int column;

			public int hashCode() {
				final int PRIME = 31;
				int result = 1;
				result = PRIME * result + column;
				result = PRIME * result + ((filename == null) ? 0 : filename.hashCode());
				result = PRIME * result + row;
				return result;
			}

			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (! (obj instanceof Key))
					return false;
				final Key other = (Key) obj;
				if (column != other.column)
					return false;
				if (filename == null) {
					if (other.filename != null)
						return false;
				} else if (!filename.equals(other.filename))
					return false;
				if (row != other.row)
					return false;
				return true;
			}
			
		}

	private class Value {
		MapImage image;

		boolean used;

		public Value(MapImage im) {
			image = im;
			used = true;
		}

	}

	public Dimension getDimension(String filename) {
		return (Dimension) dimensions.get(filename);
	}

	public void putDimension(String filename, Dimension rect2) {
		dimensions.put(filename, rect2);
	}

}
